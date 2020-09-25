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
import de.blu.connector.common.ConnectorService;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
@Getter
public final class BungeeConnectorService extends ConnectorService {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    @Inject
    private ServiceRepository serviceRepository;

    private Map<UUID, String> serverUniqueIdByName = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();

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
}
