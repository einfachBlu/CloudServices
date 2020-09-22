package de.blu.common.network.packet;

import com.google.inject.Inject;
import de.blu.common.network.packet.repository.PacketCallbackRepository;
import de.blu.common.network.packet.sender.PacketSender;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public abstract class Packet {

    public static final String SPLITERATOR = ";-:-:;";

    private UUID uniqueId = UUID.randomUUID();

    @Inject
    private PacketCallbackRepository packetCallbackRepository;

    @Inject
    private PacketSender packetSender;

    public Map<String, String> write(Map<String, String> data) {
        return data;
    }

    public void read(Map<String, String> content) {
    }

    public void executeCallback() {
        // Call Callbacks
        this.getPacketCallbackRepository().executeCallback(this.getUniqueId(), this);
    }

    public void sendBack() {
        this.getPacketSender().sendPacket(this, "CallbackChannel");
    }
}
