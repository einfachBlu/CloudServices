

package de.blu.common.setup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.ConsoleInputReader;
import de.blu.common.config.FileRootConfig;
import de.blu.common.logging.Logger;
import de.blu.common.util.AddressResolver;
import lombok.Getter;

import java.io.File;

@Singleton
@Getter
public final class FileRootSetup {

    @Inject
    private Logger logger;

    @Inject
    private ConsoleInputReader consoleInputReader;

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private AddressResolver addressResolver;

    public void startSetup(File configFile) {
        this.getLogger().info("");
        this.getLogger().info("&bYou need to Setup the Directory where Templates and Configs are located.");

        this.askForDirectory();
        this.getLogger().info("&aUsing Directory '" + this.getFileRootConfig().getRootFileDirectory() + "'");

        this.getLogger().info("&bYour Directory is now set.");
        this.getFileRootConfig().save(configFile);
    }

    private void askForDirectory() {
        String defaultDirectory = "/home/sync/";
        this.getLogger().info("What is the RootDirectory for Templates & Configs? (default: &e" + defaultDirectory + "&r)");
        String directory = this.getConsoleInputReader().readLine();

        if(!directory.endsWith("/")){
            directory += "/";
        }

        if (directory.equalsIgnoreCase("")) {
            directory = defaultDirectory;
        }

        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }

        this.getFileRootConfig().setRootFileDirectory(directory);
    }
}
