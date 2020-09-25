package de.blu.connector.bungeecord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.ServiceInformation;
import de.blu.connector.bungeecord.listener.ServerKickListener;
import de.blu.connector.common.ConnectorService;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.*;
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
    private Plugin plugin;

    private Map<UUID, String> serverUniqueIdByName = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();

        // Register Listener
        ProxyServer.getInstance().getPluginManager().registerListener(this.getPlugin(), this.getServerKickListener());

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
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            System.out.println("Service connected: " + serviceConnectedPacket.getServiceInformation().getName() + " (" + serviceConnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().addService(serviceConnectedPacket.getServiceInformation());

            ServiceInformation serviceInformation = serviceConnectedPacket.getServiceInformation();
            GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(serviceInformation.getIdentifier());

            if (gameServerInformation == null || !gameServerInformation.getCloudType().getType().equals(CloudType.Type.BUKKIT)) {
                return;
            }

            this.registerServer(gameServerInformation);
        }, "ServiceConnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceDisconnectedPacket serviceDisconnectedPacket = (ServiceDisconnectedPacket) packet;
            System.out.println("&cService disconnected: " + serviceDisconnectedPacket.getServiceInformation().getName() + " (" + serviceDisconnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().removeService(serviceDisconnectedPacket.getServiceInformation().getIdentifier());

            ServiceInformation serviceInformation = serviceDisconnectedPacket.getServiceInformation();

            this.unregisterServer(serviceInformation.getIdentifier());
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
                    .collect(Collectors.toList());
        }

        GameServerInformation fallbackServer = fallbackServers.get(new Random().nextInt(fallbackServers.size()));

        return ProxyServer.getInstance().getServers().get(fallbackServer.getName());
    }
}
