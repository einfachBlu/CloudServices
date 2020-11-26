package de.blu.webpanel;

import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.config.FileRootConfig;
import de.blu.common.config.RedisConfig;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.database.redis.RedisConnectionProvider;
import de.blu.common.loader.GameServerLoader;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.ServiceConnectorBroadcast;
import de.blu.common.service.ServiceKeepAlive;
import de.blu.common.service.StaticIdentifierStorage;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;

import javax.inject.Inject;
import java.io.File;
import java.util.UUID;

@Singleton
@Getter
public class WebPanelService {

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
    @Named("dataFolder")
    private File dataFolder;

    private GameServerInformation selfGameServerInformation;

    public void init() {
        File fileRoot = new File("/home/sync/MiniNetwork-Dev/");

        // Set ServiceName
        /*
        this.getSelfServiceInformation().setName("web-panel");
        this.getSelfServiceInformation().setIdentifier(UUID.randomUUID());

        // Connect to Redis
        /*
        File configFile = new File(new File(fileRoot, "Configs"), "redis.properties");
        if (!configFile.exists()) {
            System.exit(0);
            return;
        }
         */

        //this.getRedisConfig().load(configFile);
        this.redisConfig = new RedisConfig();
        this.getRedisConfig().setHost("179.61.251.16");
        this.getRedisConfig().setPort(6379);
        this.getRedisConfig().setPassword("iA4x7G9aEinbVJNImUtL");

        this.redisConnection = new RedisConnectionProvider();
        this.getRedisConnection().init(this.getRedisConfig().getHost(), this.getRedisConfig().getPort(), this.getRedisConfig().getPassword());
        this.getRedisConnection().connect();
        if (!this.getRedisConnection().isConnected()) {
            System.out.println("Could not connect to Redis! Check your Credentials (~/redis.properties)");
            return;
        }

        System.out.println("Connected to Redis.");

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
