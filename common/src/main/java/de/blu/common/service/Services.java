package de.blu.common.service;

import lombok.Getter;

public enum Services {
    SERVER_COORDINATOR("server-coordinator"),
    SERVER_STARTER("server-starter"),
    SERVER_CONNECTOR("server-connector");

    @Getter
    private String serviceName;

    Services(String serviceName) {
        this.serviceName = serviceName;
    }
}
