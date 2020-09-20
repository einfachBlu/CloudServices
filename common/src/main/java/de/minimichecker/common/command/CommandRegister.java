package de.minimichecker.common.command;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.minimichecker.common.command.data.Command;
import de.minimichecker.common.command.data.CommandExecutor;
import lombok.Getter;
import org.reflections.Reflections;

@Singleton
public final class CommandRegister {

    @Getter
    private Injector injector;

    @Getter
    private ConsoleCommandHandler consoleCommandHandler;

    @Inject
    private CommandRegister(Injector injector, ConsoleCommandHandler consoleCommandHandler) {
        this.injector = injector;
        this.consoleCommandHandler = consoleCommandHandler;
    }

    public void registerRecursive(String packageName) {
        Reflections reflections = new Reflections(packageName);
        for (Class<?> commandClass : reflections.getTypesAnnotatedWith(Command.class)) {
            if (commandClass.getSuperclass() != CommandExecutor.class) {
                continue;
            }

            try {
                CommandExecutor commandExecutor = (CommandExecutor) commandClass.getDeclaredConstructor().newInstance();
                this.getInjector().injectMembers(commandExecutor);

                Command command = commandClass.getAnnotation(Command.class);
                String name = command.name();
                String[] aliases = command.aliases();

                this.getConsoleCommandHandler().registerCommand(name, aliases, commandExecutor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
