package de.blu.connector.bungeecord.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.storage.LogsStorage;
import de.blu.connector.bungeecord.BungeeConnectorService;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

@Singleton
@Getter
public class LogCommand extends Command {

    @Inject
    private BungeeConnectorService bungeeConnectorService;

    @Inject
    private LogsStorage logsStorage;

    public LogCommand() {
        super("log", "command.log");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§eUsage: /log <ServerUniqueId>");
            return;
        }

        String uniqueIdString = args[0];

        try {
            String url = this.getLogsStorage().getLogUrl(UUID.fromString(uniqueIdString));

            if (url.equalsIgnoreCase("")) {
                sender.sendMessage("§cServer has no log yet. Maybe its still running.");
                return;
            }

            sender.sendMessage("§7Log of " + uniqueIdString + ": §6" + url);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage("§cInvalid UUID Format.");
            return;
        }
    }
}