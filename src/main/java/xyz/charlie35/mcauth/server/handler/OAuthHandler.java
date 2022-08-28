package xyz.charlie35.mcauth.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import xyz.charlie35.mcauth.server.MsAuthApplication;
import xyz.charlie35.mcauth.server.model.*;
import xyz.charlie35.mcauth.server.requester.XBLTokenRequester;
import xyz.charlie35.mcauth.server.requester.XSTSTokenRequester;
import xyz.charlie35.mcauth.server.exception.AuthenticationException;
import xyz.charlie35.mcauth.server.requester.MinecraftTokenRequester;
import xyz.charlie35.mcauth.server.requester.MsTokenRequester;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static xyz.charlie35.mcauth.server.MsAuthApplication.authCache;

public class OAuthHandler implements HttpHandler {


    @Override
    public void handle(@NotNull HttpExchange httpExchange) throws IOException {
        if("GET".equals(httpExchange.getRequestMethod())) {
            Map<String, String> requestParameters = queryToMap(httpExchange.getRequestURI().getQuery());
            if (!requestParameters.containsKey("code") || !requestParameters.containsKey("state")) {
                String httpResponse = "400 Bad request";
                httpExchange.sendResponseHeaders(400, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            String code = requestParameters.get("code");
            String state = requestParameters.get("state");

            boolean reauth = requestParameters.containsKey("reauth") && Objects.equals(requestParameters.get("reauth"), "true");

            UUID uid = null;
            if (state.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                uid = UUID.fromString(state);
            }

            String client = httpExchange.getRemoteAddress().getHostString();
            if (httpExchange.getRequestHeaders().containsKey("X-Forwarded-For"))
                client = httpExchange.getRequestHeaders().getFirst("X-Forwarded-For").split(",")[0];

            int ttnr = (int) MsAuthApplication.timeToNoRateLimit(client);
            if (MsAuthApplication.handleRateLimit(client)) {
                String httpResponse = "429 Ratelimited -- come back in "+ttnr+"ms";
                httpExchange.sendResponseHeaders(429, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            try {
                TokenPair authToken;
                if (reauth) {
                    System.out.println("> Refreshing TOKEN for " + client);
                    authToken = MsTokenRequester.refreshFor(code);
                }else {
                    System.out.println("> Requesting TOKEN for " + client);
                    authToken = MsTokenRequester.getFor(code);
                }

                System.out.println("> Authenticating with XBL for " + client);
                XBLToken xblToken = XBLTokenRequester.getFor(authToken.getToken());

                System.out.println("> Authenticating with XSTS for " + client);
                XSTSToken xstsToken = XSTSTokenRequester.getFor(xblToken.getToken());

                System.out.println("> Authenticating with Minecraft for " + client);
                MinecraftToken minecraftToken = MinecraftTokenRequester.getFor(xstsToken);

                System.out.println("> Checking ownership and getting profile for "+client);
                MinecraftTokenRequester.checkAccount(minecraftToken);
                MinecraftProfile minecraftProfile = MinecraftTokenRequester.getProfile(minecraftToken);

                JSONObject authResult = new JSONObject();
                authResult.put("access_token", minecraftToken.getAccessToken());
                authResult.put("refresh_token", authToken.getRefreshToken());
                authResult.put("uuid", minecraftProfile.getUuid());
                authResult.put("name", minecraftProfile.getName());
                authResult.put("skin", minecraftProfile.getSkinUrl());

                String httpResponse = authResult.toString();

                if (uid!=null) {
                    System.out.println("> Cached auth for "+client);
                    authCache.put(uid, new AuthInfo(System.currentTimeMillis(), httpResponse, client));
                }

                httpExchange.getResponseHeaders().add("Content-type","application/json");
                httpExchange.sendResponseHeaders(200, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
            } catch (AuthenticationException e) {
                System.out.println("Auth error for "+client+"! "+e.getMessage());
                String httpResponse = "Authentication error: "+e.getMessage();
                httpExchange.sendResponseHeaders(401, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
            }
        } else {
            String httpResponse = "400 Bad request";
            httpExchange.sendResponseHeaders(400, httpResponse.length());
            httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
        }
    }

    public Map<String, String> queryToMap(@NotNull String query) {
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
