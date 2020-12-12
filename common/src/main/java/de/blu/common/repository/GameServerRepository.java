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

    private Collection<GameServerInformation> gameServers = new ArrayList<GameServerInformation>() {
        @Override
        public boolean add(GameServerInformation o) {
            if (o == null) {
                new NullPointerException("Null GameServerInformation was added to the GameServerRepository").printStackTrace();
                return false;
            }

            if(this.stream().anyMatch(gameServerInformation -> gameServerInformation.getUniqueId().equals(o.getUniqueId()))){
                return false;
            }

            return super.add(o);
        }
    };

    public GameServerInformation getGameServerByName(String name) {
        return new ArrayList<>(this.getGameServers()).stream()
                .filter(gameServerInformation -> gameServerInformation.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    public GameServerInformation getGameServerByUniqueId(UUID uniqueId) {
        return new ArrayList<>(this.getGameServers()).stream()
                .filter(gameServerInformation -> gameServerInformation.getUniqueId().equals(uniqueId))
                .findFirst().orElse(null);
    }

    public Collection<GameServerInformation> getGameServersByCloudType(CloudType cloudType) {
        return new ArrayList<>(this.getGameServers()).stream()
                .filter(gameServerInformation -> {
                    try {
                        boolean equals = gameServerInformation.getCloudType().equals(cloudType);
                        return equals;
                    } catch (NullPointerException e) {
                        System.out.println("Where is the NPE? Server in iteration: " + gameServerInformation);
                        if (gameServerInformation == null) {
                            System.out.println("gameserverInformation is null");
                            return false;
                        }
                        if (gameServerInformation.getCloudType() == null) {
                            System.out.println("gameServerInformation.getCloudType() is null");
                            return false;
                        }
                        if (cloudType == null) {
                            System.out.println("cloudType is null");
                            return false;
                        }
                    }

                    return false;
                })
                .collect(Collectors.toList());
    }
}
