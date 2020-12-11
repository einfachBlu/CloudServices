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
            try {
                Collection<ServiceInformation> serverStarterServiceInformation = this.getServiceRepository().getServicesBy(Services.SERVER_STARTER);
                Map<ServiceInformation, RequestResourcesPacket> serviceResources = new HashMap<>();

                for (ServiceInformation serviceInformation : serverStarterServiceInformation) {
                    this.getResourceRequester().requestResources(requestResourcesPacket -> {
                        serviceResources.put(serviceInformation, requestResourcesPacket);

                    /*
                    System.out.println("&bReceived Resources of ServerStarter " + serviceInformation.getIdentifier().toString() + ":");
                    System.out.println("hostname: " + requestResourcesPacket.getHostName());
                    System.out.println("averageCpu: " + requestResourcesPacket.getAverageCpuLoad());
                    System.out.println("usedMemory: " + requestResourcesPacket.getUsedMemory());
                    System.out.println("maxMemory: " + requestResourcesPacket.getMaxMemory());
                     */
                    }, serviceInformation);
                }

                long time = System.currentTimeMillis();
                long timeout = 5000;
                boolean lastStarterHasNoMemory = false;
                whileLoop:
                while (true) {
                    if (System.currentTimeMillis() - time >= timeout) {
                        System.out.println("&cMissing a Callback of a ResourceRequest! timed out. CloudType: " + cloudType.getName());
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
                            lastStarterHasNoMemory = true;
                            continue;
                        }

                        lastStarterHasNoMemory = false;

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

                    if (bestServerStarter == null && lastStarterHasNoMemory) {
                        System.out.println("&eAll ServerStarters are out of memory for " + cloudType.getName() + "!");
                    }

                    bestServerStarterCallback.accept(bestServerStarter);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                bestServerStarterCallback.accept(null);
            }
        });
    }
}
