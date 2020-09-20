package de.blu.common.network.packet.sender;

import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.Packet;
import lombok.Getter;

import java.util.Map;

@Singleton
@Getter
public final class PacketWriter {

    public Map<String, String> writePacket(Packet packet, Map<String, String> data) {
        String packetName = packet.getClass().getSimpleName();

        data = packet.write(data);
        data.put("packetName", packetName);
        data.put("packetUniqueId", packet.getUniqueId().toString());

        return data;
    }
}
