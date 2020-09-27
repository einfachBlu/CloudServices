package de.blu.connector.common.sender;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.PlayerSendToServerPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.connector.common.provider.SelfGameServerInformationProvider;
import lombok.Getter;

import java.util.UUID;
import java.util.function.Consumer;

@Singleton
@Getter
public final class PlayerServerSender {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    @Inject
    private SelfGameServerInformationProvider selfGameServerInformationProvider;

    public void sendToServer(UUID player, String serverName, Consumer<Void> doneCallback) {
        PlayerSendToServerPacket playerSendToServerPacket = this.getInjector().getInstance(PlayerSendToServerPacket.class);
        playerSendToServerPacket.setPlayer(player);
        playerSendToServerPacket.setServerName(serverName);

        this.getPacketSender().sendPacket(playerSendToServerPacket, doneCallback, "PlayerSendToServer");
    }
}
