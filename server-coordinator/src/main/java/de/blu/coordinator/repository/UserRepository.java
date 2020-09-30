package de.blu.coordinator.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.FileRootConfig;
import de.blu.common.util.PasswordAuthentication;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

@Singleton
@Getter
public final class UserRepository {

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private PasswordAuthentication passwordAuthentication;

    public void createUser(String username, String password) {
        if (this.userExist(username)) {
            return;
        }

        File userDirectory = new File(this.getUsersDirectory(), username);
        userDirectory.mkdirs();

        File configFile = new File(userDirectory, "data.properties");

        Properties properties = new Properties();
        properties.setProperty("username", username);
        properties.setProperty("password", this.getPasswordAuthentication().hash(password));

        try {
            properties.store(new FileWriter(configFile), "Credentials for this user");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean userExist(String username) {
        return new File(this.getUsersDirectory(), username).exists();
    }

    public File getUsersDirectory() {
        File usersDirectory = new File(this.getFileRootConfig().getRootFileDirectory() + "/Users");
        if (!usersDirectory.exists()) {
            usersDirectory.mkdirs();
        }

        return usersDirectory;
    }

    public boolean verifyUser(String username, String password) {
        if (!this.userExist(username)) {
            return false;
        }

        File userDirectory = new File(this.getUsersDirectory(), username);
        File configFile = new File(userDirectory, "data.properties");

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String hash = properties.getProperty("password");

        if (password.equals(hash)) {
            return true;
        }

        return this.getPasswordAuthentication().authenticate(password, hash);
    }

    public String getPasswordHash(String username) {
        if (!this.userExist(username)) {
            return "";
        }

        File userDirectory = new File(this.getUsersDirectory(), username);
        File configFile = new File(userDirectory, "data.properties");

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(configFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String hash = properties.getProperty("password");

        return hash;
    }
}
