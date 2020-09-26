package de.blu.common.network.packet.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.network.packet.Packet;
import de.blu.common.network.packet.repository.PacketCallbackRepository;
import de.blu.common.service.SelfServiceInformation;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
@Getter
public final class PacketSender {

    @Inject
    private PacketWriter packetWriter;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private PacketCallbackRepository packetCallbackRepository;

    @Inject
    private RedisConnection redisConnection;

    public <T extends Packet> boolean sendRequestPacket(T packet, Consumer<T> callback, String channel) {
        Map<String, String> data = new HashMap<>();

        this.getPacketCallbackRepository().addRequestCallback(packet.getUniqueId(), callback);

        data = this.getPacketWriter().writePacket(packet, data);
        return this.send(data, channel);
    }

    public boolean sendPacket(Packet packet, Consumer<Void> doneCallback, String channel) {
        Map<String, String> data = new HashMap<>();

        this.getPacketCallbackRepository().addDoneCallback(packet.getUniqueId(), doneCallback);

        data = this.getPacketWriter().writePacket(packet, data);
        return this.send(data, channel);
    }

    public boolean sendPacket(Packet packet, String channel) {
        Map<String, String> data = new HashMap<>();
        data = this.getPacketWriter().writePacket(packet, data);
        return this.send(data, channel);
    }

    private boolean send(Map<String, String> data, String channel) {
        String message = "";

        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!message.equalsIgnoreCase("")) {
                message += Packet.SPLITERATOR;
            }

            message += entry.getKey() + "=" + entry.getValue();
        }

        //System.out.println("Send Data: " + message);
        return this.getRedisConnection().publish(channel, message);
    }
}
