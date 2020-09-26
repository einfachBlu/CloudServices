package de.blu.connector.bungeecord.api.event;

import de.blu.common.data.GameServerInformation;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.plugin.Event;

@Getter
@Setter
public final class ServerStoppedEvent extends Event {

    private GameServerInformation gameServerInformation;
}
