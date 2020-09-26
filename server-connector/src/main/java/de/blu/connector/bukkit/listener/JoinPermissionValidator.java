package de.blu.connector.bukkit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

@Singleton
@Getter
public final class JoinPermissionValidator implements Listener {

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        Player player = e.getPlayer();

        GameServerInformation gameServerInformation = this.getSelfGameServerInformationProvider().getGameServerInformation();
        String permission = gameServerInformation.getCloudType().getPermission();
        if (permission == null || permission.equalsIgnoreCase("")) {
            // No Permission
            return;
        }

        if (player.hasPermission(permission)) {
            // Has Permission to join this CloudType
            return;
        }

        // Prevent Join
        e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        e.setKickMessage(ChatColor.RED + "You are not allowed to join this Server.");
    }
}
