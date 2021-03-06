package de.blu.coordinator.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.converter.GameServerJsonConverter;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.service.ServiceInformation;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;

@Singleton
@Getter(AccessLevel.PRIVATE)
public final class GameServerFactory {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerJsonConverter gameServerJsonConverter;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private Injector injector;

    public GameServerInformation create(CloudType cloudType, boolean manually, Map<String, String> meta, ServiceInformation serviceInformation) {
        GameServerInformation gameServerInformation = this.getInjector().getInstance(GameServerInformation.class);

        if (cloudType.isStaticService()) {
            gameServerInformation.setName(cloudType.getName());
        } else {
            int id = 0;
            for (int i = 1; i < 5000; i++) {
                int finalI = i;
                if (this.getGameServerRepository().getGameServersByCloudType(cloudType).stream()
                        .noneMatch(gameServerInformation1 -> gameServerInformation1.getId() == finalI)) {
                    id = i;
                    break;
                }
            }

            gameServerInformation.setId(id);
            gameServerInformation.setName(cloudType.getName() + "-" + gameServerInformation.getId());
        }

        gameServerInformation.setServerStarterInformation(serviceInformation);
        gameServerInformation.setCloudType(cloudType);

        int port = 0;
        for (int currentPort = cloudType.getPortStart(); currentPort <= cloudType.getPortEnd(); currentPort++) {
            int finalCurrentPort = currentPort;
            if (this.getGameServerRepository().getGameServers().stream()
                    .filter(gameServerInformation1 -> gameServerInformation1.getServerStarterInformation().getIdentifier().equals(serviceInformation.getIdentifier()))
                    .noneMatch(gameServerInformation1 -> gameServerInformation1.getPort() == finalCurrentPort)) {
                port = currentPort;
                break;
            }
        }

        if (port == 0) {
            System.out.println("&cFailed to create Server for CloudType &e" + cloudType.getName());
            System.out.println("&cNo Port is left. Check your CloudType Configuration!");
            return null;
        }

        gameServerInformation.setPort(port);
        gameServerInformation.setState(GameServerInformation.State.CREATED);
        gameServerInformation.setManuallyStarted(manually);
        gameServerInformation.setMeta(meta);

        this.getGameServerRepository().getGameServers().add(gameServerInformation);
        System.out.println("Created GameServer " + gameServerInformation.getName() + " on Port " + port);
        String json = this.getGameServerJsonConverter().toJson(gameServerInformation);
        this.getRedisConnection().set("gameserver." + gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString(), json);

        return gameServerInformation;
    }
}
