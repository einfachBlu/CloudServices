package de.blu.connector.bukkit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.database.redis.RedisConnectionProvider;
import de.blu.common.util.LibraryUtils;
import de.blu.connector.common.ConnectorService;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public final class BukkitConnectorPlugin extends JavaPlugin {

    private ConnectorService connectorService;

    @Override
    public void onEnable() {
        try {
            // Init Libraries
            LibraryUtils.createLibraryFolder(this.getDataFolder());
            LibraryUtils.loadLibraries();

            // Create Injector
            Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(JavaPlugin.class).toInstance(BukkitConnectorPlugin.this);
                    bind(File.class).annotatedWith(Names.named("dataFolder")).toInstance(BukkitConnectorPlugin.this.getDataFolder());

                    bind(RedisConnection.class).to(RedisConnectionProvider.class);
                    bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
                }
            });

            // Calling Injected Constructor
            this.connectorService = injector.getInstance(BukkitConnectorService.class);
            this.connectorService.onEnable();
        } catch (Exception e) {
            e.printStackTrace();
            ProxyServer.getInstance().stop();
        }
    }

    @Override
    public void onDisable() {
        this.connectorService.onEnable();
    }
}
