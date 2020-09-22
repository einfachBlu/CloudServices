package de.blu.starter.sender;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.packets.ServerStoppedPacket;
import de.blu.common.network.packet.sender.PacketSender;
import lombok.Getter;

@Singleton
@Getter
public final class ServerStoppedSender {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    public void sendServerStopped(GameServerInformation gameServerInformation) {
        ServerStoppedPacket serverStoppedPacket = this.getInjector().getInstance(ServerStoppedPacket.class);
        serverStoppedPacket.setGameServerName(gameServerInformation.getName());
        serverStoppedPacket.setGameServerUniqueId(gameServerInformation.getUniqueId());

        this.getPacketSender().sendPacket(serverStoppedPacket, "ServerStopped");
    }
}
