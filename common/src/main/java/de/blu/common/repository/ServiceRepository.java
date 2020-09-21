package de.blu.common.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.logging.Logger;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.ServiceInformation;
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
    private RedisConnection redisConnection;

    @Inject
    private Logger logger;

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
}
