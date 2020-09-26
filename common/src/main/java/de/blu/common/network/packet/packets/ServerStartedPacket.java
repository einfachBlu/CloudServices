package de.blu.common.network.packet.packets;

import com.google.inject.Inject;
import com.google.inject.Injector;
import de.blu.common.network.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ServerStartedPacket extends Packet {

    private UUID gameServerUniqueId = null;
    private String gameServerName = "";

    @Inject
    private Injector injector;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("gameServerName", this.getGameServerName());
        data.put("gameServerUniqueId", this.getGameServerUniqueId().toString());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.setGameServerName(content.get("gameServerName"));
        this.setGameServerUniqueId(UUID.fromString(content.get("gameServerUniqueId")));
    }
}
