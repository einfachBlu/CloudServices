package de.minimichecker.common.network.packet.receiver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.minimichecker.common.network.packet.listener.PacketListener;
import de.minimichecker.common.network.packet.packets.Packet;
import de.minimichecker.common.network.packet.repository.PacketCallbackRepository;
import de.minimichecker.common.network.packet.repository.PacketListenerRepository;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;

@Singleton
@Getter
public final class PacketReceiver {

    @Inject
    private PacketReader packetReader;

    @Inject
    private PacketCallbackRepository packetCallbackRepository;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    public void onMessageReceived(String channel, String message) {
        boolean hadCallback = false;

        Packet packet = this.getPacketReader().readPacket(message);
        if (packet == null) {
            return;
        }

        // Call Callbacks
        hadCallback = this.getPacketCallbackRepository().executeCallback(packet.getUniqueId(), packet);

        // Call listeners
        for (Map.Entry<PacketListener, Collection<String>> entry : this.getPacketListenerRepository().getPacketListeners().entrySet()) {
            if (!entry.getValue().contains(channel)) {
                continue;
            }

            entry.getKey().onPacketReceived(packet, hadCallback);
        }
    }
}
