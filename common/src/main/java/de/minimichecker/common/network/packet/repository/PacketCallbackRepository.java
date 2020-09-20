package de.minimichecker.common.network.packet.repository;

import com.google.inject.Singleton;
import de.minimichecker.common.network.packet.packets.Packet;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Singleton
@Getter
public final class PacketCallbackRepository {

    private Map<UUID, Consumer<Packet>> requestCallbacks = new HashMap<>();
    private Map<UUID, Consumer<Void>> doneCallbacks = new HashMap<>();

    public void addRequestCallback(UUID uuid, Consumer<? extends Packet> callback) {
        this.getRequestCallbacks().put(uuid, (Consumer<Packet>) callback);
    }

    public void addDoneCallback(UUID uuid, Consumer<Void> callback) {
        this.getDoneCallbacks().put(uuid, callback);
    }

    public <T extends Packet> boolean executeCallback(UUID uuid, T packet) {
        boolean executedCallbacks = false;
        if (this.getRequestCallbacks().containsKey(uuid)) {
            this.getRequestCallbacks().remove(uuid).accept(packet);
            executedCallbacks = true;
        }

        if (this.getDoneCallbacks().containsKey(uuid)) {
            this.getDoneCallbacks().remove(uuid).accept(null);
            executedCallbacks = true;
        }

        return executedCallbacks;
    }

    public boolean hasCallbacks(UUID uuid) {
        if (this.getRequestCallbacks().containsKey(uuid)) {
            return true;
        }

        if (this.getDoneCallbacks().containsKey(uuid)) {
            return true;
        }

        return false;
    }
}
