package de.blu.coordinator.request;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.RequestGameServerStartPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.storage.GameServerStorage;
import de.blu.coordinator.server.GameServerFactory;
import de.blu.coordinator.server.ServerStarterReceiver;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
@Getter
public final class ServerStartRequester {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private ServerStarterReceiver serverStarterReceiver;

    @Inject
    private GameServerFactory gameServerFactory;

    public void requestGameServerStart(GameServerInformation gameServerInformation, Consumer<GameServerInformation> callback) {
        RequestGameServerStartPacket requestGameServerStartPacket = this.getInjector().getInstance(RequestGameServerStartPacket.class);
        requestGameServerStartPacket.setGameServerUniqueId(gameServerInformation.getUniqueId());
        requestGameServerStartPacket.setGameServerName(gameServerInformation.getName());

        // Request GameServerStart
        this.getPacketSender().sendRequestPacket(requestGameServerStartPacket, requestGameServerStartPacket1 -> {
            GameServerInformation gameServerInformation1 = requestGameServerStartPacket1.getGameServerInformation();

            if (gameServerInformation1.getState().equals(GameServerInformation.State.OFFLINE)) {
                System.out.println("&cCould not start GameServer " + gameServerInformation1.getName() + ":");
                System.out.println("&c" + requestGameServerStartPacket1.getErrorMessage());
                this.getGameServerRepository().getGameServers().remove(gameServerInformation);
                this.getGameServerStorage().removeGameServer(gameServerInformation);
                callback.accept(null);
            } else {
                // Add the new object to the repository to update values on fields
                this.getGameServerRepository().getGameServers().remove(gameServerInformation);
                this.getGameServerRepository().getGameServers().add(gameServerInformation1);
                callback.accept(gameServerInformation1);
            }
        }, gameServerInformation.getServerStarterInformation().getIdentifier().toString());
    }

    public void requestGameServerStart(CloudType cloudType, boolean manually) {
        this.requestGameServerStart(cloudType, manually, new HashMap<>());
    }

    public void requestGameServerStart(CloudType cloudType, boolean manually, Map<String, String> meta) {
        this.requestGameServerStart(cloudType, manually, meta, requestGameServerStartPacket -> {
        });
    }

    public void requestGameServerStart(CloudType cloudType, boolean manually, Consumer<GameServerInformation> callback) {
        this.requestGameServerStart(cloudType, manually, new HashMap<>(), callback);
    }

    public void requestGameServerStart(CloudType cloudType, boolean manually, Map<String, String> meta, Consumer<GameServerInformation> callback) {
        try {
            if (manually) {
                int currentOnlineAmount = this.getGameServerRepository().getGameServersByCloudType(cloudType).size();
                if (cloudType.isStaticService() && currentOnlineAmount >= 1) {
                    // Allow only 1 server at once running for static Service
                    System.out.println("&cCloudType is static and is already started");
                    callback.accept(null);
                    return;
                }
            }

            System.out.println("Try to Start Server from " + cloudType.getName());

            // Get best ServerStarter Service
            this.getServerStarterReceiver().getBestServerStarter(cloudType, bestServerStarter -> {
                if (bestServerStarter == null) {
                    callback.accept(null);
                    return;
                }

                GameServerInformation gameServerInformation = this.getGameServerFactory().create(cloudType, manually, meta, bestServerStarter);
                if (gameServerInformation == null) {
                    callback.accept(null);
                    return;
                }

                System.out.println("ServerStart Request for &e" + gameServerInformation.getName() +
                        "&r to ServerStarter &e" + bestServerStarter.getIdentifier().toString());

                this.requestGameServerStart(gameServerInformation, callback);
            });
        } catch (Exception e) {
            e.printStackTrace();
            callback.accept(null);
        }
    }
}
