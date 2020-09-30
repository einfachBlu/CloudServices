package de.blu.coordinator.rest;

import de.blu.common.config.WebTokenConfig;
import lombok.Getter;
import spark.Request;

@Getter
public abstract class RestAPIListener {

    private WebTokenConfig webTokenConfig;

    protected RestAPIListener(WebTokenConfig webTokenConfig) {
        this.webTokenConfig = webTokenConfig;
    }

    public abstract void init();

    public boolean isAllowed(Request request) {
        String apiToken = request.headers("apitoken");

        return this.getWebTokenConfig().getToken().equalsIgnoreCase(apiToken);
    }
}
