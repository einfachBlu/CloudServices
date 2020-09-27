package de.blu.connector.bungeecord;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.*;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.connector.bungeecord.api.event.ServerStartedEvent;
import de.blu.connector.bungeecord.api.event.ServerStoppedEvent;
import de.blu.connector.bungeecord.api.event.ServerUpdatedEvent;
import de.blu.connector.bungeecord.command.HubCommand;
import de.blu.connector.bungeecord.listener.OnlinePlayersUpdater;
import de.blu.connector.bungeecord.listener.ServerKickListener;
import de.blu.connector.common.ConnectorService;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Getter
public final class BungeeConnectorService extends ConnectorService {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private ServerKickListener serverKickListener;

    @Inject
    private OnlinePlayersUpdater onlinePlayersUpdater;

    @Inject
    private HubCommand hubCommand;

    @Inject
    private Plugin plugin;

    @Inject
    private Injector injector;

    private Map<UUID, String> serverUniqueIdByName = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();

        ProxyServer.getInstance().getServers().remove("lobby");

        // Register Command
        ProxyServer.getInstance().getPluginManager().registerCommand(this.getPlugin(), this.getHubCommand());

        // Register Listener
        ProxyServer.getInstance().getPluginManager().registerListener(this.getPlugin(), this.getServerKickListener());
        ProxyServer.getInstance().getPluginManager().registerListener(this.getPlugin(), this.getOnlinePlayersUpdater());

        // Add ReconnectHandler
        ProxyServer.getInstance().setReconnectHandler(new ReconnectHandler() {
            @Override
            public ServerInfo getServer(ProxiedPlayer player) {
                return BungeeConnectorService.this.getFallbackServer(player);
            }

            @Override
            public void setServer(ProxiedPlayer player) {
            }

            @Override
            public void save() {
            }

            @Override
            public void close() {
            }
        });

        // Create ServerInfo Objects
        for (GameServerInformation gameServer : this.getGameServerRepository().getGameServers()) {
            if (!gameServer.getCloudType().getType().equals(CloudType.Type.BUKKIT) || !gameServer.getState().equals(GameServerInformation.State.ONLINE)) {
                continue;
            }

            this.registerServer(gameServer);
        }

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            PlayerSendToServerPacket playerSendToServerPacket = (PlayerSendToServerPacket) packet;

            UUID playerUniqueId = playerSendToServerPacket.getPlayer();
            String serverName = playerSendToServerPacket.getServerName();

