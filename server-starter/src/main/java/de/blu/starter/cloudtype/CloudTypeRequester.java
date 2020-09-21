package de.blu.starter.cloudtype;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.RequestCloudTypesPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.ServiceInformation;
import de.blu.common.service.Services;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collection;

@Singleton
@Getter
public final class CloudTypeRequester {

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private PacketSender packetSender;

    @Inject
    private Injector injector;

    public void requestCloudTypes() {
        Collection<ServiceInformation> serverCoordinatorServices = this.getServiceRepository().getServicesByName(Services.SERVER_COORDINATOR.getServiceName());
        if (serverCoordinatorServices.size() > 0) {
            // Request CloudTypes
            RequestCloudTypesPacket requestCloudTypesPacket = this.getInjector().getInstance(RequestCloudTypesPacket.class);
            this.getPacketSender().sendRequestPacket(requestCloudTypesPacket, requestCloudTypesPacket1 -> {
                System.out.println("&eCloudTypes &rreceived from server-coordinator: &e" + Arrays.toString(requestCloudTypesPacket1.getCloudTypes().toArray()));
            }, "RequestCloudTypes");
        }
    }
}
