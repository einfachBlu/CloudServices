package de.blu.connector.bungeecord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.connector.bungeecord.BungeeConnectorService;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@Singleton
@Getter
public final class ServerKickListener implements Listener {

    @Inject
    private BungeeConnectorService bungeeConnectorService;

    @EventHandler
    public void onKick(ServerKickEvent e) {
        ProxiedPlayer player = e.getPlayer();

        ServerInfo kickedFrom = null;
        if (player.getServer() != null) {
            kickedFrom = player.getServer().getInfo();
        }

        if (kickedFrom == null) {
            return;
        }

        ServerInfo moveTo = this.getBungeeConnectorService().getFallbackServer(player);
        if (moveTo == null) {
            e.setKickReasonComponent(new BaseComponent[]{new TextComponent("No Fallback Server available")});
            return;
        }

        e.setCancelled(true);
        e.setCancelServer(moveTo);
        player.sendMessage(e.getKickReasonComponent());
    }
}
