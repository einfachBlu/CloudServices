package de.blu.coordinator.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.WebTokenConfig;
import de.blu.common.data.CloudType;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.coordinator.request.ServerStartRequester;
import lombok.Getter;

import static spark.Spark.get;

@Singleton
@Getter
public final class StartServerRequest extends RestAPIListener {

    @Inject
    private ServerStartRequester serverStartRequester;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private StartServerRequest(WebTokenConfig webTokenConfig) {
        super(webTokenConfig);
    }

    public void init() {
        get("/cloudtype/startserver/:cloudtype", (request, response) -> {
            if (!this.isAllowed(request)) {
                return "<h1>Forbidden</h1>";
            }

            String cloudTypeName = request.params("cloudtype");

            if (cloudTypeName == null) {
                return "unknown CloudType";
            }

            CloudType cloudType = this.getCloudTypeRepository().getCloudTypeByName(cloudTypeName);
            if (cloudType == null) {
                return "unknown CloudType";
            }

            this.getServerStartRequester().requestGameServerStart(cloudType, true);
            return "success";
        });
    }
}
