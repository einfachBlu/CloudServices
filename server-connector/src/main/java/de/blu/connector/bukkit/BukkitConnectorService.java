package de.blu.connector.bukkit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.*;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.connector.bukkit.api.event.ServerStartedEvent;
import de.blu.connector.bukkit.api.event.ServerStoppedEvent;
import de.blu.connector.bukkit.api.event.ServerUpdatedEvent;
import de.blu.connector.bukkit.listener.JoinPermissionValidator;
import de.blu.connector.bukkit.listener.OnlinePlayersUpdater;
import de.blu.connector.common.ConnectorService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@Singleton
@Getter
public final class BukkitConnectorService extends ConnectorService {

    @Inject
    private PacketListenerRepository packetListenerRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private JavaPlugin plugin;

    @Inject
    private JoinPermissionValidator joinPermissionValidator;

    @Inject
    private OnlinePlayersUpdater onlinePlayersUpdater;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private Injector injector;

    @Override
    public void onEnable() {
        super.onEnable();

        this.getPlugin().getServer().getPluginManager().registerEvents(this.getJoinPermissionValidator(), this.getPlugin());
        this.getPlugin().getServer().getPluginManager().registerEvents(this.getOnlinePlayersUpdater(), this.getPlugin());


        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            GameServerUpdatePacket gameServerUpdatePacket = (GameServerUpdatePacket) packet;

            GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(gameServerUpdatePacket.getGameServerUniqueId());
            this.getGameServerRepository().getGameServers().remove(gameServerInformation);
            this.getGameServerRepository().getGameServers().add(gameServerUpdatePacket.getGameServerInformation());

            ServerUpdatedEvent serverUpdatedEvent = this.getInjector().getInstance(ServerUpdatedEvent.class);
            serverUpdatedEvent.setGameServerInformation(gameServerInformation);
            Bukkit.getServer().getPluginManager().callEvent(serverUpdatedEvent);
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

                ServerStartedEvent serverStartedEvent = this.getInjector().getInstance(ServerStartedEvent.class);
                serverStartedEvent.setGameServerInformation(gameServerInformation);
                Bukkit.getServer().getPluginManager().callEvent(serverStartedEvent);
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

                ServerStoppedEvent serverStoppedEvent = this.getInjector().getInstance(ServerStoppedEvent.class);
                serverStoppedEvent.setGameServerInformation(gameServerInformation);
                Bukkit.getServer().getPluginManager().callEvent(serverStoppedEvent);
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
}
