package de.blu.connector.common.listener;

import com.google.inject.Singleton;
import de.blu.common.network.packet.handler.DefaultPacketHandler;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import lombok.Getter;

@Singleton
@Getter
public final class PacketHandler extends DefaultPacketHandler {

    @Override
    public void registerAll() {
        super.registerAll();

        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, "CallbackChannel");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, this.getSelfServiceInformation().getIdentifier().toString());
    }
}
