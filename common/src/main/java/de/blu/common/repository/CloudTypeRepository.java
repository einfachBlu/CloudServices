package de.blu.common.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.logging.Logger;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public final class CloudTypeRepository {

    private static final int REDIS_CACHE_TIME = (int) TimeUnit.DAYS.toSeconds(90);

    @Setter
    private Collection<CloudType> cloudTypes = new ArrayList<>();

    @Inject
    private RedisConnection redisConnection;

    @Setter
    private String json = "";

    public CloudType getCloudTypeByName(String cloudTypeName) {
        return this.getCloudTypes()
                .stream()
                .filter(cloudType -> cloudType.getName().equalsIgnoreCase(cloudTypeName))
                .findFirst().orElse(null);
    }
}
