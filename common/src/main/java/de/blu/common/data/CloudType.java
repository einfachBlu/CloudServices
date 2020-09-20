package de.blu.common.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

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
    private int memory = 0;
    private String permission = null;
    private boolean staticService = false;
    private String templatePath = "/" + this.name + "/";
    private Collection<String> hosts = new ArrayList<>();
    private Collection<String> inheritances = new ArrayList<>();
    private Collection<String> proxyFallbackPriorities = new ArrayList<>();
    private Collection<String> javaParameters = new ArrayList<>();
    private Collection<String> serverParameters = new ArrayList<>();

    public enum Type {
        BUKKIT, BUNGEECORD, TEMPLATE
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
