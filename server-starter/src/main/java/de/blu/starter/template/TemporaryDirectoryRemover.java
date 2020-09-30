package de.blu.starter.template;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.repository.GameServerRepository;
import de.blu.common.storage.GameServerStorage;
import de.blu.starter.ServerStarter;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public final class TemporaryDirectoryRemover {

    private static final int CHECK_INTERVAL = (int) TimeUnit.SECONDS.toMillis(30);

    @Inject
    private GameServerRepository gameServerRepository;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private RedisConnection redisConnection;

    public void startTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                TemporaryDirectoryRemover.this.checkForDirectories();
            }
        }, CHECK_INTERVAL, CHECK_INTERVAL);
    }

    private void checkForDirectories() {
        File mainTemporaryDirectory = new File(ServerStarter.getRootDirectory(), "temporary");
        if (!mainTemporaryDirectory.exists()) {
            return;
        }

        for (File cloudTypeTempDirectory : mainTemporaryDirectory.listFiles()) {
            for (File temporaryDirectory : cloudTypeTempDirectory.listFiles()) {
                if (this.getRedisConnection().contains("gameserver." + temporaryDirectory.getName())) {
                    continue;
                }
            }
        }
    }
}
