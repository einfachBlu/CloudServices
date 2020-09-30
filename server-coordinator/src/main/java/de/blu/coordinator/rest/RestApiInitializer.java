package de.blu.coordinator.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.WebTokenConfig;
import lombok.Getter;

import static spark.Spark.port;

@Singleton
@Getter
public final class RestApiInitializer {

    @Inject
    private CloudTypeGetter cloudTypeGetter;

    @Inject
    private GameServerGetter gameServerGetter;

    @Inject
    private StartServerRequest startServerRequest;

    @Inject
    private StopServerRequest stopServerRequest;

    @Inject
    private UserVerify userVerify;

    @Inject
    private WebTokenConfig webTokenConfig;

    public void init() {
        port(8090);

        this.getCloudTypeGetter().init();
        this.getGameServerGetter().init();
        this.getStartServerRequest().init();
        this.getStopServerRequest().init();
        this.getUserVerify().init();
    }
}
