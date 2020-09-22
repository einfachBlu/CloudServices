package de.blu.common.converter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.ServiceInformation;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.UUID;

@Singleton
@Getter
public final class GameServerJsonConverter {

    @Inject
    private Injector injector;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    public String toJson(GameServerInformation gameServerInformation) {
        JSONObject data = this.getInjector().getInstance(JSONObject.class);

        data.put("uniqueId", gameServerInformation.getUniqueId().toString());
        data.put("name", gameServerInformation.getName());
        data.put("id", gameServerInformation.getId());
        data.put("cloudType", gameServerInformation.getCloudType().getName());
        data.put("host", gameServerInformation.getHost());
        data.put("port", gameServerInformation.getPort());
        data.put("temporaryPath", gameServerInformation.getTemporaryPath());
        data.put("onlinePlayers", gameServerInformation.getOnlinePlayers());
        data.put("maxPlayers", gameServerInformation.getMaxPlayers());
        data.put("state", gameServerInformation.getState().name());
        data.put("serverStarterInformationIdentifier", gameServerInformation.getServerStarterInformation().getIdentifier().toString());
        data.put("serverStarterInformationName", gameServerInformation.getServerStarterInformation().getName());

        return data.toJSONString();
    }

    public GameServerInformation fromJson(String json) {
        JSONObject data = (JSONObject) JSONValue.parse(json);
        GameServerInformation gameServerInformation = this.getInjector().getInstance(GameServerInformation.class);
        ServiceInformation serviceInformation = this.getInjector().getInstance(ServiceInformation.class);

        gameServerInformation.setUniqueId(UUID.fromString((String) data.get("uniqueId")));
        gameServerInformation.setName((String) data.get("name"));
        gameServerInformation.setId((int) ((long) data.get("id")));
        gameServerInformation.setOnlinePlayers((int) ((long) data.get("onlinePlayers")));
        gameServerInformation.setMaxPlayers((int) ((long) data.get("maxPlayers")));
        gameServerInformation.setTemporaryPath((String) data.get("temporaryPath"));
        gameServerInformation.setHost((String) data.get("host"));
        gameServerInformation.setPort((int) ((long) data.get("port")));
        gameServerInformation.setState(GameServerInformation.State.valueOf((String) data.get("state")));

        UUID serviceIdentifier = UUID.fromString((String) data.get("serverStarterInformationIdentifier"));
        String serviceName = (String) data.get("serverStarterInformationName");
        String cloudTypeName = (String) data.get("cloudType");

        serviceInformation.setIdentifier(serviceIdentifier);
        serviceInformation.setName(serviceName);
        gameServerInformation.setServerStarterInformation(serviceInformation);

        CloudType cloudType = this.getCloudTypeRepository().getCloudTypeByName(cloudTypeName);
        gameServerInformation.setCloudType(cloudType);

        return gameServerInformation;
    }
}
