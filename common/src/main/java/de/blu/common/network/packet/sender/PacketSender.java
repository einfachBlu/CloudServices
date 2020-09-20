package de.blu.common.network.packet.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.network.packet.packets.Packet;
import de.blu.common.network.packet.repository.PacketCallbackRepository;
import de.blu.common.util.ApplicationIdentifierProvider;
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
    private ApplicationIdentifierProvider applicationIdentifierProvider;

    @Inject
    private PacketCallbackRepository packetCallbackRepository;

    @Inject
    private RedisConnection redisConnection;

    public <T extends Packet> void sendRequestPacket(T packet, Consumer<T> callback, String channel) {
        Map<String, String> data = new HashMap<>();
        data.put("senderIdentifier", this.getApplicationIdentifierProvider().getUniqueId().toString());

        this.getPacketCallbackRepository().addRequestCallback(packet.getUniqueId(), callback);

        data = this.getPacketWriter().writePacket(packet, data);
        this.send(data, channel);
    }

    public void sendPacket(Packet packet, Consumer<Void> doneCallback, String channel) {
        Map<String, String> data = new HashMap<>();
        data.put("senderIdentifier", this.getApplicationIdentifierProvider().getUniqueId().toString());

        this.getPacketCallbackRepository().addDoneCallback(packet.getUniqueId(), doneCallback);

        data = this.getPacketWriter().writePacket(packet, data);
        this.send(data, channel);
    }

    public void sendPacket(Packet packet, String channel) {
        Map<String, String> data = new HashMap<>();
        data.put("senderIdentifier", this.getApplicationIdentifierProvider().getUniqueId().toString());
        data = this.getPacketWriter().writePacket(packet, data);
        this.send(data, channel);
    }

    private void send(Map<String, String> data, String channel) {
        String message = "";

        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!message.equalsIgnoreCase("")) {
                message += Packet.SPLITERATOR;
            }

            message += entry.getKey() + "=" + entry.getValue();
        }

        //System.out.println("Send Data: " + message);
        this.getRedisConnection().publish(channel, message);
    }
}
