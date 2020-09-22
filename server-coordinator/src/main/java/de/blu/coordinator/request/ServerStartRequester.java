package de.blu.coordinator.request;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.RequestGameServerStartPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;

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

    public void requestGameServerStart(GameServerInformation gameServerInformation) {
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
            } else {
                // Add the new object to the repository to update values on fields
                this.getGameServerRepository().getGameServers().remove(gameServerInformation);
                this.getGameServerRepository().getGameServers().add(gameServerInformation1);
            }
        }, gameServerInformation.getServerStarterInformation().getIdentifier().toString());
    }
}
