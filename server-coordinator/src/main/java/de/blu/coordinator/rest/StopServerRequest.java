package de.blu.coordinator.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.WebTokenConfig;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.GameServerRepository;
import de.blu.coordinator.request.ServerStopRequester;
import lombok.Getter;

import static spark.Spark.get;

@Singleton
@Getter
public final class StopServerRequest extends RestAPIListener {

    @Inject
    private ServerStopRequester serverStopRequester;

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private StopServerRequest(WebTokenConfig webTokenConfig) {
        super(webTokenConfig);
    }

    public void init() {
        get("/servers/stopserver/:server", (request, response) -> {
            if (!this.isAllowed(request)) {
                return "<h1>Forbidden</h1>";
            }

            String serverName = request.params("server");

            if (serverName == null) {
                return "unknown server";
            }

            GameServerInformation gameServer = this.getGameServerRepository().getGameServerByName(serverName);
            if (gameServer == null) {
                return "unknown server";
            }

            this.getServerStopRequester().requestGameServerStop(gameServer);
            return "success";
        });
    }
}
