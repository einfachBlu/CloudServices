package de.blu.common.service;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.network.packet.sender.PacketSender;
import lombok.Getter;

@Singleton
@Getter
public final class ServiceConnectorBroadcast {

    @Inject
    private Injector injector;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private PacketSender packetSender;

    public void broadcastConnect() {
        ServiceConnectedPacket serviceConnectedPacket = this.getInjector().getInstance(ServiceConnectedPacket.class);
        serviceConnectedPacket.setServiceInformation(this.getSelfServiceInformation());
        this.getPacketSender().sendPacket(serviceConnectedPacket, "ServiceConnected");
    }

    public void broadcastDisconnect() {
        ServiceDisconnectedPacket serviceDisconnectedPacket = this.getInjector().getInstance(ServiceDisconnectedPacket.class);
        serviceDisconnectedPacket.setServiceInformation(this.getSelfServiceInformation());
        this.getPacketSender().sendPacket(serviceDisconnectedPacket, "ServiceDisconnected");
    }

    public void broadcastDisconnect(ServiceInformation targetServiceInformation) {
        ServiceDisconnectedPacket serviceDisconnectedPacket = this.getInjector().getInstance(ServiceDisconnectedPacket.class);
        serviceDisconnectedPacket.setServiceInformation(targetServiceInformation);
        this.getPacketSender().sendPacket(serviceDisconnectedPacket, "ServiceDisconnected");
    }
}
