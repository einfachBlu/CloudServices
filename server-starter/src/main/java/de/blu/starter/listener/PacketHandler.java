package de.blu.starter.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.converter.GameServerJsonConverter;
import de.blu.common.data.GameServerInformation;
import de.blu.common.database.redis.RedisConnection;
import de.blu.common.logging.Logger;
import de.blu.common.network.packet.packets.RequestGameServerStartPacket;
import de.blu.common.network.packet.packets.RequestResourcesPacket;
import de.blu.common.network.packet.packets.ServiceConnectedPacket;
import de.blu.common.network.packet.packets.ServiceDisconnectedPacket;
import de.blu.common.network.packet.repository.PacketListenerRepository;
import de.blu.common.network.packet.sender.PacketSender;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.common.repository.ServiceRepository;
import de.blu.common.service.SelfServiceInformation;
import de.blu.common.service.Services;
import de.blu.common.storage.GameServerStorage;
import de.blu.common.util.AddressResolver;
import de.blu.starter.ServerStarter;
import de.blu.starter.request.CloudTypeRequester;
import de.blu.starter.template.TemplateInitializer;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import java.io.File;
import java.io.IOException;

@Singleton
@Getter
public final class PacketHandler {

    @Inject
    private PacketSender packetSender;

    @Inject
    private PacketListenerRepository packetListenerRepository;

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private ServiceRepository serviceRepository;

    @Inject
    private CloudTypeRequester cloudTypeRequester;

    @Inject
    private SelfServiceInformation selfServiceInformation;

    @Inject
    private RedisConnection redisConnection;

    @Inject
    private GameServerJsonConverter gameServerJsonConverter;

    @Inject
    private Logger logger;

    @Inject
    private GameServerStorage gameServerStorage;

    @Inject
    private TemplateInitializer templateInitializer;

    @Inject
    private AddressResolver addressResolver;

    public void registerAll() {
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

            if (packet instanceof RequestGameServerStartPacket) {
                RequestGameServerStartPacket requestGameServerStartPacket = (RequestGameServerStartPacket) packet;
                GameServerInformation gameServerInformation = requestGameServerStartPacket.getGameServerInformation();

                File tempDirectory = new File(ServerStarter.getRootDirectory(), "" +
                        "temporary/" + gameServerInformation.getCloudType().getName() + "/" + gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString());

                if (gameServerInformation.getCloudType().isStaticService()) {
                    tempDirectory = new File(ServerStarter.getRootDirectory(), "" +
                            "static/" + gameServerInformation.getCloudType().getName() + "/" + gameServerInformation.getName());
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

                // Check for needed files
                File serverSoftwareFile = new File(tempDirectory, "server-software.jar");
                File cloudConnectorFile = new File(tempDirectory, "plugins/cloud-connector.jar");

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
                    requestGameServerStartPacket.setErrorMessage("You need to put the File 'cloud-connector.jar' in the plugins folder of your template.");

                    try {
                        FileUtils.deleteDirectory(tempDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    requestGameServerStartPacket.sendBack();
                    return;
                }

                // Start Server as Screen

                // Send packet back
                requestGameServerStartPacket.sendBack();
            }
        }, this.getSelfServiceInformation().getIdentifier().toString());

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceConnectedPacket serviceConnectedPacket = (ServiceConnectedPacket) packet;
            this.getLogger().info("&aService connected: " + serviceConnectedPacket.getServiceInformation().getName() + " (" + serviceConnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().addService(serviceConnectedPacket.getServiceInformation());

            if (Services.SERVER_COORDINATOR.equals(serviceConnectedPacket.getServiceInformation().getService())) {
                this.getCloudTypeRequester().requestCloudTypes();
            }
        }, "ServiceConnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            ServiceDisconnectedPacket serviceDisconnectedPacket = (ServiceDisconnectedPacket) packet;
            this.getLogger().info("&cService disconnected: " + serviceDisconnectedPacket.getServiceInformation().getName() + " (" + serviceDisconnectedPacket.getServiceInformation().getIdentifier().toString() + ")");
            this.getServiceRepository().removeService(serviceDisconnectedPacket.getServiceInformation().getIdentifier());
        }, "ServiceDisconnected");

        this.getPacketListenerRepository().registerListener((packet, hadCallback) -> {
            this.getCloudTypeRequester().requestCloudTypes();
        }, "CloudCoordinatorReloaded");
    }
}
