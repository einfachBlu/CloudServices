package de.blu.common.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class CloudType {

    private String name = "";
    private Type type = Type.BUKKIT;
    private int minOnlineServers = 0;
    private int maxOnlineServers = 0;
    private int portStart = 0;
    private int portEnd = 0;
    private int priority = 0;
    private int maxPlayers = 0;
    private int memory = 0;
    private String permission = null;
    private boolean staticService = false;
    private String templatePath = null;
    private Collection<String> hosts = new ArrayList<>();
    private Collection<String> inheritances = new ArrayList<>();
    private Collection<String> proxyFallbackPriorities = new ArrayList<>();
    private Collection<String> javaParameters = new ArrayList<>();
    private Collection<String> serverParameters = new ArrayList<>();
    private Map<String, Object> meta = new HashMap<>();

    public enum Type {
        BUKKIT, BUNGEECORD, TEMPLATE
    }

    public boolean hasMeta(String key) {
        return this.getMeta().containsKey(key);
    }

    public Object getMeta(String key) {
        return this.getMeta().getOrDefault(key, null);
    }

    public int getMetaInt(String key) {
        return (int) ((long) this.getMeta().getOrDefault(key, -1));
    }

    public String getMetaString(String key) {
        return (String) this.getMeta().getOrDefault(key, "");
    }

    public boolean getMetaBoolean(String key) {
        return (boolean) this.getMeta().getOrDefault(key, false);
    }

    public long getMetaLong(String key) {
        return (long) this.getMeta().getOrDefault(key, -1);
    }

    @Override
    public boolean equals(Object object) {
        return ((CloudType) object).getName().equalsIgnoreCase(this.getName());
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
