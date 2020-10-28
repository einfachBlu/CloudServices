package de.blu.connector.bukkit.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.storage.LogsStorage;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

@Singleton
@Getter
public final class LogBukkitCommand implements CommandExecutor {

    @Inject
    private LogsStorage logsStorage;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        GameServerInformation gameServerInformation = this.getSelfGameServerInformationProvider().getGameServerInformation();
        File logFile = new File(gameServerInformation.getTemporaryPath() + "/logs/latest.log");

        int lines = -1;
        if (args.length > 0) {
            try {
                lines = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
            }
        }

        String url = this.getLogsStorage().postUrl(gameServerInformation.getUniqueId(), logFile, lines);
        if (!(sender instanceof Player)) {
            sender.sendMessage("§7Log of " + gameServerInformation.getName() + ": §6" + url);
            return true;
        }

        Player player = (Player) sender;

        TextComponent textComponent = new TextComponent("§7Log of " + gameServerInformation.getName() + ": ");
        TextComponent textComponent1 = new TextComponent("§6" + url);

        textComponent1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        player.spigot().sendMessage(textComponent, textComponent1);
        return true;
    }
}
