package de.minimichecker.coordinator;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.minimichecker.common.command.CommandRegister;
import de.minimichecker.common.command.ConsoleInputReader;
import de.minimichecker.common.config.RedisConfig;
import de.minimichecker.common.database.redis.RedisConnection;
import de.minimichecker.common.logging.Logger;
import de.minimichecker.common.logging.LoggingInitializer;
import de.minimichecker.common.network.packet.packets.HelloWorldPacket;
import de.minimichecker.common.network.packet.repository.PacketListenerRepository;
import de.minimichecker.common.network.packet.sender.PacketSender;
import de.minimichecker.common.util.LibraryUtils;
import de.minimichecker.coordinator.module.ModuleSettings;
import lombok.Getter;

import java.io.File;
import java.net.URISyntaxException;

@Singleton
@Getter
public final class ServerCoordinator {
    public static void main(String[] args) {
        try {
            // Init Libraries
            LibraryUtils.createLibraryFolder(ServerCoordinator.getRootDirectory());
            LibraryUtils.loadLibraries();

            // Create Injector
            Injector injector = Guice.createInjector(new ModuleSettings());

            // Calling Injected Constructor of CloudNode and start the Node
            injector.getInstance(ServerCoordinator.class);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                System.out.println("Stopping in 3 Seconds...");
                Thread.sleep(3000);
                System.exit(0);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static File getRootDirectory() {
        File directory = null;

        String debugPath = "G:/JavaProjects/mini-network/server-coordinator/build/debugging/";
        boolean isDebug = java.lang.management.ManagementFactory.
                getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");

        if (isDebug) {
            directory = new File(debugPath);
        } else {
            try {
                directory = new File(ServerCoordinator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (!directory.isDirectory()) {
            if (!directory.mkdir()) {
                throw new NullPointerException("Couldn't create root directory!");
            }
        }

        return directory;
    }

    @Inject
    private LoggingInitializer loggingInitializer;

    @Inject
    private CommandRegister commandRegister;

    @Inject
    private ConsoleInputReader consoleInputReader;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private RedisConfig redisConfig;

    @Inject
    private PacketSender packetSender;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    private Logger logger;

    @Inject
    private ServerCoordinator(Injector injector) {
        // Inject global variables
        injector.injectMembers(this);

        // Set System Parameters
        System.setProperty("http.agent", "Chrome");

        // Init Logger
        this.getLoggingInitializer().init(new File(ServerCoordinator.getRootDirectory(), "logs"));
        this.logger = this.getLoggingInitializer().getLogger();

        // Initialize ConsoleInputReader
        this.getConsoleInputReader().init();

        // Connect to Redis
        File configFile = new File(ServerCoordinator.getRootDirectory(), "redis.properties");
        this.getRedisConfig().createIfNotExist(configFile);
        this.getRedisConfig().load(configFile);

        this.getRedisConnection().init(this.getRedisConfig().getHost(), this.getRedisConfig().getPort(), this.getRedisConfig().getPassword());
        this.getRedisConnection().connect();
        if (!this.getRedisConnection().isConnected()) {
            this.getLogger().error("Could not connect to Redis! Check your Credentials (~/redis.properties)");
            return;
        }

        this.getLogger().info("Connected to Redis.");

        HelloWorldPacket helloWorldPacket = new HelloWorldPacket();
        helloWorldPacket.setMessage("HelloWorld2");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            System.out.println("Incoming Packet (hadCallback=" + hadCallback + "): " + packet.toString());

            packet.sendCallback();
        }, "testChannel");

        this.getPacketSender().sendRequestPacket(helloWorldPacket, resultPacket -> {
            HelloWorldPacket packet = resultPacket;
            System.out.println("Packet came back! " + packet.toString());
        }, "testChannel");
    }
}
