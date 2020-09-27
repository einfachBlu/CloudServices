package de.blu.starter.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.cloudtype.CloudTypeConfigLoader;
import de.blu.common.data.GameServerInformation;
import de.blu.common.network.packet.handler.DefaultPacketHandler;
import de.blu.common.network.packet.packets.*;
import de.blu.starter.ServerStarter;
import de.blu.starter.server.GameServerStarter;
import de.blu.starter.template.TemplateInitializer;
import de.blu.starter.watch.ServerWatcher;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.io.File;
import java.io.IOException;

@Singleton
@Getter
public final class PacketHandler extends DefaultPacketHandler {

    @Inject
    private TemplateInitializer templateInitializer;

    @Inject
    private GameServerStarter gameServerStarter;

    @Inject
    private CloudTypeConfigLoader cloudTypeConfigLoader;

    @Inject
    private ServerWatcher serverWatcher;

    @Override
    public void registerAll() {
        super.registerAll();

        // Register default for Callbacks
        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
        }, "CallbackChannel");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            if (packet instanceof RequestResourcesPacket) {
                RequestResourcesPacket requestResourcesPacket = (RequestResourcesPacket) packet;
                Sigar sigar = new Sigar();

                try {
                    int usedMemory = (int) (sigar.getMem().getUsed() / 1024 / 1024);
                    int maxMemory = (int) (sigar.getMem().getTotal() / 1024 / 1024);
                    double cpuLoadAverage = sigar.getLoadAverage()[0];
                    int amountOfCores = Runtime.getRuntime().availableProcessors();
                    double cpuLoad = (cpuLoadAverage / (double) amountOfCores) * 100;

                    requestResourcesPacket.setAverageCpuLoad(cpuLoad);
                    requestResourcesPacket.setUsedMemory(usedMemory);
                    requestResourcesPacket.setMaxMemory(maxMemory);
                    requestResourcesPacket.setHostName(sigar.getNetInfo().getHostName());
                } catch (SigarException e) {
                    e.printStackTrace();
                }

                sigar.close();

                requestResourcesPacket.sendBack();
                return;
            }

            if (packet instanceof RequestGameServerStopPacket) {
                RequestGameServerStopPacket requestGameServerStopPacket = (RequestGameServerStopPacket) packet;
                GameServerInformation gameServerInformation = requestGameServerStopPacket.getGameServerInformation();

                this.getServerWatcher().killScreen(gameServerInformation);
                return;
            }

            if (packet instanceof RequestServiceStopPacket) {
                System.exit(0);
            }

            if (packet instanceof RequestGameServerStartPacket) {
                RequestGameServerStartPacket requestGameServerStartPacket = (RequestGameServerStartPacket) packet;
                GameServerInformation gameServerInformation = requestGameServerStartPacket.getGameServerInformation();

                File tempDirectory = new File(ServerStarter.getRootDirectory(), "" +
                        "temporary/" + gameServerInformation.getCloudType().getName() + "/" + gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString());

                if (gameServerInformation.getCloudType().isStaticService()) {
                    tempDirectory = new File(ServerStarter.getRootDirectory(), "" +
                            "static/" + gameServerInformation.getName());
                }

                gameServerInformation.setHost(this.getAddressResolver().getIPAddress());
                gameServerInformation.setTemporaryPath(tempDirectory.getAbsolutePath());

                // Check if port is in use
                if (this.getAddressResolver().isPortInUse(gameServerInformation.getHost(), gameServerInformation.getPort())) {
                    gameServerInformation.setState(GameServerInformation.State.OFFLINE);
                    this.getGameServerStorage().saveGameServer(gameServerInformation);
                    // Information:
                    // Its also possible to increase the port here and check if any other is free on this host, set
                    // the new port and save the data then. The current case is very rare normally, so we leave
                    // it like this for now
                    requestGameServerStartPacket.setErrorMessage("The Port is already in use!");
                    requestGameServerStartPacket.sendBack();
                    return;
                }

                // Save new State, Host & TemporaryPath
                gameServerInformation.setState(GameServerInformation.State.STARTING);
                this.getGameServerStorage().saveGameServer(gameServerInformation);

                // Create Directory
                this.getTemplateInitializer().createDirectory(gameServerInformation);
                this.getTemplateInitializer().copyTemplates(gameServerInformation);
                this.getTemplateInitializer().prepareDirectory(gameServerInformation);

                // Check for needed files
                File serverSoftwareFile = new File(tempDirectory, "server-software.jar");
                File cloudConnectorFile = new File(tempDirectory, "plugins/server-connector.jar");

                if (!serverSoftwareFile.exists()) {
                    gameServerInformation.setState(GameServerInformation.State.OFFLINE);
                    this.getGameServerStorage().saveGameServer(gameServerInformation);
                    requestGameServerStartPacket.setErrorMessage("You need to put the File 'server-software.jar' in the base folder of your template.");

                    try {
                        FileUtils.deleteDirectory(tempDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    requestGameServerStartPacket.sendBack();
                    return;
                }

                if (!cloudConnectorFile.exists()) {
                    gameServerInformation.setState(GameServerInformation.State.OFFLINE);
                    this.getGameServerStorage().saveGameServer(gameServerInformation);
                    requestGameServerStartPacket.setErrorMessage("You need to put the File 'server-connector.jar' in the plugins folder of your template.");

                    try {
                        FileUtils.deleteDirectory(tempDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    requestGameServerStartPacket.sendBack();
                    return;
                }

                // Start Server as Screen
                boolean success = this.getGameServerStarter().startGameServer(gameServerInformation);

                if (!success) {
                    gameServerInformation.setState(GameServerInformation.State.OFFLINE);
                    this.getGameServerStorage().saveGameServer(gameServerInformation);
                    requestGameServerStartPacket.setErrorMessage("An error occured on process start.");

                    try {
                        FileUtils.deleteDirectory(tempDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    requestGameServerStartPacket.sendBack();
                    return;
                }

                // Add to Watch
                this.getServerWatcher().getGameServers().add(gameServerInformation);

                System.out.println("&e" + gameServerInformation.getName() + "&r started successfully.");

                // Send packet back
                requestGameServerStartPacket.sendBack();
            }
        }, this.getSelfServiceInformation().getIdentifier().toString());

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            System.out.println("&aService connected: " + serviceConnectedPacket.getServiceInformation().getName() + " (" + serviceConnectedPacket.getServiceInformation().getIdentifier().toString() + ")");

            this.getServiceRepository().addService(serviceConnectedPacket.getServiceInformation());
        }, "ServiceConnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceDisconnectedPacket serviceDisconnectedPacket = (ServiceDisconnectedPacket) packet;
            System.out.println("&cService disconnected: " + serviceDisconnectedPacket.getServiceInformation().getName() + " (" + serviceDisconnectedPacket.getServiceInformation().getIdentifier().toString() + ")");

            this.getServiceRepository().removeService(serviceDisconnectedPacket.getServiceInformation().getIdentifier());
        }, "ServiceDisconnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            this.getCloudTypeConfigLoader().reload();
        }, "CloudCoordinatorReloaded");
    }
}
