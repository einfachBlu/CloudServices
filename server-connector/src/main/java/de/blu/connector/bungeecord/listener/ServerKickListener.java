package de.blu.connector.bungeecord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.connector.bungeecord.BungeeConnectorService;
import lombok.Getter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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

        /*
        System.out.println("DEBUG ServerKickEvent for Player " + player.getName());
        System.out.println("kickedFrom= " + (kickedFrom == null ? "null" : kickedFrom.getName()));
        System.out.println("State= " + e.getState());
         */

        if (kickedFrom != null && this.getBungeeConnectorService().isFallbackServer(kickedFrom) && e.getState().equals(ServerKickEvent.State.CONNECTING)) {
            e.setCancelled(true);
            e.setCancelServer(kickedFrom);
            player.sendMessage(e.getKickReasonComponent());
            return;
        }

        ServerInfo moveTo = this.getBungeeConnectorService().getFallbackServer(player);
        if (moveTo == null) {
            if (kickedFrom != null) {
                e.setKickReasonComponent(new BaseComponent[]{new TextComponent(e.getKickReasonComponent())});
                return;
            }

            e.setKickReasonComponent(new BaseComponent[]{new TextComponent("No Fallback Server available")});
            return;
        }

        if (kickedFrom == null) {
            return;
        }

        e.setCancelled(true);
        e.setCancelServer(moveTo);
        player.sendMessage(e.getKickReasonComponent());
    }
}
