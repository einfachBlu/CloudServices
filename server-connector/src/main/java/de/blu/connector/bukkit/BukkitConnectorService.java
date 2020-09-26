package de.blu.connector.bukkit;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.GameServerUpdatePacket;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.connector.bukkit.listener.JoinPermissionValidator;
import de.blu.connector.bukkit.listener.OnlinePlayersUpdater;
import de.blu.connector.common.ConnectorService;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

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
        }, "GameServerUpdated");

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
