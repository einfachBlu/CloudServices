package de.blu.common.network.packet.repository;

import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.*;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@Singleton
@Getter
public final class PacketRepository {

    private Collection<Class<? extends Packet>> packets = new HashSet<>();

    public PacketRepository() {
        this.registerPackets(RequestCloudTypesPacket.class);
        this.registerPackets(ServiceConnectedPacket.class);
        this.registerPackets(ServiceDisconnectedPacket.class);
    }

    @SafeVarargs
    public final void registerPackets(Class<? extends Packet>... packetClass) {
        this.getPackets().addAll(Arrays.asList(packetClass));
    }
}
