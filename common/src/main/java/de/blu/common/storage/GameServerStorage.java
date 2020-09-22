package de.blu.common.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.converter.GameServerJsonConverter;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import lombok.Getter;

import java.util.UUID;

@Singleton
@Getter
public final class GameServerStorage {

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private GameServerJsonConverter gameServerJsonConverter;

    public void saveGameServer(GameServerInformation gameServerInformation) {
        String json = this.getGameServerJsonConverter().toJson(gameServerInformation);
        this.getRedisConnection().set("gameserver." + gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString(), json);
    }

    public GameServerInformation getGameServer(String name, UUID uniqueId) {
        if (!this.getRedisConnection().contains("gameserver." + name + "_" + uniqueId.toString())) {
            return null;
        }

        String json = this.getRedisConnection().get("gameserver." + name + "_" + uniqueId.toString());
        return this.getGameServerJsonConverter().fromJson(json);
    }

    public void removeGameServer(GameServerInformation gameServerInformation) {
        this.getRedisConnection().remove("gameserver." + gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString());
    }
}
