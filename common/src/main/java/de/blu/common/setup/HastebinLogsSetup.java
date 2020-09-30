package de.blu.common.setup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.command.ConsoleInputReader;
import de.blu.common.config.LogsConfig;
import de.blu.common.logging.Logger;
import lombok.Getter;

import java.io.File;

@Singleton
@Getter
public final class HastebinLogsSetup {

    @Inject
    private Logger logger;

    @Inject
    private ConsoleInputReader consoleInputReader;

    @Inject
    private LogsConfig logsConfig;

    public void startSetup(File configFile) {
        this.getLogger().info("");
        this.getLogger().info("&bYou need to Setup LogsStorage in haste-server.");

        this.askForEnabled();
        if (this.getLogsConfig().isEnabled()) {
            this.askForPostUrl();
            this.getLogger().info("&aUsing PostURL '" + this.getLogsConfig().getPostUrl() + "'");
            this.askForPasteUrl();
            this.getLogger().info("&aUsing PasteURL '" + this.getLogsConfig().getPasteUrl() + "'");
            this.askForCacheTime();
            this.getLogger().info("&aUsing CacheTime '" + this.getLogsConfig().getCacheTime() + "'");
        }

        this.getLogger().info("&bYour LogsStorage Settings are now set.");
        this.getLogsConfig().save(configFile);
    }

    private void askForEnabled() {
        boolean defaultEnabled = false;
        boolean enabled = false;
        this.getLogger().info("Would you like to enable the haste-server Logs Storage? (default: &e" + defaultEnabled + "&r)");
        String enabledString = this.getConsoleInputReader().readLine();

        if (enabledString.equalsIgnoreCase("")) {
            enabledString = String.valueOf(defaultEnabled);
        }

        try {
            enabled = Boolean.parseBoolean(enabledString);
        } catch (Exception e) {
            this.getLogger().error("The Value must be a boolean. Enter 'true' or 'false'!");
            this.askForEnabled();
            return;
        }

        this.getLogsConfig().setEnabled(enabled);
    }

    private void askForCacheTime() {
        long defaultCacheTime = 259200;
        long cacheTime = 259200;
        this.getLogger().info("How long should the logs are saved in seconds? (default: &e" + defaultCacheTime + "&r)");
        String cacheTimeString = this.getConsoleInputReader().readLine();

        if (cacheTimeString.equalsIgnoreCase("")) {
            cacheTimeString = String.valueOf(defaultCacheTime);
        }

        try {
            cacheTime = Integer.parseInt(cacheTimeString);
        } catch (NumberFormatException e) {
            this.getLogger().error("The Value must be a number!");
            this.askForEnabled();
            return;
        }

        this.getLogsConfig().setCacheTime(cacheTime);
    }

    private void askForPostUrl() {
        String defaultPostUrl = "http://127.0.0.1:7777/documents";
        this.getLogger().info("What is the PostURL? (default: &e" + defaultPostUrl + "&r)");
        String postUrl = this.getConsoleInputReader().readLine();

        if (postUrl.equalsIgnoreCase("")) {
            postUrl = defaultPostUrl;
        }

        this.getLogsConfig().setPostUrl(postUrl);
    }

    private void askForPasteUrl() {
        String defaultPasteUrl = "http://127.0.0.1:7777/";
        this.getLogger().info("What is the PostURL? (default: &e" + defaultPasteUrl + "&r)");
        String pasteUrl = this.getConsoleInputReader().readLine();

        if (pasteUrl.equalsIgnoreCase("")) {
            pasteUrl = defaultPasteUrl;
        }

        this.getLogsConfig().setPasteUrl(pasteUrl);
    }
}