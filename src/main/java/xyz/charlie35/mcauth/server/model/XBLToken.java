package xyz.charlie35.mcauth.server.model;

/**
 * @author mikoto
 * @time 2022/8/29
 * Create for mcauth-server
 */
public class XBLToken {
    private final String token;
    private final String uhs;
    public XBLToken(String token, String uhs) {
        this.token = token;
        this.uhs = uhs;
    }

    public String getToken() {
        return token;
    }

    public String getUhs() {
        return uhs;
    }
}