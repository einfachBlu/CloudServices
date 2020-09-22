package de.blu.starter.request;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.network.packet.packets.RequestCloudTypesPacket;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.CloudTypeRepository;
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
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private Injector injector;

    public void requestCloudTypes() {
        Collection<ServiceInformation> serverCoordinatorServices = this.getServiceRepository().getServicesBy(Services.SERVER_COORDINATOR);
        if (serverCoordinatorServices.size() > 0) {
            // Request CloudTypes
            RequestCloudTypesPacket requestCloudTypesPacket = this.getInjector().getInstance(RequestCloudTypesPacket.class);
            this.getPacketSender().sendRequestPacket(requestCloudTypesPacket, requestCloudTypesPacket1 -> {
                this.getCloudTypeRepository().setCloudTypes(requestCloudTypesPacket1.getCloudTypes());
                System.out.println("&eCloudTypes &rreceived from server-coordinator: &e" + Arrays.toString(requestCloudTypesPacket1.getCloudTypes().toArray()));
            }, "RequestCloudTypes");
        }
    }
}
