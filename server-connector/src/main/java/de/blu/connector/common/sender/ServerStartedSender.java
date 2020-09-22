package de.blu.connector.common.sender;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.ServerStartedPacket;
import de.blu.common.network.packet.packets.ServerStoppedPacket;
import de.blu.common.network.packet.sender.PacketSender;
import lombok.Getter;

@Singleton
@Getter
public final class ServerStartedSender {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    public void sendServerStarted(GameServerInformation gameServerInformation) {
        ServerStartedPacket serverStartedPacket = this.getInjector().getInstance(ServerStartedPacket.class);
        serverStartedPacket.setGameServerName(gameServerInformation.getName());
        serverStartedPacket.setGameServerUniqueId(gameServerInformation.getUniqueId());

        this.getPacketSender().sendPacket(serverStartedPacket, "ServerStarted");
    }
}
