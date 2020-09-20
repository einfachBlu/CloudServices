package de.minimichecker.common.network.packet.receiver;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.minimichecker.common.network.packet.packets.Packet;
import de.minimichecker.common.network.packet.repository.PacketRepository;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
@Getter
public final class PacketReader {

    @Inject
    private PacketRepository packetRepository;

    @Inject
    private Injector injector;

    public Packet readPacket(String message) {
        Map<String, String> data = new HashMap<>();

        for (String entryString : message.split(";")) {
            String key = entryString.split("=")[0];
            String value = entryString.split("=")[1];

            data.put(key, value);
        }

        String packetName = data.get("packetName");
        UUID uuid = UUID.fromString(data.get("packetUniqueId"));
        Class<? extends Packet> packetClass = this.getPacketRepository().getPackets().stream()
                .filter(packetClass2 -> packetClass2.getSimpleName().equalsIgnoreCase(packetName))
                .findFirst().orElse(null);

        if (packetClass == null) {
            new NullPointerException("Unregistered Packet received: " + packetName).printStackTrace();
            return null;
        }

        Packet packet = this.getInjector().getInstance(packetClass);
        packet.read(data);
        packet.setUniqueId(uuid);
        return packet;
    }
}
