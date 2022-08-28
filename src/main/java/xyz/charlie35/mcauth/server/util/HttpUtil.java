package xyz.charlie35.mcauth.server.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mikoto
 * @time 2022/8/29
 * Create for mcauth-server
 */
public class HttpUtil {
    public static final int TIME_BETWEEN_REQS = 4000;
    public static ConcurrentHashMap<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    public static @NotNull Map<String, String> queryToMap(@NotNull String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            }else{
                result.put(entry[0], "");
            }
        }
        return result;
    }

    public static boolean handleRateLimit(String client) {
        lastRequestTime.putIfAbsent(client, 0L);
        if (System.currentTimeMillis() - lastRequestTime.get(client) <= TIME_BETWEEN_REQS) {
            //lastRequestTime.put(client, System.currentTimeMillis());
            return true;
        }
        lastRequestTime.put(client, System.currentTimeMillis());
        return false;
    }

    public static long timeToNoRateLimit(String client) {
        if (!lastRequestTime.containsKey(client))
            return 0;
        return 4000L-(System.currentTimeMillis() - lastRequestTime.get(client));
    }
}
