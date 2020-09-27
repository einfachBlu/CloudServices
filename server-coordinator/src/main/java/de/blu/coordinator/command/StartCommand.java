package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import de.blu.common.data.CloudType;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.coordinator.request.ServerStartRequester;
import lombok.Getter;

@Singleton
@Getter
@Command(name = "start", aliases = "startserver")
public final class StartCommand extends CommandExecutor {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private ServerStartRequester serverStartRequester;

    @Override
    public void execute(String label, String[] args) {
        if (args.length == 0) {
            System.out.println("&eUsage: start <CloudType>");
            return;
        }

        String target = args[0];

        CloudType cloudType = this.getCloudTypeRepository().getCloudTypeByName(target);
        if (cloudType != null) {
            this.getServerStartRequester().requestGameServerStart(cloudType, true);
            return;
        }

        System.out.println("&cThere is no CloudType with the name '" + target + "'");
    }
}
