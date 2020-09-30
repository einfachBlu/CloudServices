package de.blu.coordinator.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.config.WebTokenConfig;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.FileReader;

import static spark.Spark.get;

@Singleton
@Getter
public final class CloudTypeGetter extends RestAPIListener {

    @Inject
    private CloudTypeConfigLoader cloudTypeConfigLoader;

    @Inject
    private CloudTypeGetter(WebTokenConfig webTokenConfig) {
        super(webTokenConfig);
    }

    public void init() {
        get("/cloudtypes", (request, response) -> {
            if (!this.isAllowed(request)) {
                return "<h1>Forbidden</h1>";
            }

            JSONObject jsonObject = (JSONObject) JSONValue.parse(new FileReader(this.getCloudTypeConfigLoader().getConfigFile()));
            return jsonObject.toJSONString();
        });
    }
}
