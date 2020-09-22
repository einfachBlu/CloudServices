package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import de.blu.coordinator.printer.GameServerPrinter;
import lombok.Getter;

@Singleton
@Command(name = "servers")
@Getter
public final class ServersCommand extends CommandExecutor {

    @Inject
    private GameServerPrinter gameServerPrinter;

    @Override
    public void execute(String label, String[] args) {
        this.getGameServerPrinter().printAll();
    }
}
