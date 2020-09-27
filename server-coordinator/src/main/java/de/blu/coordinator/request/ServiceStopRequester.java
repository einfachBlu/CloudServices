package de.blu.coordinator.request;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.RequestServiceStopPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.service.ServiceInformation;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;

@Singleton
@Getter
public final class ServiceStopRequester {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerStorage gameServerStorage;

    public void requestServiceStop(ServiceInformation serviceInformation) {
        RequestServiceStopPacket requestServiceStopPacket = this.getInjector().getInstance(RequestServiceStopPacket.class);
        requestServiceStopPacket.setIdentifier(serviceInformation.getIdentifier());

        // Request GameServerStop
        this.getPacketSender().sendPacket(requestServiceStopPacket, serviceInformation.getIdentifier().toString());
    }
}
