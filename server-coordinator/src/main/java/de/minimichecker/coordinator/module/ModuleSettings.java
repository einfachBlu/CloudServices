package de.minimichecker.coordinator.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import de.minimichecker.common.command.ConsoleCommandHandler;
import de.minimichecker.common.command.ConsoleInputReader;
import de.minimichecker.common.database.redis.RedisConnection;
import de.minimichecker.common.database.redis.RedisConnectionProvider;
import de.minimichecker.common.logging.ConsoleAndFileLogger;
import de.minimichecker.common.logging.Logger;
import de.minimichecker.coordinator.ServerCoordinator;
import jline.console.ConsoleReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ModuleSettings extends AbstractModule {

    @Override
    protected void configure() {
        // Binding
        bind(Logger.class).to(ConsoleAndFileLogger.class);
        bind(ConsoleInputReader.class).to(ConsoleCommandHandler.class);
        bind(RedisConnection.class).to(RedisConnectionProvider.class);
        bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
        bind(File.class).annotatedWith(Names.named("rootdir")).toInstance(ServerCoordinator.getRootDirectory());

        // Override Output & Error Stream
        System.setOut(new PrintStream(System.out) {
            @Override
            public void println(String x) {
                Logger.getGlobal().info(x);
            }
        });
        System.setErr(new PrintStream(System.err) {
            @Override
            public void print(String x) {
                if (x.equalsIgnoreCase(Logger.COLOR_ERROR_START) || x.equalsIgnoreCase(Logger.COLOR_ERROR_END)) {
                    super.print(x);
                    return;
                }

                System.err.print(Logger.COLOR_ERROR_START);
                super.print(x);
                //Logger.getGlobal().error(x);
                System.err.print(Logger.COLOR_ERROR_END);
            }
        });

        // Binding ConsoleReader
        try {
            ConsoleReader reader = new ConsoleReader(System.in, System.out);
            reader.setExpandEvents(false);
            reader.setPrompt("> ");
            bind(ConsoleReader.class).toInstance(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
