package de.blu.common.sender;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.GameServerUpdatePacket;
import de.blu.common.network.packet.packets.ServerStoppedPacket;
import de.blu.common.network.packet.sender.PacketSender;
import lombok.Getter;

@Singleton
@Getter
public final class GameServerUpdatedSender {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    public void sendUpdated(GameServerInformation gameServerInformation) {
        GameServerUpdatePacket gameServerUpdatePacket = this.getInjector().getInstance(GameServerUpdatePacket.class);
        gameServerUpdatePacket.setGameServerName(gameServerInformation.getName());
        gameServerUpdatePacket.setGameServerUniqueId(gameServerInformation.getUniqueId());

        this.getPacketSender().sendPacket(gameServerUpdatePacket, "GameServerUpdated");
    }
}
