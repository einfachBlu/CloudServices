package de.blu.common.network.packet.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RequestResourcesPacket extends Packet {

    private int maxMemory;
    private int usedMemory;
    private int usedCpu;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("usedCpu", String.valueOf(this.getUsedCpu()));
        data.put("usedMemory", String.valueOf(this.getUsedMemory()));
        data.put("maxMemory", String.valueOf(this.getMaxMemory()));

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.setUsedCpu(Integer.parseInt(content.get("usedCpu")));
        this.setUsedMemory(Integer.parseInt(content.get("usedMemory")));
        this.setMaxMemory(Integer.parseInt(content.get("maxMemory")));
    }
}
