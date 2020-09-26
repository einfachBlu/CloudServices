package de.blu.connector.common.sender;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.GameServerUpdatePacket;
import de.blu.common.network.packet.packets.ServerStartedPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import lombok.Getter;

@Singleton
@Getter
public final class GameServerUpdateSender {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    public void sendServerUpdated() {
        GameServerInformation gameServerInformation = this.getSelfGameServerInformationProvider().getGameServerInformation();
        GameServerUpdatePacket gameServerUpdatePacket = this.getInjector().getInstance(GameServerUpdatePacket.class);
        gameServerUpdatePacket.setGameServerName(gameServerInformation.getName());
        gameServerUpdatePacket.setGameServerUniqueId(gameServerInformation.getUniqueId());

        this.getPacketSender().sendPacket(gameServerUpdatePacket, "GameServerUpdated");
    }
}
