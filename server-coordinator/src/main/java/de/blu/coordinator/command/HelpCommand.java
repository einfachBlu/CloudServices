package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.CommandRegister;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandData;
import de.blu.common.command.data.CommandExecutor;
import lombok.Getter;

import java.util.Arrays;

@Singleton
@Command(name = "help", aliases = "?")
@Getter
public final class HelpCommand extends CommandExecutor {

    @Inject
    private CommandRegister commandRegister;

    @Override
    public void execute(String label, String[] args) {
        System.out.println("&bCommands:");

        for (CommandData command : this.getCommandRegister().getConsoleCommandHandler().getCommands()) {
            System.out.println("- &e" + command.getName() + " &r(Aliases: &e" + Arrays.toString(command.getAliases()) + "&r)");
        }
    }
}
