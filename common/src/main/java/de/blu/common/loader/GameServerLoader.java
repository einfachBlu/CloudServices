package de.blu.common.loader;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;

import java.util.UUID;

@Singleton
@Getter
public final class GameServerLoader {

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private Injector injector;

    public void loadAllServers() {
        this.getGameServerRepository().getGameServers().clear();
        for (String gameServerFullName : this.getRedisConnection().getKeys("gameserver")) {
            String name = gameServerFullName.split("_")[0];
            UUID uniqueId = UUID.fromString(gameServerFullName.split("_")[1]);

            GameServerInformation gameServerInformation = this.getGameServerStorage().getGameServer(name, uniqueId);
            this.getGameServerRepository().getGameServers().add(gameServerInformation);
        }
    }
}
