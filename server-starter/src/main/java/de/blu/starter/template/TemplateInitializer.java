package de.blu.starter.template;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.config.FileRootConfig;
import de.blu.common.data.CloudType;
import de.blu.common.data.GameServerInformation;
import de.blu.common.repository.CloudTypeRepository;
import de.blu.starter.ServerStarter;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Singleton
@Getter
public final class TemplateInitializer {

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    @Inject
    private FileRootConfig fileRootConfig;

    private File tempDirectory;

    public void createDirectory(GameServerInformation gameServerInformation) {
        this.tempDirectory = new File(ServerStarter.getRootDirectory(), "" +
                "temporary/" + gameServerInformation.getCloudType().getName() + "/" + gameServerInformation.getName() + "_" + gameServerInformation.getUniqueId().toString());

        if (gameServerInformation.getCloudType().isStaticService()) {
            this.tempDirectory = new File(ServerStarter.getRootDirectory(), "" +
                    "static/" + gameServerInformation.getName());
        }

        if (!this.tempDirectory.exists()) {
            this.tempDirectory.mkdirs();
        }
    }

    public void copyTemplates(GameServerInformation gameServerInformation) {
        Collection<CloudType> inheritances = new ArrayList<>();
        this.getInheritancesRecursive(gameServerInformation.getCloudType(), inheritances);

        for (CloudType cloudType : inheritances) {
            this.copyTemplate(cloudType, this.getTempDirectory());
        }
    }

    public void prepareDirectory(GameServerInformation gameServerInformation) {
        switch (gameServerInformation.getCloudType().getType()) {
            case BUKKIT:
                this.prepareBukkitDirectory(gameServerInformation);
                break;
            case BUNGEECORD:
                this.prepareBungeeCordDirectory(gameServerInformation);
                break;
        }
    }

    private void prepareBungeeCordDirectory(GameServerInformation gameServerInformation) {
        File configFile = new File(this.getTempDirectory(), "config.yml");
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            Yaml yaml = new Yaml();
            Map<String, Object> config = (Map<String, Object>) yaml.load(new FileReader(configFile));
            if (config == null) config = new LinkedHashMap<>();
            List<Map<String, Object>> listeners = (List) config.get("listeners");
            if (listeners == null) listeners = new ArrayList<>();
            Map<String, Object> map = listeners.size() == 0 ? new LinkedHashMap<>() : listeners.get(0);
            map.put("force_default_server", false);
            map.put("host", "0.0.0.0:" + gameServerInformation.getPort());
            if (listeners.size() == 0) listeners.add(map);
            FileWriter writer = new FileWriter(configFile);
            config.put("listeners", listeners);
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            new Yaml(dumperOptions).dump(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareBukkitDirectory(GameServerInformation gameServerInformation) {
        File serverProperties = new File(this.getTempDirectory(), "server.properties");

        try {
            if (!serverProperties.exists()) {
                serverProperties.createNewFile();
            }

            Properties properties = new Properties();
            properties.load(new FileReader(serverProperties));

            properties.setProperty("online-mode", "false");
            properties.setProperty("server-name", gameServerInformation.getName());

            properties.store(new FileWriter(serverProperties), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyTemplate(CloudType cloudType, File directory) {
        File cloudTypeTemplateDirectory;

        if (cloudType.getTemplatePath() != null) {
            cloudTypeTemplateDirectory = new File(cloudType.getTemplatePath());
        } else {
            cloudTypeTemplateDirectory = new File(new File(this.getFileRootConfig().getRootFileDirectory()), "Templates/" + cloudType.getName());
        }

        this.copyFilesRecursive(cloudTypeTemplateDirectory, this.getTempDirectory());
    }

    private Collection<CloudType> getInheritancesRecursive(CloudType cloudType, Collection<CloudType> inheritances) {
        for (String cloudTypeInheritanceName : cloudType.getInheritances()) {
            CloudType cloudTypeInheritance = this.getCloudTypeRepository().getCloudTypeByName(cloudTypeInheritanceName);
            this.getInheritancesRecursive(cloudTypeInheritance, inheritances);
        }

        if (inheritances.stream().noneMatch(cloudType1 -> cloudType1.equals(cloudType))) {
            inheritances.add(cloudType);
        }

        return inheritances;
    }

    private void copyFilesRecursive(File from, File to) {
        if (to.exists()) {
            to.mkdirs();
        }

        for (File file : from.listFiles()) {
            if (file.isDirectory()) {
                this.copyFilesRecursive(file, new File(to, file.getName()));
            } else {
                try {
                    FileUtils.copyFile(file, new File(to, file.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
