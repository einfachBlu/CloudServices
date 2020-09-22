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
import de.blu.common.service.Services;
import de.blu.starter.request.CloudTypeRequester;
import lombok.Getter;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import javax.management.*;
import java.lang.management.ManagementFactory;

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
    private Logger logger;

    public void registerAll() {
        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, "CallbackChannel");

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

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            RequestResourcesPacket requestResourcesPacket = (RequestResourcesPacket) packet;
            Sigar sigar = new Sigar();

            try {
                requestResourcesPacket.setUsedCpu((int) sigar.getCpu().getTotal());
                requestResourcesPacket.setUsedMemory((int) sigar.getMem().getUsed());
                requestResourcesPacket.setMaxMemory((int) sigar.getMem().getTotal());
            } catch (SigarException e) {
                e.printStackTrace();
            }

            sigar.close();

            requestResourcesPacket.sendBack();
        }, "RequestResources");
    }

    private int getCpuUsage() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

            if (list.isEmpty()) return -2;

            Attribute att = (Attribute) list.get(0);
            Double value = (Double) att.getValue();

            // usually takes a couple of seconds before we get real values
            if (value == -1.0) return -3;
            // returns a percentage value with 1 decimal point precision

            return (int) ((int) (value * 1000) / 10.0);
        } catch (MalformedObjectNameException | InstanceNotFoundException | ReflectionException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
