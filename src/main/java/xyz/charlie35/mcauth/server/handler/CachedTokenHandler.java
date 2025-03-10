package xyz.charlie35.mcauth.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;
import xyz.charlie35.mcauth.server.MsAuthApplication;
import xyz.charlie35.mcauth.server.model.AuthInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import static xyz.charlie35.mcauth.server.util.HttpUtil.*;

public class CachedTokenHandler implements HttpHandler {
    @Override
    public void handle(@NotNull HttpExchange httpExchange) throws IOException {
        if ("GET".equals(httpExchange.getRequestMethod())) {
            Map<String, String> requestParameters = queryToMap(httpExchange.getRequestURI().getQuery());
            if (!requestParameters.containsKey("uid")) {
                String httpResponse = "400 Bad request";
                httpExchange.sendResponseHeaders(400, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            String client = httpExchange.getRemoteAddress().getHostString();
            if (httpExchange.getRequestHeaders().containsKey("X-Forwarded-For"))
                client = httpExchange.getRequestHeaders().getFirst("X-Forwarded-For").split(",")[0];

            int ttnr = (int) timeToNoRateLimit(client);
            if (handleRateLimit(client)) {
                String httpResponse = "429 Ratelimited -- come back in "+ttnr+"ms";
                httpExchange.sendResponseHeaders(429, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            String uid_ = requestParameters.get("uid");
            if (!uid_.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                String httpResponse = "400 Bad request - Invalid UUID";
                httpExchange.sendResponseHeaders(400, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            UUID uid = UUID.fromString(uid_);

            System.out.println("> Request auth for "+client);

            if (!MsAuthApplication.authCache.containsKey(uid)){
                String _404 = "404 Not found";
                httpExchange.sendResponseHeaders(404, _404.length());
                httpExchange.getResponseBody().write(_404.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            AuthInfo authInfo = MsAuthApplication.authCache.get(uid);
            if (!authInfo.getAddress().equals(client)) {
                String _401 = "401 Unauthorized";
                httpExchange.sendResponseHeaders(401, _401.length());
                httpExchange.getResponseBody().write(_401.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            if (System.currentTimeMillis() - authInfo.getTime() > MsAuthApplication.TOKEN_STORE_TIME_MS) {
                MsAuthApplication.authCache.remove(uid);
                String _404 = "404 Not found";
                httpExchange.sendResponseHeaders(404, _404.length());
                httpExchange.getResponseBody().write(_404.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            MsAuthApplication.authCache.remove(uid);

            String resp = authInfo.getInfo();
            httpExchange.sendResponseHeaders(200, resp.length());
            httpExchange.getResponseBody().write(resp.getBytes(StandardCharsets.US_ASCII));
        }
    }
}
