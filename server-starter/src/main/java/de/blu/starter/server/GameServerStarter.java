package de.blu.starter.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.FileRootConfig;
import de.blu.common.data.GameServerInformation;
import lombok.Getter;

import java.io.*;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
@Getter
public final class GameServerStarter {

    @Inject
    private FileRootConfig fileRootConfig;

    public boolean startGameServer(GameServerInformation gameServerInformation) {
        String fullServerName = gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString();
        File logFile = new File(new File(this.getFileRootConfig().getRootFileDirectory()), "Logs/" + fullServerName + ".log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.println("Starting " + gameServerInformation.getName() + " on Port " + gameServerInformation.getPort());

            Process process = new ProcessBuilder(
                    "/bin/sh", "-c",
                    "screen -dmS " + fullServerName +
                            /*" -L -Logfile " + logFile.getAbsolutePath() + */ // Disabled Logging into a specific directory
                            " /bin/sh -c '" +
                            "cd " + new File(gameServerInformation.getTemporaryPath()).getAbsolutePath() + " &&" +
                            " java -server" +
                            " -Xmx" + gameServerInformation.getCloudType().getMemory() + "M " +
                            this.convertParametersToString(gameServerInformation.getCloudType().getJavaParameters()) +
                            " -Dcom.mojang.eula.agree=true" +
                            " -Dcloud-servername=" + gameServerInformation.getName() +
                            " -Dcloud-serveruuid=" + gameServerInformation.getUniqueId().toString() +
                            " -Dcloud-fileroot=" + this.getFileRootConfig().getRootFileDirectory() +
                            " -jar server-software.jar -p " + gameServerInformation.getPort() + " " +
                            this.convertParametersToString(gameServerInformation.getCloudType().getServerParameters()) +
                            "'"
            ).start();

            process.waitFor(10, TimeUnit.SECONDS);

            /*
            // TEst
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    String command = "ls -A -1 /var/run/screen/S-${USER} | grep \"^[0-9]*\\.%s$\"";
                    String filledCommand = String.format(command, fullServerName);

                    String output = GameServerStarter.this.executeCommand(filledCommand);
                    if (!output.equalsIgnoreCase("")) {
                        System.out.println(fullServerName + " IS NOW STARTED AS SCREEN!");
                    } else {
                        System.out.println("FAILED:");
                        System.out.println("CMD: " + filledCommand);
                    }
                }
            }, 500, 500);
            */
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    private String convertParametersToString(Collection<String> parameters) {
        return String.join(" ", parameters);
    }
}
