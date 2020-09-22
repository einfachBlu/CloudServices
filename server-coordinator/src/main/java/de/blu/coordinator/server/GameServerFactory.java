package de.blu.coordinator.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.service.ServiceInformation;
import lombok.AccessLevel;
import lombok.Getter;

@Singleton
@Getter(AccessLevel.PRIVATE)
public final class GameServerFactory {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private Injector injector;

    public GameServerInformation create(CloudType cloudType, ServiceInformation serviceInformation) {
        GameServerInformation gameServerInformation = this.getInjector().getInstance(GameServerInformation.class);

        if (cloudType.isStaticService()) {
            gameServerInformation.setName(cloudType.getName());
        } else {
            int id = 0;
            for (int i = 1; i < 2000; i++) {
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

        int port = 0;
        for (int currentPort = cloudType.getPortStart(); currentPort <= cloudType.getPortEnd(); currentPort++) {
            int finalCurrentPort = currentPort;
            if (this.getGameServerRepository().getGameServersByCloudType(cloudType).stream()
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
        gameServerInformation.setCloudType(cloudType);
        gameServerInformation.setState(GameServerInformation.State.CREATED);
        gameServerInformation.setServerStarterInformation(serviceInformation);

        this.getGameServerRepository().getGameServers().add(gameServerInformation);
        return gameServerInformation;
    }
}
