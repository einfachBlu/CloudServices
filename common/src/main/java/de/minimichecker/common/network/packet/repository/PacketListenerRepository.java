package de.minimichecker.common.network.packet.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.minimichecker.common.database.redis.RedisConnection;
import de.minimichecker.common.network.packet.listener.PacketListener;
import de.minimichecker.common.network.packet.receiver.RedisMessageReceiver;
import lombok.Getter;

import java.util.*;

@Singleton
@Getter
public final class PacketListenerRepository {

    @Inject
    private RedisMessageReceiver redisMessageReceiver;

    @Inject
    private RedisConnection redisConnection;

    private Map<PacketListener, Collection<String>> packetListeners = new HashMap<>();

    public void registerListener(PacketListener packetListener, String channel) {
        if (!this.getPacketListeners().containsKey(packetListener)) {
            this.getPacketListeners().put(packetListener, new HashSet<>(Collections.singleton(channel)));
            return;
        }

        this.getPacketListeners().get(packetListener).add(channel);
        this.getRedisConnection().subscribe((channel1, message) -> this.getRedisMessageReceiver().onMessageReceived(channel1, message), channel);
    }
}
