package de.blu.webpanel;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.database.redis.RedisConnectionProvider;
import de.blu.common.util.LibraryUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class WebPanel {

    public static void main(String[] args) {
        try {
            // Init Libraries
            LibraryUtils.createLibraryFolder(WebPanel.getRootDirectory());
            LibraryUtils.loadLibraries();

            new WebPanelService().init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SpringApplication.run(WebPanel.class, args);
    }

    public static File getRootDirectory() {
        File directory = null;

        try {
            directory = new File(WebPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (!directory.isDirectory()) {
            if (!directory.mkdir()) {
                throw new NullPointerException("Couldn't create root directory!");
            }
        }

        return directory;
    }
}