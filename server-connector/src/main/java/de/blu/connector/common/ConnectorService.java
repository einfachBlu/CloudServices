package de.blu.connector.common;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import de.blu.common.config.RedisConfig;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.request.CloudTypeRequester;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.ServiceConnectorBroadcast;
import de.blu.common.service.ServiceKeepAlive;
import de.blu.common.service.StaticIdentifierStorage;
import de.blu.common.storage.GameServerStorage;
import de.blu.connector.common.listener.PacketHandler;
import de.blu.connector.common.sender.ServerStartedSender;
import lombok.Getter;

import java.io.File;
import java.util.UUID;

@Singleton
@Getter
public final class ConnectorService {

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
    private CloudTypeRequester cloudTypeRequester;

    @Inject
    @Named("dataFolder")
    private File dataFolder;

    public void onEnable() {
        String serverName = System.getProperty("cloud-servername");
        String serverUniqueIdString = System.getProperty("cloud-serveruuid");
        String fileRootPath = System.getProperty("cloud-fileroot");
        File fileRoot = new File(fileRootPath);

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

        this.getPacketHandler().registerAll();
        this.getServiceKeepAlive().init();

        this.getCloudTypeRequester().requestCloudTypes(aVoid -> {
            GameServerInformation gameServerInformation = this.getGameServerStorage().getGameServer(serverName, UUID.fromString(serverUniqueIdString));
            gameServerInformation.setState(GameServerInformation.State.ONLINE);
            this.getGameServerStorage().saveGameServer(gameServerInformation);

            this.getServerStartedSender().sendServerStarted(gameServerInformation);
        });

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
