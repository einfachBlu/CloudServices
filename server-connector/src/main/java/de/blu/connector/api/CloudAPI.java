package de.blu.connector.api;

import com.google.inject.Inject;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.storage.GameServerStorage;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import de.blu.connector.common.sender.GameServerUpdateSender;
import de.blu.connector.common.sender.PlayerServerSender;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
public abstract class CloudAPI {

    @Getter
    private static CloudAPI instance;

    @Inject
    public GameServerRepository gameServerRepository;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private GameServerUpdateSender gameServerUpdateSender;

    @Inject
    private PlayerServerSender playerServerSender;

    public CloudAPI() {
        CloudAPI.instance = this;
    }

    public GameServerInformation getGameServerInformation() {
        return this.getSelfGameServerInformationProvider().getGameServerInformation();
    }

    public Collection<GameServerInformation> getAllServers() {
        return new ArrayList<>(this.getGameServerRepository().getGameServers());
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

    public void setGameState(String newGameState) {
        GameServerInformation gameServerInformation = this.getGameServerInformation();
        gameServerInformation.setGameState(newGameState);

        this.getGameServerStorage().saveGameServer(gameServerInformation);
        this.getGameServerUpdateSender().sendServerUpdated();
    }

    public void setExtra(String extra) {
        GameServerInformation gameServerInformation = this.getGameServerInformation();
        gameServerInformation.setExtra(extra);

        this.getGameServerStorage().saveGameServer(gameServerInformation);
        this.getGameServerUpdateSender().sendServerUpdated();
    }

    public void sendToServer(UUID player, String serverName) {
        this.sendToServer(player, serverName, aVoid -> {
        });
    }

    public void sendToServer(UUID player, String serverName, Consumer<Void> doneCallback) {
        this.getPlayerServerSender().sendToServer(player, serverName, doneCallback);
    }
}
