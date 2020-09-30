package de.blu.common.config;

import com.google.inject.Singleton;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class LogsConfig {

    @Getter
    private Properties properties = new Properties() {{
        this.setProperty("enabled", "false");
        this.setProperty("post_url", "http://127.0.0.1:7777/documents");
        this.setProperty("paste_url", "http://127.0.0.1:7777/");
        this.setProperty("cache_time", "259200"); // Seconds
    }};

    public void load(File configFile) {
        // Read Properties of it
        try {
            this.getProperties().load(new FileInputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isEnabled() {
        return Boolean.parseBoolean(this.getProperties().getProperty("enabled"));
    }

    public long getCacheTime() {
        return Integer.parseInt(this.getProperties().getProperty("cache_time"));
    }

    public String getPostUrl() {
        return this.getProperties().getProperty("post_url");
    }

    public String getPasteUrl() {
        return this.getProperties().getProperty("paste_url");
    }

    public void setEnabled(boolean enabled) {
        this.getProperties().setProperty("enabled", String.valueOf(enabled));
    }

    public void setCacheTime(long cacheTime) {
        this.getProperties().setProperty("cache_time", String.valueOf(cacheTime));
    }

    public void setPostUrl(String postUrl) {
        this.getProperties().setProperty("post_url", postUrl);
    }

    public void setPasteUrl(String pasteUrl) {
        this.getProperties().setProperty("paste_url", pasteUrl);
    }

    public void save(File configFile) {
        // Read Properties of it
        configFile.getParentFile().mkdirs();

        try {
            this.getProperties().store(new FileWriter(configFile), "Hastebin Logs Storage");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
