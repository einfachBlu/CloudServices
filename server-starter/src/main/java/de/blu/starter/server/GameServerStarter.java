package de.blu.starter.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.FileRootConfig;
import de.blu.common.data.GameServerInformation;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

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
            Process process = new ProcessBuilder(
                    "/bin/sh", "-c",
                    "screen -mdS " + fullServerName +
                            " -L -Logfile " + logFile.getAbsolutePath() +
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
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private String convertParametersToString(Collection<String> parameters) {
        return String.join(" ", parameters);
    }
}
