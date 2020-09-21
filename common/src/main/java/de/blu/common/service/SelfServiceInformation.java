package de.blu.common.service;

import com.google.inject.Singleton;

import java.util.UUID;

@Singleton
public final class SelfServiceInformation extends ServiceInformation {
    public SelfServiceInformation() {
        this.setName("service-unnamed");
        this.setIdentifier(UUID.randomUUID());
    }
}
