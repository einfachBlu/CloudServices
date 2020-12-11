package de.blu.coordinator.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.Services;
import de.blu.coordinator.repository.ServerStarterHostRepository;
import de.blu.coordinator.request.ResourceRequester;
import de.blu.coordinator.request.ServerStartRequester;
import de.blu.coordinator.request.ServerStopRequester;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Singleton
@Getter
public final class CheckForServers {

    private static final int CHECK_INTERVAL = 1000;
    private static final int CREATING_SERVER_TIMEOUT = 15 * 1000;

    @Setter
    private boolean creatingServer = false;

    @Setter
    private long lastCreatingServer = 0;

    @Inject
    private ExecutorService executorService;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private ResourceRequester resourceRequester;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private ServerStarterReceiver serverStarterReceiver;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerFactory gameServerFactory;

    @Inject
    private ServerStartRequester serverStartRequester;

    @Inject
    private ServerStopRequester serverStopRequester;

    @Inject
    private ServerStarterHostRepository serverStarterHostRepository;

    public void startTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    CheckForServers.this.startServersIfNeeded();
                    CheckForServers.this.stopServersIfPossible();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, CHECK_INTERVAL);
    }

    private void stopServersIfPossible() {
        // Check if at least one ServerStarter is online
        if (this.getServiceRepository().getServicesBy(Services.SERVER_STARTER).size() == 0) {
            return;
        }

        // Check which Server can be stopped
        GameServerInformation targetGameServerInformation = null;
        for (CloudType cloudType : this.getCloudTypeRepository().getCloudTypes()) {
            // We filter out every manually started server
            int currentOnlineAmount = (int) this.getGameServerRepository().getGameServersByCloudType(cloudType)
                    .stream()
                    .filter(gameServerInformation -> !gameServerInformation.isManuallyStarted())
                    .count();

            if (currentOnlineAmount <= cloudType.getMinOnlineServers()) {
                // Dont need to, because its not or equals to the minOnlineServers()
                continue;
            }

            int playersInCloudType = this.getGameServerRepository().getGameServersByCloudType(cloudType)
                    .stream()
                    .mapToInt(GameServerInformation::getOnlinePlayers)
                    .sum();
            int maxPlayersForCloudType = this.getGameServerRepository().getGameServersByCloudType(cloudType)
                    .stream()
                    .mapToInt(GameServerInformation::getMaxPlayers)
                    .sum();

            if (playersInCloudType >= maxPlayersForCloudType / 4) {
                continue;
            }

            // We need to remove the servers with 0 players online
            List<GameServerInformation> gameServers = this.getGameServerRepository().getGameServersByCloudType(cloudType)
                    .stream()
                    .filter(gameServerInformation -> !gameServerInformation.isManuallyStarted())
                    .filter(gameServerInformation -> gameServerInformation.getOnlinePlayers() <= 0)
                    .collect(Collectors.toList());

            if (gameServers.size() == 0) {
                // No Empty Server found
                continue;
            }

            targetGameServerInformation = gameServers.get(0);
            break;
        }

        if (targetGameServerInformation == null) {
            // No GameServer needs to be stopped
            return;
        }

        this.getServerStopRequester().requestGameServerStop(targetGameServerInformation);
        System.out.println("ServerStop Request for &e" + targetGameServerInformation.getName() +
                "&r to ServerStarter &e" + targetGameServerInformation.getServerStarterInformation().getIdentifier().toString());
    }

    private synchronized void startServersIfNeeded() {
        if (this.creatingServer) {
            // Wait until the last server Creation was done

            // Check for Timeout
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastCreatingServer >= CREATING_SERVER_TIMEOUT) {
                System.out.println("Callback missing of creation of Server!");
                this.creatingServer = false;
            } else {
                return;
            }
        }

        // Check if at least one ServerStarter is online
        if (this.getServiceRepository().getServicesBy(Services.SERVER_STARTER).size() == 0) {
            return;
        }

        // Check which CloudType needs to be started and set by priority
        CloudType targetCloudType = null;
        for (CloudType cloudType : this.getCloudTypeRepository().getCloudTypes()) {
            if (cloudType.getType().equals(CloudType.Type.TEMPLATE)) {
                continue;
            }

            int currentOnlineAmount = this.getGameServerRepository().getGameServersByCloudType(cloudType).size();

            if (cloudType.isStaticService() && currentOnlineAmount >= 1) {
                // Allow only 1 server at once running for static Service
                continue;
            }

            if (currentOnlineAmount >= cloudType.getMaxOnlineServers()) {
                // MaxOnlineServers already reached
                continue;
            }

            if (currentOnlineAmount >= cloudType.getMinOnlineServers()) {
                // minOnlineAmount already reached
                // Check here if enough players are online
                int playersInCloudType = this.getGameServerRepository().getGameServersByCloudType(cloudType)
                        .stream()
                        .mapToInt(GameServerInformation::getOnlinePlayers)
                        .sum();
                int maxPlayersForCloudType = this.getGameServerRepository().getGameServersByCloudType(cloudType)
                        .stream()
                        .mapToInt(GameServerInformation::getMaxPlayers)
                        .sum();

                if (playersInCloudType <= maxPlayersForCloudType / 2) {
                    continue;
                }
            }

            if (cloudType.getHosts().size() > 0) {
                // Check if there is at least one serverstarter to start this cloudtype
                boolean found = false;
                for (String host : this.getServerStarterHostRepository().getServerStarterHosts().values()) {
                    if (cloudType.getHosts().contains(host)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // Cant start this CloudType because no ServerStarter can start it
                    continue;
                }
            }

            if (targetCloudType == null) {
                targetCloudType = cloudType;
                continue;
            }

            if (cloudType.getPriority() <= targetCloudType.getPriority()) {
                // Has no higher Priority to startup
                continue;
            }

            targetCloudType = cloudType;
        }

        if (targetCloudType == null) {
            // No CloudType needs to be started
            return;
        }

        // Request ServerStart
        CloudType cloudType = targetCloudType;
        this.creatingServer = true;
        this.lastCreatingServer = System.currentTimeMillis();
        this.getServerStartRequester().requestGameServerStart(cloudType, false, new HashMap<>(), gameServerInformation -> {
            this.creatingServer = false;
        });
    }
}
