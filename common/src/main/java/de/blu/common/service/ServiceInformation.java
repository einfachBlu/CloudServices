package de.blu.common.service;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ServiceInformation {
    private String name = "";
    private UUID identifier = UUID.randomUUID();

    @Override
    public boolean equals(Object o) {
        ServiceInformation serviceInformation = (ServiceInformation) o;

        return serviceInformation.getIdentifier().equals(this.getIdentifier());
    }

    public Services getService() {
        if (Services.SERVER_COORDINATOR.getServiceName().equalsIgnoreCase(this.getName())) {
            return Services.SERVER_COORDINATOR;
        } else if (Services.SERVER_STARTER.getServiceName().equalsIgnoreCase(this.getName())) {
            return Services.SERVER_STARTER;
        }

        return null;
    }
}
