package de.blu.starter.watch;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.FileRootConfig;
import de.blu.common.data.GameServerInformation;
import de.blu.starter.sender.ServerStoppedSender;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

@Singleton
@Getter
public final class ServerWatcher {

    private Collection<GameServerInformation> gameServers = new ArrayList<>();

    @Inject
    private ServerStoppedSender serverStoppedSender;

    @Inject
    private FileRootConfig fileRootConfig;

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
                this.getServerStoppedSender().sendServerStopped(gameServerInformation);
            }

            this.getGameServers().removeAll(toRemove);
        }
    }

    private boolean isScreenActive(GameServerInformation gameServerInformation) {
        String command = "ls -A -1 /var/run/screen/S-${USER} | grep \"^[0-9]*\\.%s$\"";
        String fullServerName = gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString();
        String filledCommand = String.format(command, fullServerName);

        String output = this.executeCommand(filledCommand);
        if (output.equalsIgnoreCase("")) {
            return false;
        } else {
            return true;
        }
    }

    public void killScreen(GameServerInformation gameServerInformation) {
        String command = "screen -S %s -X quit";
        String fullServerName = gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString();
        String filledCommand = String.format(command, fullServerName);
        this.executeCommand(filledCommand);
    }

    private String executeCommand(String command) {
        String line;
        String strstatus = "";
        try {

            String[] cmd = {"/bin/sh", "-c", command};
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                strstatus = line;
            }
            in.close();
        } catch (Exception e) {

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            String stackTrace = sw.toString();
            int logerror = stackTrace.length();
            if (logerror > 500) {
                strstatus = "Error:" + stackTrace.substring(0, 500);
            } else {
                strstatus = "Error:" + stackTrace.substring(0, logerror - 1);

            }
        }
        return strstatus;

    }
}
