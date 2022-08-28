package xyz.charlie35.mcauth.server.model;

/**
 * @author mikoto
 * @time 2022/8/29
 * Create for mcauth-server
 */
public class TokenPair {
    private final String token;
    private final String refreshToken;

    public TokenPair(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}