package de.blu.common.network.packet.packets;

import com.google.inject.Inject;
import com.google.inject.Injector;
import de.blu.common.service.ServiceInformation;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ServiceConnectedPacket extends Packet {

    private ServiceInformation serviceInformation;

    @Inject
    private Injector injector;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("serviceName", this.getServiceInformation().getName());
        data.put("serviceIdentifier", this.getServiceInformation().getIdentifier().toString());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.serviceInformation = this.getInjector().getInstance(ServiceInformation.class);
        this.getServiceInformation().setName(content.get("serviceName"));
        this.getServiceInformation().setIdentifier(UUID.fromString(content.get("serviceIdentifier")));
    }
}
