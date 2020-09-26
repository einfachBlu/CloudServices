package de.blu.connector.bukkit.api.event;

import de.blu.common.data.GameServerInformation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public final class ServerUpdatedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private GameServerInformation gameServerInformation;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
