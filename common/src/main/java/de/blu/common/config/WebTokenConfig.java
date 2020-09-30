package de.blu.common.config;

import com.google.inject.Singleton;
import de.blu.common.util.RandomString;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class WebTokenConfig {

    @Getter
    private Properties properties = new Properties() {{
        this.setProperty("token", new RandomString(80).nextString());
    }};

    public void load(File configFile) {
        // Read Properties of it
        try {
            this.getProperties().load(new FileInputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
        return this.getProperties().getProperty("token");
    }

    public void setToken(String token) {
        this.getProperties().setProperty("token", token);
    }

    public void save(File configFile) {
        // Read Properties of it
        configFile.getParentFile().mkdirs();

        try {
            this.getProperties().store(new FileWriter(configFile), "Web Credentials");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
