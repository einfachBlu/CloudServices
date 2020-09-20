package de.blu.common.util;

import com.google.inject.Singleton;
import lombok.Getter;

import java.util.UUID;

@Singleton
public final class ApplicationIdentifierProvider {

    @Getter
    private final UUID uniqueId = UUID.randomUUID();
}
