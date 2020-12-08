package de.blu.connector.bungeecord.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.Getter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@Singleton
@Getter
public final class MainConfig {

    private boolean fallbackHandling = true;

    @Inject
    @Named("dataFolder")
    private File dataFolder;

    public void load() {
        File configFile = this.getConfigFile();
        this.saveDefaultConfig();
        
        try {
            Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            this.fallbackHandling = config.getBoolean("fallbackHandling");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultConfig() {
        File configFile = this.getConfigFile();

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();

                Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

                config.set("fallbackHandling", true);

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getConfigFile() {
        return new File(this.getDataFolder(), "config.yml");
    }
}
