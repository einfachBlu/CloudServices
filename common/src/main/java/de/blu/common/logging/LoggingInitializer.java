package de.blu.common.logging;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;

import java.io.File;

@Singleton
public final class LoggingInitializer {

    @Getter
    private Logger logger;

    @Inject
    private LoggingInitializer(Logger logger) {
        this.logger = logger;
    }

    public void init(File logsDirectory) {
        this.getLogger().init(logsDirectory);
    }
}
