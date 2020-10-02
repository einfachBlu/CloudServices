package de.blu.common.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.ServiceInformation;
import de.blu.common.service.Services;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Getter
public final class ServiceRepository {

    public static final int REDIS_CACHE_TIME = 25000;
    public static final long KEEP_ALIVE_TIME = 4000;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    private Map<UUID, ServiceInformation> services = new HashMap<>();

    public void addService(ServiceInformation serviceInformation) {
        this.getServices().put(serviceInformation.getIdentifier(), serviceInformation);
    }

    public void removeService(UUID serviceIdentifier) {
        this.getServices().remove(serviceIdentifier);
    }

    public Collection<ServiceInformation> getServicesByName(String name) {
        return this.getServices().values().stream()
                .filter(serviceInformation -> serviceInformation.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    public Collection<ServiceInformation> getServicesBy(Services service) {
        if (service == null) {
            return Collections.emptyList();
        }

        List<ServiceInformation> nullReferences = this.getServices().values().stream()
                .filter(serviceInformation -> serviceInformation.getName() == null)
                .collect(Collectors.toList());

        for (ServiceInformation serviceInformation : nullReferences) {
            UUID uuid = this.getServices().entrySet().stream()
                    .filter(entry -> entry.getValue().equals(serviceInformation))
                    .findFirst().get().getKey();

            this.getServices().remove(uuid);
        }

        return this.getServices().values().stream()
                .filter(serviceInformation -> serviceInformation != null && serviceInformation.getName().equalsIgnoreCase(service.getServiceName()))
                .collect(Collectors.toList());
    }
}
