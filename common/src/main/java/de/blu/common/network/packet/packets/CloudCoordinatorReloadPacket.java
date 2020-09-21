package de.blu.common.network.packet.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class CloudCoordinatorReloadPacket extends Packet {

    @Override
    public Map<String, String> write(Map<String, String> data) {
        return data;
    }

    @Override
    public void read(Map<String, String> content) {
    }
}
