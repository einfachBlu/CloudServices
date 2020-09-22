package de.blu.common.network.packet.packets;

import com.google.inject.Inject;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.data.CloudType;
import de.blu.common.repository.CloudTypeRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Setter
public class RequestCloudTypesPacket extends Packet {

    private Collection<CloudType> cloudTypes = new ArrayList<>();
    private String json = "";

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private CloudTypeConfigLoader cloudTypeConfigLoader;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("jsonContent", this.getJson());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        String jsonContent = content.get("jsonContent");

        if (jsonContent.equalsIgnoreCase("")) {
            return;
        }

        this.cloudTypes = this.getCloudTypeConfigLoader().loadFromJson(jsonContent);
    }
}
