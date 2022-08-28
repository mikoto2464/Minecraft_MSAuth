package xyz.charlie35.mcauth.server.model;

/**
 * @author mikoto
 * @time 2022/8/29
 * Create for mcauth-server
 */
public class MinecraftToken {
    private final String accessToken;
    private final String userName;

    public MinecraftToken(String accessToken, String userName) {
        this.accessToken = accessToken;
        this.userName = userName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserName() {
        return userName;
    }
}
