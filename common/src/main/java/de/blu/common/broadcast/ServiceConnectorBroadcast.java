package de.blu.common.broadcast;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.util.ApplicationIdentifierProvider;
import lombok.Getter;

@Singleton
@Getter
public final class ServiceConnectorBroadcast {

    @Inject
    private Injector injector;

    @Inject
    private ApplicationIdentifierProvider applicationIdentifierProvider;

    @Inject
    private PacketSender packetSender;

    public void broadcastConnect(String serviceName){
        ServiceConnectedPacket serviceConnectedPacket = this.getInjector().getInstance(ServiceConnectedPacket.class);
        serviceConnectedPacket.setServiceName(serviceName);
        serviceConnectedPacket.setServiceIdentifier(this.getApplicationIdentifierProvider().getUniqueId());
        this.getPacketSender().sendPacket(serviceConnectedPacket, "ServiceConnected");
    }

    public void broadcastDisconnect(String serviceName){
        ServiceDisconnectedPacket serviceDisconnectedPacket = this.getInjector().getInstance(ServiceDisconnectedPacket.class);
        serviceDisconnectedPacket.setServiceName(serviceName);
        serviceDisconnectedPacket.setServiceIdentifier(this.getApplicationIdentifierProvider().getUniqueId());
        this.getPacketSender().sendPacket(serviceDisconnectedPacket, "ServiceDisconnected");
    }
}
