package de.blu.starter.command;

import com.google.inject.Singleton;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import lombok.Getter;

@Singleton
@Command(name = "shutdown", aliases = "quit")
@Getter
public final class ShutdownCommand extends CommandExecutor {

    @Override
    public void execute(String label, String[] args) {
        System.exit(0);
    }
}
