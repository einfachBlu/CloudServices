package de.blu.common.network.packet.packets;

import com.google.inject.Inject;
import de.blu.common.network.packet.repository.PacketCallbackRepository;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public abstract class Packet {

    private UUID uniqueId = UUID.randomUUID();

    @Inject
    private PacketCallbackRepository packetCallbackRepository;

    public Map<String, String> write(Map<String, String> data) {
        return data;
    }

    public void read(Map<String, String> content) {
    }

    public void sendCallback() {
        // Call Callbacks
        this.getPacketCallbackRepository().executeCallback(this.getUniqueId(), this);
    }
}
