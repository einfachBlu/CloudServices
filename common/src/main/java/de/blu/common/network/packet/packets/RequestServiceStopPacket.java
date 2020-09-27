package de.blu.common.network.packet.packets;

import de.blu.common.network.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class RequestServiceStopPacket extends Packet {

    private UUID identifier;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("identifier", this.getIdentifier().toString());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.setIdentifier(UUID.fromString(content.get("identifier")));
    }
}
