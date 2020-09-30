package de.blu.coordinator.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.WebTokenConfig;
import de.blu.coordinator.repository.UserRepository;
import lombok.Getter;

import static spark.Spark.get;

@Singleton
@Getter
public final class UserVerify extends RestAPIListener {

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserVerify(WebTokenConfig webTokenConfig) {
        super(webTokenConfig);
    }

    public void init() {
        get("/userlogin/:username", (request, response) -> {
            if (!this.isAllowed(request)) {
                return "<h1>Forbidden</h1>";
            }

            String username = request.params(":username");

            if (username == null) {
                return "";
            }

            return this.getUserRepository().getPasswordHash(username);
        });

        get("/userlogin_verify/:username/:password", (request, response) -> {
            if (!this.isAllowed(request)) {
                return "<h1>Forbidden</h1>";
            }

            String username = request.params(":username");
            String password = request.params(":password");

            if (username == null || password == null) {
                return "";
            }

            return this.getUserRepository().verifyUser(username, password);
        });
    }
}
