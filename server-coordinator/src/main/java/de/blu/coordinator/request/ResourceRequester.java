package de.blu.coordinator.request;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.RequestResourcesPacket;
import de.blu.common.network.packet.sender.PacketSender;
import lombok.Getter;

import java.util.function.Consumer;

@Singleton
@Getter
public final class ResourceRequester {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    public void requestResources(Consumer<RequestResourcesPacket> callback) {
        RequestResourcesPacket requestResourcesPacket = this.getInjector().getInstance(RequestResourcesPacket.class);

        // Request Resources
        this.getPacketSender().sendRequestPacket(requestResourcesPacket, callback, "RequestResources");
    }
}
