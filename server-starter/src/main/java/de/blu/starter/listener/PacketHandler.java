package de.blu.starter.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.logging.Logger;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.CloudTypeRepository;
import lombok.Getter;

@Singleton
@Getter
public final class PacketHandler {

    @Inject
    private PacketSender packetSender;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private Logger logger;

    public void registerAll() {
        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, "CallbackChannel");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            this.getLogger().info("Service connected: " + serviceConnectedPacket.getServiceName() + " (" + serviceConnectedPacket.getServiceIdentifier() + ")");
        }, "ServiceConnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            this.getLogger().info("Service disconnected: " + serviceConnectedPacket.getServiceName() + " (" + serviceConnectedPacket.getServiceIdentifier() + ")");
        }, "ServiceDisconnected");
    }
}
