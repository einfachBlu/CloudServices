package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.ServiceInformation;
import de.blu.common.service.Services;
import de.blu.coordinator.request.ServerStopRequester;
import de.blu.coordinator.request.ServiceStopRequester;
import lombok.Getter;

@Singleton
@Getter
@Command(name = "stop", aliases = "stopserver")
public final class StopCommand extends CommandExecutor {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private ServerStopRequester serverStopRequester;

    @Inject
    private ServiceStopRequester serviceStopRequester;

    @Override
    public void execute(String label, String[] args) {
        if (args.length == 0) {
            System.out.println("&eUsage: stop <CloudType | ServerName | All | Starters>");
            return;
        }

        String target = args[0];

        CloudType cloudType = this.getCloudTypeRepository().getCloudTypeByName(target);
        if (cloudType != null) {
            System.out.println("&aStopping all Servers from CloudType " + cloudType.getName());
            for (GameServerInformation gameServerInformation : this.getGameServerRepository().getGameServersByCloudType(cloudType)) {
                System.out.println("&aStopping Server " + gameServerInformation.getName());
                this.getServerStopRequester().requestGameServerStop(gameServerInformation);
            }
            return;
        }

        if (target.equalsIgnoreCase("all")) {
            System.out.println("&aStopping all Servers");
            for (GameServerInformation gameServerInformation : this.getGameServerRepository().getGameServers()) {
                System.out.println("&aStopping Server " + gameServerInformation.getName());
                this.getServerStopRequester().requestGameServerStop(gameServerInformation);
            }
            return;
        }

        if (target.equalsIgnoreCase("starters")) {
            System.out.println("&aStopping all ServerStarters");
            for (ServiceInformation serviceInformation : this.getServiceRepository().getServicesBy(Services.SERVER_STARTER)) {
                System.out.println("&aStopping Service " + serviceInformation.getName() + " with Identifier: " + serviceInformation.getIdentifier().toString());
                this.getServiceStopRequester().requestServiceStop(serviceInformation);
            }
            return;
        }

        GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByName(target);
        if (gameServerInformation != null) {
            System.out.println("&aStopping Server " + gameServerInformation.getName());
            this.getServerStopRequester().requestGameServerStop(gameServerInformation);
            return;
        }

        System.out.println("&cThere is no Server or CloudType with the name '" + target + "'");
    }
}
