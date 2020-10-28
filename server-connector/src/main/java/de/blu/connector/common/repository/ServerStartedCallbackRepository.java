package de.blu.connector.common.repository;

import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Singleton
@Getter
public final class ServerStartedCallbackRepository {
    private Map<UUID, Consumer<GameServerInformation>> callbacks = new HashMap<>();

    public void handleServerStarted(GameServerInformation gameServer) {
        if (!gameServer.hasMeta("apiStartedId")) {
            return;
        }

        UUID uuid = UUID.fromString(gameServer.getMeta().get("apiStartedId"));
        if (!this.getCallbacks().containsKey(uuid)) {
            return;
        }

        this.getCallbacks().remove(uuid).accept(gameServer);
    }
}
