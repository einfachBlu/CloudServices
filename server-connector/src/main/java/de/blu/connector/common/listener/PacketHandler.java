package de.blu.connector.common.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.network.packet.handler.DefaultPacketHandler;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import lombok.Getter;

@Singleton
@Getter
public final class PacketHandler extends DefaultPacketHandler {

    @Inject
    private CloudTypeConfigLoader cloudTypeConfigLoader;

    @Override
    public void registerAll() {
        super.registerAll();

        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, "CallbackChannel");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, this.getSelfServiceInformation().getIdentifier().toString());

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            this.getCloudTypeConfigLoader().reload();
        }, "CloudCoordinatorReloaded");
    }
}
