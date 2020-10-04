package de.blu.connector.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.config.FileRootConfig;
import de.blu.common.config.LogsConfig;
import de.blu.common.config.RedisConfig;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.loader.GameServerLoader;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.ServiceConnectorBroadcast;
import de.blu.common.service.ServiceKeepAlive;
import de.blu.common.service.StaticIdentifierStorage;
import de.blu.common.setup.HastebinLogsSetup;
import de.blu.common.storage.GameServerStorage;
import de.blu.connector.common.listener.PacketHandler;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import de.blu.connector.common.sender.ServerStartedSender;
import lombok.Getter;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public class ConnectorService {

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private StaticIdentifierStorage staticIdentifierStorage;

    @Inject
    private ServiceConnectorBroadcast serviceConnectorBroadcast;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private RedisConfig redisConfig;

    @Inject
    private ServiceKeepAlive serviceKeepAlive;

    @Inject
    private PacketHandler packetHandler;

    @Inject
    private ServerStartedSender serverStartedSender;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private CloudTypeConfigLoader cloudTypeConfigLoader;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private GameServerLoader gameServerLoader;

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private LogsConfig logsConfig;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    @Inject
    @Named("dataFolder")
    private File dataFolder;

    private GameServerInformation selfGameServerInformation;

    public void onEnable() {
        String serverName = System.getProperty("cloud-servername");
        String serverUniqueIdString = System.getProperty("cloud-serveruuid");
        String fileRootPath = System.getProperty("cloud-fileroot");
        File fileRoot = new File(fileRootPath);

        this.getFileRootConfig().setRootFileDirectory(fileRootPath);

        // Set ServiceName
        this.getSelfServiceInformation().setName("server-connector");
        this.getSelfServiceInformation().setIdentifier(UUID.fromString(serverUniqueIdString));

        // Connect to Redis
        File configFile = new File(new File(fileRoot, "Configs"), "redis.properties");
        if (!configFile.exists()) {
            System.exit(0);
            return;
        }

        this.getRedisConfig().load(configFile);

        this.getRedisConnection().init(this.getRedisConfig().getHost(), this.getRedisConfig().getPort(), this.getRedisConfig().getPassword());
        this.getRedisConnection().connect();
        if (!this.getRedisConnection().isConnected()) {
            System.out.println("Could not connect to Redis! Check your Credentials (~/redis.properties)");
            return;
        }

        System.out.println("Connected to Redis.");

        configFile = new File(new File(fileRoot, "Configs"), "logs.properties");
        if (configFile.exists()) {
            this.getLogsConfig().load(configFile);
        }

        this.getPacketHandler().registerAll();
        this.getServiceKeepAlive().init();

        try {
            // Load CloudTypes
            this.getCloudTypeConfigLoader().initDefaultConfig();

            // Create Template Directories if not exist
            for (CloudType cloudType : this.getCloudTypeRepository().getCloudTypes()) {
                File templateDirectory = new File(new File(fileRoot, "Templates"), cloudType.getName());
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

        GameServerInformation gameServerInformation = this.getGameServerStorage().getGameServer(serverName, UUID.fromString(serverUniqueIdString));
        gameServerInformation.setState(GameServerInformation.State.ONLINE);
        this.getGameServerLoader().loadAllServers();
        this.getGameServerStorage().saveGameServer(gameServerInformation);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ConnectorService.this.getServerStartedSender().sendServerStarted(gameServerInformation);
            }
        }, TimeUnit.SECONDS.toMillis(5));

        this.selfGameServerInformation = gameServerInformation;
        this.getSelfGameServerInformationProvider().setGameServerInformation(gameServerInformation);

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

    public void onDisable() {
        /*
        try {
            Thread.sleep(200);
            System.out.println("Shutting down ...");
            //some cleaning up code...
            this.getServiceConnectorBroadcast().broadcastDisconnect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
         */
    }
}
