package de.blu.common.repository;

import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Getter
public final class GameServerRepository {

    private Collection<GameServerInformation> gameServers = new ArrayList<>();

    public GameServerInformation getGameServerByName(String name) {
        return this.getGameServers().stream()
                .filter(gameServerInformation -> gameServerInformation.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public GameServerInformation getGameServerByUniqueId(UUID uniqueId) {
        return this.getGameServers().stream()
                .filter(gameServerInformation -> gameServerInformation.getUniqueId().equals(uniqueId))
                .findFirst().orElse(null);
    }

    public Collection<GameServerInformation> getGameServersByCloudType(CloudType cloudType) {
        return this.getGameServers().stream()
                .filter(gameServerInformation -> gameServerInformation.getCloudType().equals(cloudType))
                .collect(Collectors.toList());
    }
}