            ServerInfo serverInfo = ProxyServer.getInstance().getServers().getOrDefault(serverName, null);
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUniqueId);

            if (serverInfo != null && player != null) {
                player.connect(serverInfo);
            }

            packet.sendBack();
        }, "PlayerSendToServer");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            GameServerUpdatePacket gameServerUpdatePacket = (GameServerUpdatePacket) packet;

            GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(gameServerUpdatePacket.getGameServerUniqueId());
            this.getGameServerRepository().getGameServers().remove(gameServerInformation);
            this.getGameServerRepository().getGameServers().add(gameServerUpdatePacket.getGameServerInformation());

            ServerUpdatedEvent serverUpdatedEvent = this.getInjector().getInstance(ServerUpdatedEvent.class);
            serverUpdatedEvent.setGameServerInformation(gameServerInformation);
            ProxyServer.getInstance().getPluginManager().callEvent(serverUpdatedEvent);
        }, "GameServerUpdated");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof ServerStartedPacket) {
                ServerStartedPacket serverStartedPacket = (ServerStartedPacket) packet;
                UUID gameServerUniqueId = serverStartedPacket.getGameServerUniqueId();
                String gameServerName = serverStartedPacket.getGameServerName();

                GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(gameServerUniqueId);
                this.getGameServerRepository().getGameServers().remove(gameServerInformation);
                gameServerInformation = this.getGameServerStorage().getGameServer(gameServerName, gameServerUniqueId);
                this.getGameServerRepository().getGameServers().add(gameServerInformation);

                System.out.println("&e" + gameServerInformation.getName() + "&r is now &aonline&7.");

                this.registerServer(gameServerInformation);
                ServerStartedEvent serverStartedEvent = this.getInjector().getInstance(ServerStartedEvent.class);
                serverStartedEvent.setGameServerInformation(gameServerInformation);
                ProxyServer.getInstance().getPluginManager().callEvent(serverStartedEvent);
            }
        }, "ServerStarted");
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof ServerStoppedPacket) {
                ServerStoppedPacket serverStoppedPacket = (ServerStoppedPacket) packet;
                UUID gameServerUniqueId = serverStoppedPacket.getGameServerUniqueId();

                GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(gameServerUniqueId);
                this.getGameServerRepository().getGameServers().remove(gameServerInformation);
                this.getGameServerStorage().removeGameServer(gameServerInformation);

                System.out.println("&e" + gameServerInformation.getName() + "&r is now &coffline&7.");

                this.unregisterServer(gameServerInformation.getUniqueId());
                ServerStoppedEvent serverStoppedEvent = this.getInjector().getInstance(ServerStoppedEvent.class);
                serverStoppedEvent.setGameServerInformation(gameServerInformation);
                ProxyServer.getInstance().getPluginManager().callEvent(serverStoppedEvent);
            }
        }, "ServerStopped");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            System.out.println("Service connected: " + serviceConnectedPacket.getServiceInformation().getName() + " (" + serviceConnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().addService(serviceConnectedPacket.getServiceInformation());
        }, "ServiceConnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceDisconnectedPacket serviceDisconnectedPacket = (ServiceDisconnectedPacket) packet;
            System.out.println("&cService disconnected: " + serviceDisconnectedPacket.getServiceInformation().getName() + " (" + serviceDisconnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().removeService(serviceDisconnectedPacket.getServiceInformation().getIdentifier());
        }, "ServiceDisconnected");
    }

    private void registerServer(GameServerInformation gameServer) {
        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(
                gameServer.getName(),
                InetSocketAddress.createUnresolved(gameServer.getHost(), gameServer.getPort()),
                "",
                false
        );

        this.getServerUniqueIdByName().put(gameServer.getUniqueId(), gameServer.getName());

        ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
        System.out.println("Registered Server " + serverInfo.getName());
    }

    private void unregisterServer(UUID uuid) {
        if (!this.getServerUniqueIdByName().containsKey(uuid)) {
            return;
        }

        String name = this.getServerUniqueIdByName().remove(uuid);
        ProxyServer.getInstance().getServers().remove(name);
        System.out.println("Unregistered Server " + name);
    }

    public ServerInfo getFallbackServer(ProxiedPlayer player) {
        List<GameServerInformation> fallbackServers = this.getGameServerRepository().getGameServers().stream()
                .filter(gameServerInformation -> this.getSelfGameServerInformation().getCloudType().getProxyFallbackPriorities().contains(gameServerInformation.getCloudType().getName()))
                .collect(Collectors.toList());

        if (fallbackServers.size() == 0) {
            return null;
        }

        ServerInfo currentServer = null;
        if (player.getServer() != null) {
            currentServer = player.getServer().getInfo();
        }

        if (currentServer != null) {
            ServerInfo finalCurrentServer = currentServer;
            fallbackServers = fallbackServers.stream()
                    .filter(gameServerInformation -> !gameServerInformation.getName().equalsIgnoreCase(finalCurrentServer.getName()))
                    .filter(gameServerInformation -> {
                        String permission = gameServerInformation.getCloudType().getPermission();
                        if (permission == null || permission.equalsIgnoreCase("")) {
                            return true;
                        }

                        return player.hasPermission(permission);
                    })
                    .collect(Collectors.toList());
        }

        if (fallbackServers.size() == 0) {
            return null;
        }

        GameServerInformation fallbackServer = fallbackServers.get(0);
        return ProxyServer.getInstance().getServers().get(fallbackServer.getName());
    }
}
