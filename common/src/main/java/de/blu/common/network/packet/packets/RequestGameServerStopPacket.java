package de.blu.common.network.packet.packets;

import com.google.inject.Inject;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.network.packet.Packet;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class RequestGameServerStopPacket extends Packet {

    private String gameServerName;
    private UUID gameServerUniqueId;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private GameServerStorage gameServerStorage;

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

    public GameServerInformation getGameServerInformation() {
        return this.getGameServerStorage().getGameServer(this.getGameServerName(), this.getGameServerUniqueId());
    }
}
