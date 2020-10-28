package de.blu.connector.api;

import com.google.inject.Inject;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.storage.GameServerStorage;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import de.blu.connector.common.repository.ServerStartedCallbackRepository;
import de.blu.connector.common.sender.GameServerStartRequester;
import de.blu.connector.common.sender.GameServerUpdateSender;
import de.blu.connector.common.sender.PlayerServerSender;
import lombok.Getter;

import java.util.*;
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

    @Inject
    private GameServerStartRequester gameServerStartRequester;

    @Inject
    private ServerStartedCallbackRepository serverStartedCallbackRepository;

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

        this.saveGameServer(gameServerInformation);
    }

    public void setExtra(String extra) {
        GameServerInformation gameServerInformation = this.getGameServerInformation();
        gameServerInformation.setExtra(extra);

        this.saveGameServer(gameServerInformation);
    }

    public void saveGameServer(GameServerInformation gameServerInformation) {
        this.getGameServerStorage().saveGameServer(gameServerInformation);
        this.getGameServerUpdateSender().sendServerUpdated(gameServerInformation);
    }

    public void sendToServer(UUID player, String serverName) {
        this.sendToServer(player, serverName, aVoid -> {
        });
    }

    public void sendToServer(UUID player, String serverName, Consumer<Void> doneCallback) {
        this.getPlayerServerSender().sendToServer(player, serverName, doneCallback);
    }

    public void startServer(CloudType cloudType, Consumer<Boolean> startingCallback, Consumer<GameServerInformation> startedCallback) {
        this.startServer(cloudType, startingCallback, startedCallback, new HashMap<>());
    }

    public void startServer(CloudType cloudType, Consumer<Boolean> startingCallback, Consumer<GameServerInformation> startedCallback, Map<String, String> meta) {
        UUID callbackUniqueId = UUID.randomUUID();
        meta.put("apiStartedId", callbackUniqueId.toString());

        this.getGameServerStartRequester().request(cloudType, meta, requestGameServerStartPacket -> {
            if (!requestGameServerStartPacket.isSuccess()) {
                startingCallback.accept(false);
                startedCallback.accept(null);
                return;
            }

            startingCallback.accept(true);
            this.getServerStartedCallbackRepository().getCallbacks().put(callbackUniqueId, startedCallback);
        });
    }
}
