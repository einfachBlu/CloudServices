package de.blu.coordinator.printer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import de.blu.common.repository.CloudTypeRepository;
import lombok.Getter;

import java.util.Collections;

@Singleton
@Getter
public final class CloudTypePrinter {

    @Inject
    private CloudTypeRepository cloudTypeRepository;

    public void printAll() {
        System.out.println("&eCloudTypes (&c" + this.getCloudTypeRepository().getCloudTypes().size() + "&e):");
        System.out.println("");
        for (CloudType cloudType : this.getCloudTypeRepository().getCloudTypes()) {
            this.print(cloudType);
        }
    }

    public void print(CloudType cloudType) {
        System.out.println("&e" + cloudType.getName() + " &r{");
        System.out.println(this.getTabString(1) + "&etype: &r" + cloudType.getType().name());
        System.out.println(this.getTabString(1) + "&etemplatePath: &r" + cloudType.getTemplatePath());

        if (!cloudType.getType().equals(CloudType.Type.TEMPLATE)) {
            System.out.println(this.getTabString(1) + "&eminOnlineServers: &r" + cloudType.getMinOnlineServers());
            System.out.println(this.getTabString(1) + "&emaxOnlineServers: &r" + cloudType.getMaxOnlineServers());
            System.out.println(this.getTabString(1) + "&eportStart: &r" + cloudType.getPortStart());
            System.out.println(this.getTabString(1) + "&eportEnd: &r" + cloudType.getPortEnd());
            System.out.println(this.getTabString(1) + "&epriority: &r" + cloudType.getPriority());
            System.out.println(this.getTabString(1) + "&ememory: &r" + cloudType.getMemory());
            if (cloudType.getType().equals(CloudType.Type.BUKKIT)) {
                System.out.println(this.getTabString(1) + "&epermission: &r" + cloudType.getPermission());
            }
            System.out.println(this.getTabString(1) + "&estaticService: &r" + cloudType.isStaticService());

            if (cloudType.getHosts().size() == 0) {
                System.out.println(this.getTabString(1) + "&ehosts: &r[]");
            } else {
                System.out.println(this.getTabString(1) + "&ehosts: &r[");
                for (String host : cloudType.getHosts()) {
                    System.out.println(this.getTabString(2) + "- " + host);
                }
                System.out.println(this.getTabString(1) + "]");
            }
        }

        if (cloudType.getInheritances().size() == 0) {
            System.out.println(this.getTabString(1) + "&einheritances: &r[]");
        } else {
            System.out.println(this.getTabString(1) + "&einheritances: &r[");
            for (String inheritance : cloudType.getInheritances()) {
                System.out.println(this.getTabString(2) + "- " + inheritance);
            }
            System.out.println(this.getTabString(1) + "]");
        }

        if (!cloudType.getType().equals(CloudType.Type.TEMPLATE)) {
            if (cloudType.getType().equals(CloudType.Type.BUNGEECORD)) {
                if (cloudType.getProxyFallbackPriorities().size() == 0) {
                    System.out.println(this.getTabString(1) + "&eproxyFallbackPriorities: &r[]");
                } else {
                    System.out.println(this.getTabString(1) + "&eproxyFallbackPriorities: &r[");
                    for (String proxyFallbackPriority : cloudType.getProxyFallbackPriorities()) {
                        System.out.println(this.getTabString(2) + "- " + proxyFallbackPriority);
                    }
                    System.out.println(this.getTabString(1) + "]");
                }
            }

            if (cloudType.getJavaParameters().size() == 0) {
                System.out.println(this.getTabString(1) + "&ejavaParameters: &r[]");
            } else {
                System.out.println(this.getTabString(1) + "&ejavaParameters: &r[");
                for (String javaParameter : cloudType.getJavaParameters()) {
                    System.out.println(this.getTabString(2) + "- " + javaParameter);
                }
                System.out.println(this.getTabString(1) + "]");
            }

            if (cloudType.getServerParameters().size() == 0) {
                System.out.println(this.getTabString(1) + "&eserverParameters: &r[]");
            } else {
                System.out.println(this.getTabString(1) + "&eserverParameters: &r[");
                for (String serverParameter : cloudType.getServerParameters()) {
                    System.out.println(this.getTabString(2) + "- " + serverParameter);
                }
                System.out.println(this.getTabString(1) + "]");
            }
        }
        System.out.println("}");
    }

    private String getTabString(int tabAmount) {
        String tabString = "  ";
        return String.join("", Collections.nCopies(tabAmount, tabString));
    }
}
