package de.blu.common.cloudtype;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.FileRootConfig;
import de.blu.common.converter.CloudTypeJsonConverter;
import de.blu.common.data.CloudType;
import de.blu.common.logging.Logger;
import de.blu.common.repository.CloudTypeRepository;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Singleton
@Getter
public final class CloudTypeConfigLoader {

    @Inject
    private Logger logger;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private CloudTypeJsonConverter cloudTypeJsonConverter;

    private File configFile;

    public void initDefaultConfig() throws Exception {
        this.configFile = new File(this.getFileRootConfig().getRootFileDirectory(), "Configs/cloudtypes.json");

        if (!this.getConfigFile().exists()) {
            this.saveDefaultConfig();
        }
        this.loadConfig();

        this.getLogger().info("Loaded CloudTypes: " + Arrays.toString(this.getCloudTypeRepository().getCloudTypes().toArray()));
    }

    public void reload() {
        try {
            this.initDefaultConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() throws Exception {
        try {
            FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("/config/cloudtypes.json"), this.getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void loadConfig() throws Exception {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(new FileReader(this.getConfigFile()));

        this.getCloudTypeRepository().setCloudTypes(this.loadFromJson(jsonObject.toJSONString()));
    }

    public Collection<CloudType> loadFromJson(String json) {
        if (json.equalsIgnoreCase("")) {
            return new ArrayList<>();
        }

        this.getCloudTypeRepository().setJson(json);
        JSONObject jsonObject = (JSONObject) JSONValue.parse(json);

        Collection<CloudType> cloudTypes = new ArrayList<>();
        for (Object cloudTypeName : jsonObject.keySet()) {
            JSONObject cloudTypeData = (JSONObject) jsonObject.get(cloudTypeName);
            CloudType cloudType = this.getCloudTypeJsonConverter().fromJson((String) cloudTypeName, cloudTypeData);
            cloudTypes.add(cloudType);
        }

        return cloudTypes;
    }
}
