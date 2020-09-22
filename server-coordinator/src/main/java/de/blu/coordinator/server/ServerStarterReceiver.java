package de.blu.coordinator.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.network.packet.packets.RequestResourcesPacket;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.ServiceInformation;
import de.blu.common.service.Services;
import de.blu.coordinator.request.ResourceRequester;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Singleton
@Getter(AccessLevel.PRIVATE)
public final class ServerStarterReceiver {

    @Inject
    private ExecutorService executorService;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private ResourceRequester resourceRequester;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    public void getBestServerStarter(CloudType cloudType, Consumer<ServiceInformation> bestServerStarterCallback) {
        this.getExecutorService().execute(() -> {
            Collection<ServiceInformation> serverStarterServiceInformation = this.getServiceRepository().getServicesBy(Services.SERVER_STARTER);
            Map<ServiceInformation, RequestResourcesPacket> serviceResources = new HashMap<>();

            for (ServiceInformation serviceInformation : serverStarterServiceInformation) {
                this.getResourceRequester().requestResources(requestResourcesPacket -> {
                    serviceResources.put(serviceInformation, requestResourcesPacket);
                }, serviceInformation);
            }

            long time = System.currentTimeMillis();
            long timeout = 5000;
            whileLoop:
            while (true) {
                if (System.currentTimeMillis() - time >= timeout) {
                    System.out.println("Missing a Callback of an ResourceRequest! timed out.");
                    bestServerStarterCallback.accept(null);
                    break;
                }

                for (ServiceInformation serviceInformation : serverStarterServiceInformation) {
                    if (!serviceResources.containsKey(serviceInformation)) {
                        continue whileLoop;
                    }
                }

                // Check for the best ServerStarter based on their resources
                ServiceInformation bestServerStarter = null;
                RequestResourcesPacket bestServerStarterResources = null;
                for (Map.Entry<ServiceInformation, RequestResourcesPacket> entry : serviceResources.entrySet()) {
                    ServiceInformation serviceInformation = entry.getKey();
                    RequestResourcesPacket requestResourcesPacket = entry.getValue();

                    if (cloudType.getHosts().size() > 0 && !cloudType.getHosts().contains(requestResourcesPacket.getHostName())) {
                        // Cant be started on this ServerStarter because cloudtype has specified other hostNames
                        continue;
                    }

                    int availableMemory = requestResourcesPacket.getMaxMemory() - requestResourcesPacket.getUsedMemory();
                    availableMemory -= 512; // To prevent going completely out of memory

                    if (cloudType.getMemory() > availableMemory) {
                        // Has not enough memory
                        continue;
                    }

                    if (bestServerStarter == null) {
                        bestServerStarter = serviceInformation;
                        bestServerStarterResources = requestResourcesPacket;
                        continue;
                    }

                    if (requestResourcesPacket.getAverageCpuLoad() >= bestServerStarterResources.getAverageCpuLoad()) {
                        // current best has lower average cpu load, so we prefer that
                        continue;
                    }

                    bestServerStarter = serviceInformation;
                    bestServerStarterResources = requestResourcesPacket;
                }

                bestServerStarterCallback.accept(bestServerStarter);
                break;
            }
        });
    }
}
