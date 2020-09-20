package de.minimichecker.common.network.packet.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public abstract class Packet {

    public UUID uniqueId = UUID.randomUUID();

    public Map<String, String> write(Map<String, String> data) {
        return data;
    }

    public void read(Map<String, String> content) {
    }
}
