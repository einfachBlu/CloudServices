package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import de.blu.common.network.packet.packets.CloudCoordinatorReloadPacket;
import de.blu.common.network.packet.sender.PacketSender;
import lombok.Getter;

@Singleton
@Command(name = "reload", aliases = "rl")
@Getter
public final class ReloadCommand extends CommandExecutor {

    @Inject
    private PacketSender packetSender;

    @Inject
    private CloudTypeConfigLoader cloudTypeConfigLoader;

    @Inject
    private Injector injector;

    @Override
    public void execute(String label, String[] args) {
        System.out.println("Reloading...");

        try {
            this.getCloudTypeConfigLoader().loadConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }

        CloudCoordinatorReloadPacket cloudCoordinatorReloadPacket = this.getInjector().getInstance(CloudCoordinatorReloadPacket.class);
        this.getPacketSender().sendPacket(cloudCoordinatorReloadPacket, "CloudCoordinatorReloaded");

        System.out.println("Reloaded");
    }
}
