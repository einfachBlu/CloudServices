package de.blu.common.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.data.CommandData;
import de.blu.common.command.data.CommandExecutor;
import de.blu.common.logging.Logger;
import jline.console.ConsoleReader;
import lombok.Getter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Singleton
public final class ConsoleCommandHandler implements ConsoleInputReader {

    private static final String COMMAND_NOT_FOUND = "Command {0} was not found!";

    @Inject
    @Getter
    private ExecutorService threadPool;

    @Inject
    @Getter
    private ConsoleReader consoleReader;

    @Inject
    @Getter
    private Logger logger;

    @Getter
    private Collection<CommandData> commands = new HashSet<>();

    // Both Variables will be set when the readLine Method was called
    // customLine is the return value
    // customWaiting will set to true if someone needs a custom input
    // and to false if it was received
    private String customLine = null;
    private boolean customWaiting = false;

    @Override
    public void init() {
        this.getThreadPool().execute(() -> {
            Thread.currentThread().setName("ConsoleInput-Thread");

            try {
                String input;
                while ((input = this.getConsoleReader().readLine()) != null) {
                    if (this.customWaiting) {
                        this.customLine = input;
                        continue;
                    }

                    if (input.equalsIgnoreCase("")) {
                        continue;
                    }

                    String line = input;
                    this.getThreadPool().execute(() -> this.handleCommandLine(line));
                }
            } catch (Exception e) {
                Logger.getGlobal().error(e.getMessage(), e);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Remove Prompt on Shutdown
            try {
                this.getConsoleReader().resetPromptLine("", "", 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public String readLine() {
        this.customWaiting = true;
        try {
            while (this.customLine == null) {
                // Wait while no customLine was set
                Thread.sleep(50);
            }

            String line = this.customLine;
            this.customWaiting = false;
            this.customLine = null;
            return line;
        } catch (Exception e) {
            Logger.getGlobal().error(e.getMessage(), e);
        }

        this.customWaiting = false;
        this.customLine = null;
        return null;
    }

    @Override
    public void readLine(Consumer<String> lineCallback) {
        this.customWaiting = true;
        this.getThreadPool().execute(() -> {
            try {
                while (this.customLine == null) {
                    // Wait while no customLine was set
                    Thread.sleep(50);
                }

                lineCallback.accept(this.customLine);
                this.customWaiting = false;
                this.customLine = null;
            } catch (Exception e) {
                Logger.getGlobal().error(e.getMessage(), e);
            }

            this.customWaiting = false;
            this.customLine = null;
        });
    }

    @Override
    public void dispatch(String line) {
        this.handleCommandLine(line);
    }

    private void handleCommandLine(String line) {
        String name = line.split(" ")[0];
        CommandData commandData = this.getCommandByName(name);
        if (commandData == null) {
            this.getLogger().warning(COMMAND_NOT_FOUND.replace("{0}", name));
            return;
        }

        String[] args;
        if (line.split(" ").length == 1) {
            args = new String[0];
        } else {
            args = Arrays.copyOfRange(line.split(" "), 1, line.split(" ").length);
        }

        commandData.getCommandExecutor().execute(name, args);
    }

    public void registerCommand(String name, String[] aliases, CommandExecutor commandExecutor) {
        CommandData commandData = new CommandData(name, aliases, commandExecutor);
        this.getCommands().add(commandData);

        this.getLogger().info("Registered Command &e" + name + "&r with Aliases=&e" + Arrays.toString(aliases));
    }

    public CommandData getCommandByName(String name) {
        for (CommandData command : getCommands()) {
            if (command.getName().equalsIgnoreCase(name)) {
                return command;
            }

            for (String alias : command.getAliases()) {
                if (alias.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }

        return null;
    }
}
