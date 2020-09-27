package de.blu.common.network.packet.packets;

import com.google.inject.Inject;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.network.packet.Packet;
import de.blu.common.repository.GameServerRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class PlayerSendToServerPacket extends Packet {

    private UUID player;
    private String serverName;

    private GameServerInformation targetGameServerInformation;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private GameServerRepository gameServerRepository;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("player", this.getPlayer().toString());
        data.put("serverName", this.getServerName());

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.setPlayer(UUID.fromString(content.get("player")));
        this.setServerName(content.get("serverName"));

        this.setTargetGameServerInformation(this.getGameServerRepository().getGameServerByName(this.getServerName()));
    }
}
