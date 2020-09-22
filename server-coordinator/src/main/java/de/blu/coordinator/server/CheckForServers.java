package de.blu.coordinator.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.Services;
import de.blu.coordinator.request.ResourceRequester;
import lombok.Getter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

@Singleton
@Getter
public final class CheckForServers {

    private static final int CHECK_INTERVAL = 3000;

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

    public void startTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                CheckForServers.this.startServersIfNeeded();
            }
        }, 0, CHECK_INTERVAL);
    }

    private void startServersIfNeeded() {
        // Check if at least one ServerStarter is online
        if (this.getServiceRepository().getServicesBy(Services.SERVER_STARTER).size() == 0) {
            return;
        }

        // Check which CloudType needs to be started and set by priority
        CloudType targetCloudType = null;
        for (CloudType cloudType : this.getCloudTypeRepository().getCloudTypes()) {
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
                continue;
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

        // Get best ServerStarter Service
        CloudType cloudType = targetCloudType;
        this.getServerStarterReceiver().getBestServerStarter(targetCloudType, bestServerStarter -> {
            System.out.println("&bTry to Start Server of CloudType " + cloudType.getName() + " on ServerStarter: " + bestServerStarter.getIdentifier().toString());

            // TODO: Create GameServerData for $targetCloudType
            // TODO: Request to start a CloudType it on $bestServerStarter
        });
    }
}
