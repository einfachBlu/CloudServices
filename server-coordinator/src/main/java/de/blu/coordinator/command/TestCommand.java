package de.blu.coordinator.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.data.Command;
import de.blu.common.command.data.CommandExecutor;
import de.blu.common.database.redis.RedisConnection;
import lombok.Getter;

@Singleton
@Command(name = "test", aliases = "t")
@Getter
public final class TestCommand extends CommandExecutor {

    @Inject
    private RedisConnection redisConnection;

    @Override
    public void execute(String label, String[] args) {
        if (args.length == 0) {
            this.printRedisContent();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "clearredis":
            case "cr":
                this.clearRedis("");
                break;
            case "listrediscontent":
            case "listredis":
            case "redis":
            case "lr":
                this.printRedisContent();
                break;
        }
    }

    private void clearRedis(String key) {
        for (String childKey : this.getRedisConnection().getKeys(key, true)) {
            String fullKey = key.isEmpty() ? childKey : key + "." + childKey;
            this.getRedisConnection().remove(fullKey);

            this.clearRedis(fullKey);
        }
    }

    private void printRedisContent() {
        for (String key : this.getRedisConnection().getKeys("", true)) {
            System.out.println("&e" + key + ": &b" + this.getRedisConnection().get(key));
        }
    }
}
