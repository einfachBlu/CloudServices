package de.blu.connector.bungeecord.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.storage.LogsStorage;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.io.File;

@Singleton
@Getter
public class LogBungeeCommand extends Command {

    @Inject
    private LogsStorage logsStorage;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    public LogBungeeCommand() {
        super("logbungee", "command.logbungee");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        GameServerInformation gameServerInformation = this.getSelfGameServerInformationProvider().getGameServerInformation();
        File logFile = new File(gameServerInformation.getTemporaryPath() + "/logs/proxy.log.0");

        int lines = -1;
        if (args.length > 0) {
            try {
                lines = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
            }
        }

        String url = this.getLogsStorage().postUrl(gameServerInformation.getUniqueId(), logFile, lines);
        TextComponent textComponent = new TextComponent("§7Log of " + gameServerInformation.getName() + ": ");
        TextComponent textComponent1 = new TextComponent("§6" + url);

        textComponent1.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

        sender.sendMessage(textComponent, textComponent1);
    }
}