package de.blu.connector.bungeecord.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.connector.bungeecord.BungeeConnectorService;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

@Singleton
@Getter
public class HubCommand extends Command {

    @Inject
    private BungeeConnectorService bungeeConnectorService;

    public HubCommand() {
        super("hub", "", "l", "lobby");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (player.getServer().getInfo().getName().toLowerCase().contains("lobby")) {
            //player.sendMessage(ChatColor.RED + "Du bist bereits auf der Lobby.");
            return;
        }

        ServerInfo fallbackServer = this.getBungeeConnectorService().getFallbackServer(player);
        if (fallbackServer == null) {
            return;
        }

        player.connect(fallbackServer);
    }
}