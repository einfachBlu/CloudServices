package de.blu.connector.api;

import com.google.inject.Inject;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.GameServerRepository;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import lombok.Getter;

@Getter
public abstract class CloudAPI {

    @Getter
    private static CloudAPI instance;

    @Inject
    public GameServerRepository gameServerRepository;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    public CloudAPI() {
        CloudAPI.instance = this;
    }

    public GameServerInformation getGameServerInformation() {
        return this.getSelfGameServerInformationProvider().getGameServerInformation();
    }

    public int getOnlinePlayers() {
        return this.getGameServerRepository().getGameServers().stream()
                .mapToInt(GameServerInformation::getOnlinePlayers).sum();
    }

    public int getMaxPlayers() {
        return this.getGameServerRepository().getGameServers().stream()
                .filter(gameServerInformation -> gameServerInformation.getCloudType().getType().equals(CloudType.Type.BUNGEECORD))
                .mapToInt(GameServerInformation::getMaxPlayers)
                .sum();
    }
}
