package de.minimichecker.common.setup;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.minimichecker.common.command.ConsoleInputReader;
import de.minimichecker.common.config.RedisConfig;
import de.minimichecker.common.logging.Logger;
import de.minimichecker.common.util.AddressResolver;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

@Singleton
@Getter
public final class RedisCredentialsSetup {

    @Inject
    private Logger logger;

    @Inject
    private ConsoleInputReader consoleInputReader;

    @Inject
    private RedisConfig redisConfig;

    @Inject
    private AddressResolver addressResolver;

    public void startSetup(File configFile) {
        this.getLogger().info("");
        this.getLogger().info("&bYou need to Setup the Credentials of Redis.");

        this.askForHost();
        this.getLogger().info("&aUsing Host '" + this.getRedisConfig().getHost() + "'");
        this.askForPort();
        this.getLogger().info("&aUsing Port '" + this.getRedisConfig().getPort() + "'");
        this.askForPassword();
        this.getLogger().info("&aUsing Password '" + this.getRedisConfig().getPassword() + "'");

        this.getLogger().info("&bYour Credentials are now set.");
        this.getRedisConfig().save(configFile);
    }

    private void askForHost() {
        String defaultHost = this.getAddressResolver().getIPAddress();
        this.getLogger().info("What is the Hostname/IP of the Redis Server? (default: &e" + defaultHost + "&r)");
        String host = this.getConsoleInputReader().readLine();

        if (host.equalsIgnoreCase("")) {
            host = defaultHost;
        }

        // Check if Hostname is reachable
        boolean reachable = false;
        try {
            reachable = InetAddress.getByName(host).isReachable(2500);
            if (!reachable) {
                this.getLogger().error("The Hostname/IP is not reachable!");
            }
        } catch (Exception e) {
            this.getLogger().error("The Hostname/IP is not valid!");
        }

        if (!reachable) {
            this.askForHost();
            return;
        }

        this.getRedisConfig().setHost(host);
    }

    private void askForPassword() {
        String defaultPassword = "";
        this.getLogger().info("What is the Hostname/IP of the Redis Server? (default: &e" + (defaultPassword.equals("") ? "-" : defaultPassword) + "&r)");
        String password = this.getConsoleInputReader().readLine();
        this.getRedisConfig().setPassword(password);
    }

    private void askForPort() {
        int defaultPort = 6379;
        int port;
        this.getLogger().info("What is the Port of the Redis Server? (default: &e" + defaultPort + "&r)");
        String portString = this.getConsoleInputReader().readLine();

        if (portString.equalsIgnoreCase("")) {
            portString = String.valueOf(defaultPort);
        }

        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
            this.getLogger().error("The Port must be a Number!");
            this.askForPort();
            return;
        }

        if (port <= 0 || port > 65535) {
            this.getLogger().error("The Port must be a Number between 1 and 65535!");
            this.askForPort();
            return;
        }

        // Check if Hostname is reachable
        boolean reachable = false;
        reachable = this.isRemoteHostReachable(this.getRedisConfig().getHost(), port, 2500);
        if (!reachable) {
            this.getLogger().error("Could not ping " + this.getRedisConfig().getHost() + ":" + port);
        }

        if (!reachable) {
            this.askForPort();
            return;
        }

        this.getRedisConfig().setPort(port);
    }

    /**
     * Overriding default InetAddress.isReachable() method to add 2 more arguments port and timeout value
     * <p>
     * Address: www.google.com
     * port: 80 or 443
     * timeout: 2000 (in milliseconds)
     */
    private boolean isRemoteHostReachable(String address, int port, int timeout) {
        try {
            try (Socket crunchifySocket = new Socket()) {
                // Connects this socket to the server with a specified timeout value.
                crunchifySocket.connect(new InetSocketAddress(address, port), timeout);
            }
            // Return true if connection successful
            return true;
        } catch (IOException exception) {
            //exception.printStackTrace();
            // Return false if connection fails
            return false;
        }
    }
}