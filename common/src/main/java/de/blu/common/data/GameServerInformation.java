package de.blu.common.data;

import de.blu.common.service.ServiceInformation;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class GameServerInformation {

    private UUID uniqueId = UUID.randomUUID();
    private String name = "";
    private int id = 0;
    private CloudType cloudType;
    private String host = "127.0.0.1";
    private int port = 0;
    private String temporaryPath = "";
    private int onlinePlayers = 0;
    private int maxPlayers = 20;
    private String gameState = GameState.LOADING.name();
    private String extra = "";
    private ServiceInformation serverStarterInformation;
    private State state = State.CREATED;
    private boolean manuallyStarted = false;
    private Map<String, String> meta = new HashMap<>();

    public enum State {
        CREATED, STARTING, ONLINE, STOPPING, OFFLINE
    }

    public enum GameState {
        LOADING, LOBBY, INGAME, END
    }

    public void setCloudType(CloudType cloudType) {
        this.cloudType = cloudType;

        this.setMaxPlayers(this.getCloudType().getMaxPlayers());
    }

    public boolean hasMeta(String key) {
        return this.getMeta().containsKey(key);
    }

    public String getMetaString(String key) {
        return this.getMeta().getOrDefault(key, null);
    }

    public int getMetaInt(String key) {
        return Integer.parseInt(this.getMeta().getOrDefault(key, "-1"));
    }

    public boolean getMetaBoolean(String key) {
        return Boolean.parseBoolean(this.getMeta().getOrDefault(key, "false"));
    }

    public long getMetaLong(String key) {
        return Long.parseLong(this.getMeta().getOrDefault(key, "-1"));
    }

    @Override
    public boolean equals(Object object) {
        return ((GameServerInformation) object).getName().equalsIgnoreCase(this.getName());
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
