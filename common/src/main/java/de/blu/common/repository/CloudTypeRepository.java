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

    @Inject
    private Logger logger;

    public void save() {
        // Clear old CloudTypes
        this.getRedisConnection().removeRecursive("cloudtype");

        // Save CloudTypes in Redis
        synchronized (this.getCloudTypes()) {
            for (CloudType cloudType : this.getCloudTypes()) {
                /*
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".type", cloudType.getType().name(), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".staticService", String.valueOf(cloudType.isStaticService()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".minOnlineServers", String.valueOf(cloudType.getMinOnlineServers()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".maxOnlineServers", String.valueOf(cloudType.getMaxOnlineServers()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".portStart", String.valueOf(cloudType.getPortStart()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".portEnd", String.valueOf(cloudType.getPortEnd()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".memory", String.valueOf(cloudType.getMemory()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".maintenance", String.valueOf(cloudType.isMaintenance()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".templatePath", String.valueOf(cloudType.getTemplatePath()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".inheritances", String.join(",", cloudType.getInheritances()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".nodes", String.join(",", cloudType.getNodes()), REDIS_CACHE_TIME);
                this.getRedisConnection().set("cloudtype." + cloudType.getName() + ".fallbackPriorities", String.join(",", cloudType.getProxyFallbackPriorities()), REDIS_CACHE_TIME);
                 */
            }
        }
    }

    public CloudType getCloudTypeByName(String cloudTypeName) {
        return this.getCloudTypes()
                .stream()
                .filter(cloudType -> cloudType.getName().equalsIgnoreCase(cloudTypeName))
                .findFirst().orElse(null);
    }
}
