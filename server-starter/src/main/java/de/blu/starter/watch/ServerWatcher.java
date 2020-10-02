package de.blu.starter.watch;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.FileRootConfig;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.storage.LogsStorage;
import de.blu.starter.sender.ServerStoppedSender;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public final class ServerWatcher {

    private Collection<GameServerInformation> gameServers = new ArrayList<>();

    @Inject
    private ServerStoppedSender serverStoppedSender;

    @Inject
    private FileRootConfig fileRootConfig;

    @Inject
    private LogsStorage logsStorage;

    public void startTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ServerWatcher.this.run();
            }
        }, 3000, 3000);
    }

    public void run() {
        synchronized (this.getGameServers()) {
            Collection<GameServerInformation> toRemove = new ArrayList<>();
            for (GameServerInformation gameServerInformation : this.getGameServers()) {
                if (this.isScreenActive(gameServerInformation)) {
                    /* Not our business. if something failed in the screen, the user should look into it
                    on his own

                    // Check also if console stuck
                    String fullServerName = gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString();
                    File logFile = new File(new File(this.getFileRootConfig().getRootFileDirectory()), "Logs/" + fullServerName + ".log");

                    // Check if log is stuck
                    long time = System.currentTimeMillis() - logFile.lastModified();
                    long timeout = TimeUnit.SECONDS.toMillis(30);
                    if (time >= timeout) {
                        toRemove.add(gameServerInformation);
                        this.getServerStoppedSender().sendServerStopped(gameServerInformation);
                        this.killScreen(gameServerInformation);
                    }
                    */

                    continue;
                }

                toRemove.add(gameServerInformation);

                File logFile = null;
                if (gameServerInformation.getCloudType().getType().equals(CloudType.Type.BUNGEECORD)) {
                    logFile = new File(gameServerInformation.getTemporaryPath() + "/logs/proxy.log.0");
                } else if (gameServerInformation.getCloudType().getType().equals(CloudType.Type.BUKKIT)) {
                    logFile = new File(gameServerInformation.getTemporaryPath() + "/logs/latest.log");
                }

                if (logFile != null && logFile.exists()) {
                    this.getLogsStorage().postUrl(gameServerInformation.getUniqueId(), logFile, -1);
                }

                System.out.println("ServerWatcher detected stopped screen for Server " + gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString());
                this.getServerStoppedSender().sendServerStopped(gameServerInformation);
            }

            this.getGameServers().removeAll(toRemove);
        }
    }

    private boolean isScreenActive(GameServerInformation gameServerInformation) {
        String userName = System.getProperty("user.name");
        String command = "ls -A -1 /var/run/screen/S-" + userName + " | grep \"^[0-9]*\\.%s$\"";
        String fullServerName = gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString();
        String filledCommand = String.format(command, fullServerName);

        String output = this.executeCommand(filledCommand);
        return !output.equalsIgnoreCase("");
    }

    public void killScreen(GameServerInformation gameServerInformation) {
        String command = "screen -S %s -X quit";
        String fullServerName = gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString();
        String filledCommand = String.format(command, fullServerName);
        this.executeCommand(filledCommand);
    }

    private String executeCommand(String command) {
        String line;
        String output = "";
        try {
            String[] cmd = {"/bin/sh", "-c", command};
            /*
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(cmd);

            Process process = processBuilder.start();
            */

            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            process.waitFor(5, TimeUnit.SECONDS);
            while ((line = inputReader.readLine()) != null) {
                output += line;
            }
            while ((line = errorReader.readLine()) != null) {
                System.out.println("Error: " + line);
            }

            inputReader.close();
            errorReader.close();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            String stackTrace = sw.toString();
            int logerror = stackTrace.length();
            if (logerror > 500) {
                output = "Error:" + stackTrace.substring(0, 500);
            } else {
                output = "Error:" + stackTrace.substring(0, logerror - 1);
            }
        }

        return output;
    }
}
