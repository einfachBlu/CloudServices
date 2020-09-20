package de.blu.common.command.data;

import lombok.Getter;

@Getter
public final class CommandData {
    private String name;
    private String[] aliases;
    private CommandExecutor commandExecutor;

    public CommandData(String name, String[] aliases, CommandExecutor commandExecutor) {
        this.name = name;
        this.aliases = aliases;
        this.commandExecutor = commandExecutor;
    }
}
