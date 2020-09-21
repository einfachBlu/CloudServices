package de.blu.common.service;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ServiceInformation {
    private String name;
    private UUID identifier;

    @Override
    public boolean equals(Object o) {
        ServiceInformation serviceInformation = (ServiceInformation) o;

        return serviceInformation.getIdentifier().equals(this.getIdentifier());
    }
}
