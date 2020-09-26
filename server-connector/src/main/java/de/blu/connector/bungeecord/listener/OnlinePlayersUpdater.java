package de.blu.connector.bungeecord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.connector.common.handler.GameServerUpdater;
import lombok.Getter;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@Singleton
@Getter
public final class OnlinePlayersUpdater implements Listener {

    @Inject
    private GameServerUpdater gameServerUpdater;

    @EventHandler(priority = EventPriority.HIGHEST + 10)
    public void onJoin(PostLoginEvent e) {
        this.getGameServerUpdater().increaseOnlinePlayers();
    }

    @EventHandler(priority = EventPriority.HIGHEST + 10)
    public void onLeave(PlayerDisconnectEvent e) {
        this.getGameServerUpdater().decreaseOnlinePlayers();
    }
}
