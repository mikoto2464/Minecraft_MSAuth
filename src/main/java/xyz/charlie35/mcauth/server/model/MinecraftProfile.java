package xyz.charlie35.mcauth.server.model;

/**
 * @author mikoto
 * @time 2022/8/29
 * Create for mcauth-server
 */
public class MinecraftProfile {
    private final String uuid;
    private final String name;
    private final String skinUrl;

    public MinecraftProfile(String uuid, String name, String skinUrl){
        this.uuid = uuid;
        this.name = name;
        this.skinUrl = skinUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getSkinUrl() {
        return skinUrl;
    }
}