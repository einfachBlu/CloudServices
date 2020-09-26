package de.blu.connector.bukkit.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.connector.common.handler.GameServerUpdater;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Singleton
@Getter
public final class OnlinePlayersUpdater implements Listener {

    @Inject
    private GameServerUpdater gameServerUpdater;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        this.getGameServerUpdater().increaseOnlinePlayers();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        this.getGameServerUpdater().decreaseOnlinePlayers();
    }
}
