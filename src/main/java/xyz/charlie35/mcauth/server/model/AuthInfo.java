package xyz.charlie35.mcauth.server.model;

/**
 * @author mikoto
 * @time 2022/8/29
 * Create for mcauth-server
 */
public class AuthInfo {
    private final long time;
    private final String info;
    private final String address;

    public AuthInfo(long time, String info, String address) {
        this.time = time;
        this.info = info;
        this.address = address;
    }

    public long getTime() {
        return time;
    }

    public String getInfo() {
        return info;
    }

    public String getAddress() {
        return address;
    }
}