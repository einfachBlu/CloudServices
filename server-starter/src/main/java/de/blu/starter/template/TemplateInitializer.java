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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
                    "static/" + gameServerInformation.getCloudType().getName() + "/" + gameServerInformation.getName());
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
