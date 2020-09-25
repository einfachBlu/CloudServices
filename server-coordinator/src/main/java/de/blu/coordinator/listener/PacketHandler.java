package de.blu.coordinator.listener;

import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.handler.DefaultPacketHandler;
import de.blu.common.network.packet.packets.ServerStartedPacket;
import de.blu.common.network.packet.packets.ServerStoppedPacket;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.service.Services;
import lombok.Getter;

import java.util.UUID;

@Singleton
@Getter
public final class PacketHandler extends DefaultPacketHandler {

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
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            if (!serviceConnectedPacket.getServiceInformation().getService().equals(Services.SERVER_CONNECTOR)) {
                System.out.println("&aService connected: " + serviceConnectedPacket.getServiceInformation().getName() + " (" + serviceConnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            }

            this.getServiceRepository().addService(serviceConnectedPacket.getServiceInformation());
        }, "ServiceConnected");
    }
}
