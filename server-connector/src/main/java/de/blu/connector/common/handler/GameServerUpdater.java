package de.blu.connector.common.handler;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.storage.GameServerStorage;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import de.blu.connector.common.sender.GameServerUpdateSender;
import lombok.Getter;

@Singleton
@Getter
public final class GameServerUpdater {

    @Inject
    private Injector injector;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    @Inject
    private GameServerUpdateSender gameServerUpdateSender;

    @Inject
    private GameServerStorage gameServerStorage;

    public void increaseOnlinePlayers() {
        GameServerInformation gameServerInformation = this.getSelfGameServerInformationProvider().getGameServerInformation();
        gameServerInformation.setOnlinePlayers(gameServerInformation.getOnlinePlayers() + 1);
        this.getGameServerStorage().saveGameServer(gameServerInformation);

        this.getGameServerUpdateSender().sendServerUpdated();
    }

    public void decreaseOnlinePlayers() {
        GameServerInformation gameServerInformation = this.getSelfGameServerInformationProvider().getGameServerInformation();
        gameServerInformation.setOnlinePlayers(gameServerInformation.getOnlinePlayers() - 1);
        this.getGameServerStorage().saveGameServer(gameServerInformation);

        this.getGameServerUpdateSender().sendServerUpdated();
    }
}
