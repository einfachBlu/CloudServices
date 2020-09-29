package de.blu.coordinator.repository;

import com.google.inject.Singleton;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
@Getter
public final class ServerStarterHostRepository {

    private Map<UUID, String> serverStarterHosts = new HashMap<>();
}
