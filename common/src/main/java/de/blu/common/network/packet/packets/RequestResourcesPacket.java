package de.blu.common.network.packet.packets;

import de.blu.common.network.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RequestResourcesPacket extends Packet {

    private int maxMemory = 0;
    private int usedMemory = 0;
    private double averageCpuLoad = 0;
    private String hostName = "";

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("averageCpuLoad", String.valueOf(this.getAverageCpuLoad()));
        data.put("usedMemory", String.valueOf(this.getUsedMemory()));
        data.put("maxMemory", String.valueOf(this.getMaxMemory()));
        data.put("hostName", this.getHostName());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.setAverageCpuLoad(Double.parseDouble(content.get("averageCpuLoad")));
        this.setUsedMemory(Integer.parseInt(content.get("usedMemory")));
        this.setMaxMemory(Integer.parseInt(content.get("maxMemory")));
        this.setHostName(content.get("hostName"));
    }
}
