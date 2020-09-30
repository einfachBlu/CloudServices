package de.blu.coordinator.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.WebTokenConfig;
import de.blu.common.converter.GameServerJsonConverter;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.GameServerRepository;
import lombok.Getter;

import static spark.Spark.get;

@Singleton
@Getter
public final class GameServerGetter extends RestAPIListener {

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerJsonConverter gameServerJsonConverter;

    @Inject
    private GameServerGetter(WebTokenConfig webTokenConfig) {
        super(webTokenConfig);
    }

    public void init() {
        get("/servers", (request, response) -> {
            if (!this.isAllowed(request)) {
                return "<h1>Forbidden</h1>";
            }

            StringBuilder result = new StringBuilder();
            result.append("{");

            for (GameServerInformation gameServer : this.getGameServerRepository().getGameServers()) {
                String json = this.getGameServerJsonConverter().toJson(gameServer);

                if (result.length() != 1) {
                    result.append(",");
                }

                result.append(json);
            }

            result.append("}");

            return result.toString();
        });
    }
}