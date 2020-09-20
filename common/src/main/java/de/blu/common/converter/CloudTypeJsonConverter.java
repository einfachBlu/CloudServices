package de.blu.common.converter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import de.blu.common.data.CloudType;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Singleton
@Getter
public final class CloudTypeJsonConverter {

    @Inject
    private Injector injector;

    public CloudType fromJson(String cloudTypeName, JSONObject data) {
        CloudType cloudType = this.getInjector().getInstance(CloudType.class);
        cloudType.setName(cloudTypeName);
        String typeString = (String) data.get("type");

        CloudType.Type type = CloudType.Type.valueOf(typeString);
        cloudType.setType(type);

        // Inheritances
        JSONArray inheritances = (JSONArray) data.get("inheritances");
        for (Object inheritance : inheritances) {
            cloudType.getInheritances().add(String.valueOf(inheritance));
        }

        // TemplatePath
        cloudType.setTemplatePath(String.valueOf(data.get("templatePath")));

        if (!cloudType.getType().equals(CloudType.Type.TEMPLATE)) {
            boolean staticService = (boolean) data.get("staticService");
            long minOnlineServers = (long) data.get("minOnlineServers");
            long maxOnlineServers = (long) data.get("maxOnlineServers");
            long portStart = (long) data.get("portStart");
            long portEnd = (long) data.get("portEnd");
            long memory = (long) data.get("memory");
            long priority = (long) data.get("priority");
            String permission = (String) data.get("permission");
            JSONArray hosts = (JSONArray) data.get("hosts");
            JSONArray javaParameters = (JSONArray) data.get("javaParameters");
            JSONArray serverParameters = (JSONArray) data.get("serverParameters");

            cloudType.setStaticService(staticService);
            cloudType.setMinOnlineServers((int) minOnlineServers);
            cloudType.setMaxOnlineServers((int) maxOnlineServers);
            cloudType.setPortStart((int) portStart);
            cloudType.setPortEnd((int) portEnd);
            cloudType.setMemory((int) memory);
            cloudType.setPriority((int) priority);
            cloudType.setPermission(permission);

            for (Object hostName : hosts) {
                cloudType.getHosts().add(String.valueOf(hostName));
            }
            for (Object javaParameter : javaParameters) {
                cloudType.getJavaParameters().add(String.valueOf(javaParameter));
            }
            for (Object serverParameter : serverParameters) {
                cloudType.getServerParameters().add(String.valueOf(serverParameter));
            }
        }

        switch (type) {
            case BUKKIT:
                cloudType = this.loadBukkitCloudType(cloudType, data);
                break;
            case BUNGEECORD:
                cloudType = this.loadBungeeCordCloudType(cloudType, data);
                break;
            case TEMPLATE:
                cloudType = this.loadTemplateCloudType(cloudType, data);
                break;
            default:
                break;
        }

        return cloudType;
    }

    private CloudType loadTemplateCloudType(CloudType cloudType, JSONObject data) {
        cloudType.setType(CloudType.Type.TEMPLATE);
        // Nothing specific
        return cloudType;
    }

    private CloudType loadBukkitCloudType(CloudType cloudType, JSONObject data) {
        // Nothing specific
        return cloudType;
    }

    private CloudType loadBungeeCordCloudType(CloudType cloudType, JSONObject data) {
        cloudType.setType(CloudType.Type.BUNGEECORD);

        JSONArray fallbackPriorities = (JSONArray) data.get("fallbackPriorities");

        for (Object fallbackCloudTypeName : fallbackPriorities) {
            cloudType.getProxyFallbackPriorities().add(String.valueOf(fallbackCloudTypeName));
        }

        return cloudType;
    }
}
