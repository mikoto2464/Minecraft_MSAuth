package xyz.charlie35.mcauth.server.util;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mikoto
 * @time 2022/8/29
 * Create for mcauth-server
 */
public class HttpUtil {
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
}
