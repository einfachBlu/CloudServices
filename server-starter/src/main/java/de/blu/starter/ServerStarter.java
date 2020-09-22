package de.blu.starter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.command.CommandRegister;
import de.blu.common.command.ConsoleInputReader;
import de.blu.common.config.FileRootConfig;
import de.blu.common.config.RedisConfig;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.logging.Logger;
import de.blu.common.logging.LoggingInitializer;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.ServiceConnectorBroadcast;
import de.blu.common.service.ServiceKeepAlive;
import de.blu.common.setup.FileRootSetup;
import de.blu.common.setup.RedisCredentialsSetup;
import de.blu.common.util.LibraryUtils;
import de.blu.starter.listener.PacketHandler;
import de.blu.starter.module.ModuleSettings;
import de.blu.starter.request.CloudTypeRequester;
import de.blu.starter.template.TemporaryDirectoryRemover;
import lombok.Getter;

import java.io.File;
import java.net.URISyntaxException;

@Singleton
@Getter
public final class ServerStarter {
    public static void main(String[] args) {
        try {
            // Init Libraries
            LibraryUtils.createLibraryFolder(ServerStarter.getRootDirectory());
            LibraryUtils.loadLibraries();

            // Create Injector
            Injector injector = Guice.createInjector(new ModuleSettings());

            // Calling Injected Constructor of CloudNode and start the Node
            injector.getInstance(ServerStarter.class);
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

        String debugPath = "G:/JavaProjects/cloud-services/server-coordinator/build/debugging/";
        boolean isDebug = java.lang.management.ManagementFactory.
                getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");

        if (isDebug) {
            directory = new File(debugPath);
        } else {
            try {
                directory = new File(ServerStarter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
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

    @Inject
    private RedisCredentialsSetup redisCredentialsSetup;

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private FileRootSetup fileRootSetup;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private PacketHandler packetHandler;

    @Inject
    private ServiceConnectorBroadcast serviceConnectorBroadcast;

    @Inject
    private ServiceKeepAlive serviceKeepAlive;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private CloudTypeRequester cloudTypeRequester;

    @Inject
    private TemporaryDirectoryRemover temporaryDirectoryRemover;

    private Logger logger;

    @Inject
    private ServerStarter(Injector injector) {
        // Inject global variables
        injector.injectMembers(this);

        // Set System Parameters
        System.setProperty("http.agent", "Chrome");

        // Set ServiceName
        this.getSelfServiceInformation().setName("server-starter");

        // Init Logger
        this.getLoggingInitializer().init(new File(ServerStarter.getRootDirectory(), "logs"));
        this.logger = this.getLoggingInitializer().getLogger();

        this.getLogger().info("Starting ServerStarter...");

        // Initialize ConsoleInputReader
        this.getConsoleInputReader().init();

        // Check for RootDirectory
        File configFile = new File(ServerStarter.getRootDirectory(), "rootdir.properties");
        if (!configFile.exists()) {
            this.getFileRootSetup().startSetup(configFile);
        }

        this.getFileRootConfig().load(configFile);

        // Connect to Redis
        File fileRootDirectory = new File(this.getFileRootConfig().getRootFileDirectory());
        configFile = new File(new File(fileRootDirectory, "Configs"), "redis.properties");
        if (!configFile.exists()) {
            this.getRedisCredentialsSetup().startSetup(configFile);
        }

        this.getRedisConfig().load(configFile);

        this.getRedisConnection().init(this.getRedisConfig().getHost(), this.getRedisConfig().getPort(), this.getRedisConfig().getPassword());
        this.getRedisConnection().connect();
        if (!this.getRedisConnection().isConnected()) {
            this.getLogger().error("Could not connect to Redis! Check your Credentials (~/redis.properties)");
            return;
        }

        this.getLogger().info("Connected to Redis.");

        this.getPacketHandler().registerAll();
        this.getServiceKeepAlive().init();

        //this.getCommandRegister().registerRecursive("de.blu.starter.command");

        this.getCloudTypeRequester().requestCloudTypes();

        this.getTemporaryDirectoryRemover().startTimer();

        this.getLogger().info("ServerStarter is now started.");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(200);
                System.out.println("Shutting down ...");
                //some cleaning up code...
                this.getServiceConnectorBroadcast().broadcastDisconnect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }));
    }
}
