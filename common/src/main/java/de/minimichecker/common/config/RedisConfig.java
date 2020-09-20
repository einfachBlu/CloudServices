package de.minimichecker.common.config;

import com.google.inject.Singleton;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class RedisConfig {

    @Getter
    private Properties properties = new Properties() {{
        this.setProperty("host", "127.0.0.1");
        this.setProperty("port", "6379");
        this.setProperty("password", "");
    }};

    public void createIfNotExist(File configFile) {
        if (configFile.exists()) {
            return;
        }

        // Create default Config
        try {
            configFile.createNewFile();
            this.getProperties().store(new FileWriter(configFile), "Redis Credentials");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(File configFile) {
        // Read Properties of it
        try {
            this.getProperties().load(new FileInputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return this.getProperties().getProperty("host");
    }

    public int getPort() {
        return Integer.parseInt(this.getProperties().getProperty("port"));
    }

    public String getPassword() {
        return this.getProperties().getProperty("password");
    }
}
