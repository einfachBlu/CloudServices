package de.blu.common.network.packet.repository;

import com.google.inject.Singleton;
import de.blu.common.network.packet.Packet;
import lombok.Getter;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@Singleton
@Getter
public final class PacketRepository {

    private Collection<Class<? extends Packet>> packets = new HashSet<>();

    public PacketRepository() {
        Reflections reflections = new Reflections("de.blu.common.network.packet.packets");
        for (Class<? extends Packet> packetClass : reflections.getSubTypesOf(Packet.class)) {
            this.registerPackets(packetClass);
        }
    }

    @SafeVarargs
    public final void registerPackets(Class<? extends Packet>... packetClass) {
        this.getPackets().addAll(Arrays.asList(packetClass));
    }
}
