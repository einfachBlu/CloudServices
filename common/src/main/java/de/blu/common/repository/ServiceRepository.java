package de.blu.common.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.ServiceInformation;
import de.blu.common.service.Services;
import lombok.Getter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
@Getter
public final class ServiceRepository {

    public static final int REDIS_CACHE_TIME = 10000;
    public static final long KEEP_ALIVE_TIME = 8000;

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
        return this.getServices().values().stream()
                .filter(serviceInformation -> serviceInformation.getName().equalsIgnoreCase(service.getServiceName()))
                .collect(Collectors.toList());
    }
}
