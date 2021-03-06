package de.blu.common.service;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.repository.ServiceRepository;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public final class ServiceKeepAlive {

    @Inject
    private Injector injector;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private ServiceConnectorBroadcast serviceConnectorBroadcast;

    public void init() {
        this.getServiceConnectorBroadcast().broadcastConnect();

        // Add existing to local Services
        for (ServiceInformation serviceInformation : this.getServices()) {
            this.getServiceRepository().addService(serviceInformation);
        }

        this.startTimer();
    }

    public Collection<ServiceInformation> getServices() {
        if (!this.getRedisConnection().contains("service")) {
            return new ArrayList<>();
        }

        Collection<ServiceInformation> services = new ArrayList<>();

        for (String serviceIdentifierString : this.getRedisConnection().getKeys("service")) {
            if (services.stream().anyMatch(serviceInformation -> serviceInformation.getIdentifier().toString().equalsIgnoreCase(serviceIdentifierString))) {
                continue;
            }

            String name = this.getRedisConnection().get("service." + serviceIdentifierString + ".name");
            UUID identifier = UUID.fromString(serviceIdentifierString);

            ServiceInformation serviceInformation = this.getInjector().getInstance(ServiceInformation.class);
            serviceInformation.setName(name);
            serviceInformation.setIdentifier(identifier);
            services.add(serviceInformation);
        }

        return services;
    }

    public void startTimer() {
        long time = ServiceRepository.KEEP_ALIVE_TIME;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //long time = System.currentTimeMillis();
                ServiceKeepAlive.this.handleTimerRun();
                //System.out.println("Needed Time for KeepAlive Check: " + (System.currentTimeMillis() - time) + "ms");
            }
        }, 0, time);
    }

    private void handleTimerRun() {
        this.update();

        /*
        Collection<ServiceInformation> services = new ArrayList<>(this.getServiceRepository().getServices().values());

        We remove this for now because it mostly calls false
        for (ServiceInformation serviceInformation : services) {
            //System.out.println("&bCheck for KeepAlive: " + serviceInformation.getIdentifier().toString());
            if (ServiceKeepAlive.this.getRedisConnection().contains("service." + serviceInformation.getIdentifier().toString())) {
                continue;
            }

            this.getServiceConnectorBroadcast().broadcastDisconnect(serviceInformation);
        }
         */
    }

    public void update() {
        this.getRedisConnection().set("service." + this.getSelfServiceInformation().getIdentifier().toString() + ".name", this.getSelfServiceInformation().getName(), (int) TimeUnit.MILLISECONDS.toSeconds(ServiceRepository.REDIS_CACHE_TIME));
        this.getRedisConnection().set("service." + this.getSelfServiceInformation().getIdentifier().toString() + ".identifier", this.getSelfServiceInformation().getIdentifier().toString(), (int) TimeUnit.MILLISECONDS.toSeconds(ServiceRepository.REDIS_CACHE_TIME));
    }

    public void remove() {
        this.getRedisConnection().remove("service." + this.getSelfServiceInformation().getIdentifier().toString() + ".name");
        this.getRedisConnection().remove("service." + this.getSelfServiceInformation().getIdentifier().toString() + ".identifier");
    }
}
