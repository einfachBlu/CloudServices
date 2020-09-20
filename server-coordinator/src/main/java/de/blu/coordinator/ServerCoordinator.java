package de.blu.coordinator;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.command.CommandRegister;
import de.blu.common.command.ConsoleInputReader;
import de.blu.common.config.FileRootConfig;
import de.blu.common.config.RedisConfig;
import de.blu.common.data.CloudType;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.logging.Logger;
import de.blu.common.logging.LoggingInitializer;
import de.blu.common.network.packet.packets.RequestCloudTypesPacket;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.setup.FileRootSetup;
import de.blu.common.setup.RedisCredentialsSetup;
import de.blu.common.util.LibraryUtils;
import de.blu.coordinator.module.ModuleSettings;
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

        String debugPath = "G:/JavaProjects/cloud-services/server-coordinator/build/debugging/";
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

    @Inject
    private RedisCredentialsSetup redisCredentialsSetup;

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private FileRootSetup fileRootSetup;

    @Inject
    private CloudTypeConfigLoader cloudTypeConfigLoader;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

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

        this.getLogger().info("Starting ServerCoordinator...");

        // Initialize ConsoleInputReader
        this.getConsoleInputReader().init();

        // Check for RootDirectory
        File configFile = new File(ServerCoordinator.getRootDirectory(), "rootdir.properties");
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

        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((channel, message) -> {
        }, "CallbackChannel");

        try {
            // Load CloudTypes
            this.getCloudTypeConfigLoader().initDefaultConfig();

            // Create Template Directories if not exist
            for (CloudType cloudType : this.getCloudTypeRepository().getCloudTypes()) {
                File templateDirectory = new File(this.getFileRootConfig().getRootFileDirectory() + "Templates", cloudType.getName());
                if (cloudType.getTemplatePath() != null && !cloudType.getTemplatePath().equalsIgnoreCase("") && !cloudType.getTemplatePath().equalsIgnoreCase("null")) {
                    templateDirectory = new File(cloudType.getTemplatePath());
                }

                templateDirectory.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return;
        }

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof RequestCloudTypesPacket) {
                ((RequestCloudTypesPacket) packet).setCloudTypes(this.getCloudTypeRepository().getCloudTypes());
                packet.sendBack();
            }
        }, "RequestCloudTypes");

        this.getLogger().info("ServerCoordinator is now started.");

        /*
        System.out.println("CloudTypes in Repository: " + Arrays.toString(this.getCloudTypeRepository().getCloudTypes().toArray()));

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            System.out.println("Incoming Packet: " + packet.getClass().getSimpleName());
            if (hadCallback) {
                return;
            }

            System.out.println("Sending back...");
            packet.sendBack();
        }, "RequestCloudTypes");

        RequestCloudTypesPacket requestCloudTypesPacket = injector.getInstance(RequestCloudTypesPacket.class);
        this.getPacketSender().sendRequestPacket(requestCloudTypesPacket, requestCloudTypesPacket1 -> {
            System.out.println("Packet came back!");
            System.out.println("CloudTypes received: " + Arrays.toString(requestCloudTypesPacket.getCloudTypes().toArray()));
        }, "RequestCloudTypes");

        try {
            this.getLogger().info("Hostname=" + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

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
        */
    }
}