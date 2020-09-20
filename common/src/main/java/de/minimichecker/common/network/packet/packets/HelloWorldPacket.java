package de.minimichecker.common.network.packet.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class HelloWorldPacket extends Packet {

    private String message = "HelloWorld";
    private UUID uuid = UUID.randomUUID();

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("message", this.message);
        data.put("uuid", this.uuid.toString());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.message = content.get("message");
        this.uuid = UUID.fromString(content.get("uuid"));
    }

    @Override
    public String toString() {
        return "HelloWorldPacket{" +
                "message='" + message + '\'' +
                ", uuid=" + uuid +
                ", packetUniqueId=" + this.getUniqueId() +
                '}';
    }
}
