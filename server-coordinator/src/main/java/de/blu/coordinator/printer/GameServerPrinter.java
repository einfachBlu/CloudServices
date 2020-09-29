package de.blu.coordinator.printer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.GameServerRepository;
import lombok.Getter;

import java.util.Collections;

@Singleton
@Getter
public final class GameServerPrinter {

    @Inject
    private GameServerRepository gameServerRepository;

    public void printAll() {
        System.out.println("&eGameServer (&c" + this.getGameServerRepository().getGameServers().size() + "&e):");
        System.out.println("");
        for (GameServerInformation gameServerInformation : this.getGameServerRepository().getGameServers()) {
            this.print(gameServerInformation);
        }
    }

    public void print(GameServerInformation gameServerInformation) {
        System.out.println("&e" + gameServerInformation.getName() + " &r{");
        System.out.println(this.getTabString(1) + "&euniqueId: &r" + gameServerInformation.getUniqueId().toString());
        System.out.println(this.getTabString(1) + "&eid: &r" + gameServerInformation.getId());
        System.out.println(this.getTabString(1) + "&ecloudType: &r" + gameServerInformation.getCloudType().getName());
        System.out.println(this.getTabString(1) + "&emanuallyStarted: &r" + gameServerInformation.isManuallyStarted());
        System.out.println(this.getTabString(1) + "&estate: &r" + gameServerInformation.getState().name());
        System.out.println(this.getTabString(1) + "&egameState: &r" + gameServerInformation.getGameState());
        System.out.println(this.getTabString(1) + "&eextra: &r" + gameServerInformation.getExtra());
        System.out.println(this.getTabString(1) + "&eonlinePlayers: &r" + gameServerInformation.getOnlinePlayers());
        System.out.println(this.getTabString(1) + "&emaxPlayers: &r" + gameServerInformation.getMaxPlayers());
        System.out.println(this.getTabString(1) + "&ehost: &r" + gameServerInformation.getHost());
        System.out.println(this.getTabString(1) + "&eport: &r" + gameServerInformation.getPort());
        System.out.println(this.getTabString(1) + "&etemporaryPath: &r" + gameServerInformation.getTemporaryPath());
        System.out.println(this.getTabString(1) + "&eserverStarterInformation: &r" + gameServerInformation.getServerStarterInformation().getIdentifier().toString());
        System.out.println("}");
    }

    private String getTabString(int tabAmount) {
        String tabString = "  ";
        return String.join("", Collections.nCopies(tabAmount, tabString));
    }
}
