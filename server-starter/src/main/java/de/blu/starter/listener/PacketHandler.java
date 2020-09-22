package de.blu.starter.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.logging.Logger;
import de.blu.common.network.packet.packets.RequestResourcesPacket;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.Services;
import de.blu.starter.request.CloudTypeRequester;
import lombok.Getter;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

@Singleton
@Getter
public final class PacketHandler {

    @Inject
    private PacketSender packetSender;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private CloudTypeRequester cloudTypeRequester;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private Logger logger;

    public void registerAll() {
        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, "CallbackChannel");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof RequestResourcesPacket) {
                RequestResourcesPacket requestResourcesPacket = (RequestResourcesPacket) packet;
                Sigar sigar = new Sigar();

                try {
                    int usedMemory = (int) (sigar.getMem().getUsed() / 1024 / 1024);
                    int maxMemory = (int) (sigar.getMem().getTotal() / 1024 / 1024);
                    double cpuLoadAverage = sigar.getLoadAverage()[0];
                    int amountOfCores = Runtime.getRuntime().availableProcessors();
                    double cpuLoad = (cpuLoadAverage / (double) amountOfCores) * 100;

                    requestResourcesPacket.setAverageCpuLoad(cpuLoad);
                    requestResourcesPacket.setUsedMemory(usedMemory);
                    requestResourcesPacket.setMaxMemory(maxMemory);
                    requestResourcesPacket.setHostName(sigar.getNetInfo().getHostName());
                } catch (SigarException e) {
                    e.printStackTrace();
                }

                sigar.close();

                requestResourcesPacket.sendBack();
            }
        }, this.getSelfServiceInformation().getIdentifier().toString());

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            this.getLogger().info("&aService connected: " + serviceConnectedPacket.getServiceInformation().getName() + " (" + serviceConnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().addService(serviceConnectedPacket.getServiceInformation());

            if (Services.SERVER_COORDINATOR.equals(serviceConnectedPacket.getServiceInformation().getService())) {
                this.getCloudTypeRequester().requestCloudTypes();
            }
        }, "ServiceConnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceDisconnectedPacket serviceDisconnectedPacket = (ServiceDisconnectedPacket) packet;
            this.getLogger().info("&cService disconnected: " + serviceDisconnectedPacket.getServiceInformation().getName() + " (" + serviceDisconnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().removeService(serviceDisconnectedPacket.getServiceInformation().getIdentifier());
        }, "ServiceDisconnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            this.getCloudTypeRequester().requestCloudTypes();
        }, "CloudCoordinatorReloaded");
    }
}
