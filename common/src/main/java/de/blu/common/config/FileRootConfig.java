package de.blu.common.config;

import com.google.inject.Singleton;
import lombok.Getter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class FileRootConfig {

    @Getter
    private Properties properties = new Properties() {{
        this.setProperty("directory", "/home/sync/");
    }};

    public void load(File configFile) {
        // Read Properties of it
        try {
            this.getProperties().load(new FileInputStream(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getRootFileDirectory() {
        return this.getProperties().getProperty("directory");
    }

    public void setRootFileDirectory(String rootFileDirectoryPath) {
        this.getProperties().setProperty("directory", rootFileDirectoryPath);
    }

    public void save(File configFile) {
        // Read Properties of it
        try {
            this.getProperties().store(new FileWriter(configFile), "RootFile Directory");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
