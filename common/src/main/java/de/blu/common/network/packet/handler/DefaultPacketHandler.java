package de.blu.common.network.packet.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.converter.GameServerJsonConverter;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.network.packet.packets.GameServerUpdatePacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.sender.GameServerUpdatedSender;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.storage.GameServerStorage;
import de.blu.common.util.AddressResolver;
import lombok.Getter;

@Singleton
@Getter
public class DefaultPacketHandler {

    @Inject
    private PacketSender packetSender;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private GameServerJsonConverter gameServerJsonConverter;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private AddressResolver addressResolver;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerUpdatedSender gameServerUpdatedSender;

    public void registerAll() {
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceDisconnectedPacket serviceDisconnectedPacket = (ServiceDisconnectedPacket) packet;
            System.out.println("&cService disconnected: " + serviceDisconnectedPacket.getServiceInformation().getName() + " (" + serviceDisconnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().removeService(serviceDisconnectedPacket.getServiceInformation().getIdentifier());
        }, "ServiceDisconnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            GameServerUpdatePacket gameServerUpdatePacket = (GameServerUpdatePacket) packet;

            GameServerInformation gameServerInformation = this.getGameServerRepository().getGameServerByUniqueId(gameServerUpdatePacket.getGameServerUniqueId());
            this.getGameServerRepository().getGameServers().remove(gameServerInformation);
            this.getGameServerRepository().getGameServers().add(gameServerUpdatePacket.getGameServerInformation());
        }, "GameServerUpdated");
    }
}
