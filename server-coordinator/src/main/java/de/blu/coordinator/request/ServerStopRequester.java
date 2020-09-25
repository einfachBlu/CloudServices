package de.blu.coordinator.request;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.RequestGameServerStopPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;

@Singleton
@Getter
public final class ServerStopRequester {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerStorage gameServerStorage;

    public void requestGameServerStop(GameServerInformation gameServerInformation) {
        RequestGameServerStopPacket requestGameServerStopPacket = this.getInjector().getInstance(RequestGameServerStopPacket.class);
        requestGameServerStopPacket.setGameServerUniqueId(gameServerInformation.getUniqueId());
        requestGameServerStopPacket.setGameServerName(gameServerInformation.getName());

        // Request GameServerStop
        this.getPacketSender().sendPacket(requestGameServerStopPacket, gameServerInformation.getServerStarterInformation().getIdentifier().toString());
    }
}
