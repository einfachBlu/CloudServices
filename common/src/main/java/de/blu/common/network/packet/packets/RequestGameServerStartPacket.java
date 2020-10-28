package de.blu.common.network.packet.packets;

import com.google.inject.Inject;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.network.packet.Packet;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.storage.GameServerStorage;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class RequestGameServerStartPacket extends Packet {

    private static final String SPLITERATOR = ";GEmaAspjMwpZZu3w;";

    // This is when sending request from server-connector API -> Master
    private CloudType cloudType;
    private Map<String, String> meta = new HashMap<>();

    // This is when sending started Server from Master -> ServerStarter
    private String gameServerName = "";
    private UUID gameServerUniqueId;

    // Callback Information
    private String errorMessage = "";
    private boolean success = false;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Override
    public Map<String, String> write(Map<String, String> data) {
        data.put("gameServerName", this.getGameServerName());
        if (this.getGameServerUniqueId() != null) {
            data.put("gameServerUniqueId", this.getGameServerUniqueId().toString());
        }
        data.put("errorMessage", this.getErrorMessage());
        data.put("success", String.valueOf(this.isSuccess()));

        if (this.getCloudType() != null) {
            data.put("cloudType", this.getCloudType().getName());
        }
        data.put("metaSize", String.valueOf(this.getMeta().size()));

        int i = 0;
        for (Map.Entry<String, String> entry : this.getMeta().entrySet()) {
            data.put("meta." + i, entry.getKey() + SPLITERATOR + entry.getValue());
            i++;
        }

        return data;
    }

    @Override
    public void read(Map<String, String> content) {
        this.setGameServerName(content.get("gameServerName"));

        if (content.containsKey("gameServerUniqueId")) {
            this.setGameServerUniqueId(UUID.fromString(content.get("gameServerUniqueId")));
        }

        this.setErrorMessage(content.get("errorMessage"));
        this.setSuccess(Boolean.parseBoolean(content.get("success")));

        if (content.containsKey("cloudType")) {
            String cloudTypeName = content.get("cloudType");
            this.setCloudType(this.getCloudTypeRepository().getCloudTypeByName(cloudTypeName));
        }

        long metaSize = Long.parseLong(content.get("metaSize"));
        for (int i = 0; i < metaSize; i++) {
            String entry = content.get("meta." + i);
            String key = entry.split(SPLITERATOR)[0];
            String value = entry.split(SPLITERATOR)[1];

            this.getMeta().put(key, value);
        }
    }

    public GameServerInformation getGameServerInformation() {
        return this.getGameServerStorage().getGameServer(this.getGameServerName(), this.getGameServerUniqueId());
    }
}
