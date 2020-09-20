package de.minimichecker.common.network.packet.receiver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

@Singleton
@Getter
public final class RedisMessageReceiver {

    @Inject
    private PacketReceiver packetReceiver;

    public void onMessageReceived(String channel, String message) {
        this.getPacketReceiver().onMessageReceived(channel, message);
    }
}
