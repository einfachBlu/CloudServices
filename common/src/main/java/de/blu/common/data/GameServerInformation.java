package de.blu.common.data;

import de.blu.common.service.ServiceInformation;
import lombok.Getter;
import lombok.Setter;

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
    private int maxPlayers = 100;
    private ServiceInformation serverStarterInformation;
    private State state = State.CREATED;

    public enum State {
        CREATED, STARTING, ONLINE, STOPPING, OFFLINE
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
