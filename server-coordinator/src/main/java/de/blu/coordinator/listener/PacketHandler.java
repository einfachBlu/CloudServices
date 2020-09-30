package de.blu.coordinator.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.handler.DefaultPacketHandler;
import de.blu.common.network.packet.packets.ServerStartedPacket;
import de.blu.common.network.packet.packets.ServerStoppedPacket;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.service.ServiceInformation;
import de.blu.common.service.Services;
import de.blu.coordinator.repository.ServerStarterHostRepository;
import de.blu.coordinator.request.ResourceRequester;
import lombok.Getter;

import java.util.UUID;

@Singleton
@Getter
public final class PacketHandler extends DefaultPacketHandler {

    @Inject
    private ServerStarterHostRepository serverStarterHostRepository;

    @Inject
    private ResourceRequester resourceRequester;

    @Override
    public void registerAll() {
        super.registerAll();

        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, "CallbackChannel");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, this.getSelfServiceInformation().getIdentifier().toString());

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof ServerStoppedPacket) {
                ServerStoppedPacket serverStoppedPacket = (ServerStoppedPacket) packet;
                UUID gameServerUniqueId = serverStoppedPacket.getGameServerUniqueId();

                GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(gameServerUniqueId);
                this.getGameServerRepository().getGameServers().remove(gameServerInformation);
                this.getGameServerStorage().removeGameServer(gameServerInformation);

                System.out.println("&e" + gameServerInformation.getName() + "&r is now &coffline&7.");
            }
        }, "ServerStopped");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof ServerStartedPacket) {
                ServerStartedPacket serverStartedPacket = (ServerStartedPacket) packet;
                UUID gameServerUniqueId = serverStartedPacket.getGameServerUniqueId();

                GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(gameServerUniqueId);
                this.getGameServerRepository().getGameServers().remove(gameServerInformation);

                gameServerInformation = this.getGameServerStorage().getGameServer(gameServerInformation.getName(), gameServerUniqueId);
                this.getGameServerRepository().getGameServers().add(gameServerInformation);

                System.out.println("&e" + gameServerInformation.getName() + "&r is now &aonline&7.");
            }
        }, "ServerStarted");
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof ServiceConnectedPacket) {
                ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
                ServiceInformation serviceInformation = serviceConnectedPacket.getServiceInformation();

                if (serviceInformation == null) {
                    return;
                }

                System.out.println("&aService connected: " + serviceInformation.getName() + " (" + serviceInformation.getIdentifier().toString() + ")");

                this.getServiceRepository().addService(serviceInformation);

                if (serviceInformation.getService().equals(Services.SERVER_STARTER)) {
                    // Host already stored from previous cache
                    if (this.getServerStarterHostRepository().getServerStarterHosts().containsKey(serviceInformation.getIdentifier())) {
                        return;
                    }

                    // Request Hostname
                    this.getResourceRequester().requestResources(requestResourcesPacket -> {
                        this.getServerStarterHostRepository().getServerStarterHosts().put(serviceInformation.getIdentifier(), requestResourcesPacket.getHostName());
                    }, serviceInformation);
                }
            }
        }, "ServiceConnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof ServiceDisconnectedPacket) {
                ServiceDisconnectedPacket serviceDisconnectedPacket = (ServiceDisconnectedPacket) packet;
                System.out.println("&cService disconnected: " + serviceDisconnectedPacket.getServiceInformation().getName() + " (" + serviceDisconnectedPacket.getServiceInformation().getIdentifier().toString() + ")");

                this.getServiceRepository().removeService(serviceDisconnectedPacket.getServiceInformation().getIdentifier());
            }
        }, "ServiceDisconnected");
    }
}
