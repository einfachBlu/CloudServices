package de.blu.common.network.packet.packets;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ServiceConnectedPacket extends Packet {

    private String serviceName;
    private UUID serviceIdentifier;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("serviceName", this.getServiceName());
        data.put("serviceIdentifier", this.getServiceIdentifier().toString());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.setServiceName(content.get("serviceName"));
        this.setServiceIdentifier(UUID.fromString(content.get("serviceIdentifier")));
    }
}
