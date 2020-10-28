package de.blu.connector.common.sender;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.network.packet.packets.RequestGameServerStartPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;

import java.util.Map;
import java.util.function.Consumer;

@Singleton
@Getter
public final class GameServerStartRequester {

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    @Inject
    private GameServerStorage gameServerStorage;

    public void request(CloudType cloudType, Map<String, String> meta, Consumer<RequestGameServerStartPacket> callback) {
        RequestGameServerStartPacket requestGameServerStartPacket = this.getInjector().getInstance(RequestGameServerStartPacket.class);
        requestGameServerStartPacket.setCloudType(cloudType);
        requestGameServerStartPacket.setMeta(meta);

        this.getPacketSender().sendRequestPacket(requestGameServerStartPacket, callback, "RequestCoordinatorStartGameServer");
    }
}
