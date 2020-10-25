package de.blu.connector.bukkit.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.connector.api.CloudAPI;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@Getter
public final class HubCommand implements CommandExecutor {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        GameServerInformation gameServerInformation = this.getSelfGameServerInformationProvider().getGameServerInformation();
        List<GameServerInformation> fallbackServers = new ArrayList<>();

        GameServerInformation proxyServer = this.getGameServerRepository().getGameServers().stream()
                .filter(gameServerInformation1 -> gameServerInformation1.getCloudType().getType().equals(CloudType.Type.BUNGEECORD))
                .findFirst().orElse(null);

        if (proxyServer == null) {
            return false;
        }

        for (String proxyFallbackPriority : proxyServer.getCloudType().getProxyFallbackPriorities()) {
            CloudType cloudType = this.getCloudTypeRepository().getCloudTypeByName(proxyFallbackPriority);

            if (proxyFallbackPriority == null) {
                continue;
            }

            fallbackServers.addAll(this.getGameServerRepository().getGameServersByCloudType(cloudType));
        }

        if (fallbackServers.size() == 0) {
            return false;
        }

        fallbackServers = fallbackServers.stream()
                .filter(gameServerInformation1 -> !gameServerInformation1.getUniqueId().equals(gameServerInformation.getUniqueId())) // Should not be the same fallbackserver
                .filter(gameServerInformation1 -> gameServerInformation1.getState().equals(GameServerInformation.State.ONLINE)) // Should be online
                .filter(gameServerInformation1 -> {
                    String permission = gameServerInformation.getCloudType().getPermission();
                    if (permission == null || permission.equalsIgnoreCase("")) {
                        return true;
                    }

                    return sender.hasPermission(permission);
                }) // Should have permission to join it
                .collect(Collectors.toList());

        if (fallbackServers.size() == 0) {
            return false;
        }

        GameServerInformation fallbackServer = fallbackServers.get(0);
        CloudAPI.getInstance().sendToServer(player.getUniqueId(), fallbackServer.getName());
        return true;
    }
}
