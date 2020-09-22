package de.blu.common.network.packet.receiver;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.Packet;
import de.blu.common.network.packet.repository.PacketRepository;
import de.blu.common.service.SelfServiceInformation;
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

    @Inject
    private SelfServiceInformation selfServiceInformation;

    public Packet readPacket(String message) {
        Map<String, String> data = new HashMap<>();

        for (String entryString : message.split(Packet.SPLITERATOR)) {
            String key = entryString.split("=")[0];

            String value = "";
            if (entryString.split("=").length == 2) {
                value = entryString.split("=")[1];
            }

            if (key.equalsIgnoreCase("senderIdentifier")) {
                if (value.equalsIgnoreCase(this.getSelfServiceInformation().getIdentifier().toString())) {
                    // Stop executing here because we dont want to receive redis calls from our own
                    //return null;
                }
            }

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
