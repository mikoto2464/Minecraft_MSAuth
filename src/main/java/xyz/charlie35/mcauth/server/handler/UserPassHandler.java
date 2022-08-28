package xyz.charlie35.mcauth.server.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import xyz.charlie35.mcauth.server.exception.AuthenticationException;
import xyz.charlie35.mcauth.server.model.*;
import xyz.charlie35.mcauth.server.requester.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UserPassHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        if("GET".equals(httpExchange.getRequestMethod())) {
            Map<String, String> requestParameters = queryToMap(httpExchange.getRequestURI().getQuery());
            if (!requestParameters.containsKey("user") || !requestParameters.containsKey("pass")) {
                String httpResponse = "400 Bad request";
                httpExchange.sendResponseHeaders(400, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
                return;
            }

            String client = httpExchange.getRemoteAddress().getHostString();
            if (httpExchange.getRequestHeaders().containsKey("X-Forwarded-For"))
                client = httpExchange.getRequestHeaders().getFirst("X-Forwarded-For").split(",")[0];

            try {
                String code = UserAuthRequester.getTokenFor(requestParameters.get("user"), requestParameters.get("pass"), client);

                System.out.println("> Requesting TOKEN for "+client);
                TokenPair authToken = MsTokenRequester.getForUserPass(code);

                System.out.println("> Authenticating with XBL for " + client);
                XBLToken xblToken = XBLTokenRequester.getForUserPass(authToken.getToken());

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

                httpExchange.getResponseHeaders().add("Content-type","application/json");
                httpExchange.sendResponseHeaders(200, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
            } catch (AuthenticationException e) {
                System.out.println("Auth error for "+client+"! "+e.getMessage());
                String httpResponse = "Authentication error: "+e.getMessage();
                httpExchange.sendResponseHeaders(401, httpResponse.length());
                httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
            }

            String httpResponse = "200 OK";
            httpExchange.sendResponseHeaders(200, httpResponse.length());
            httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
        } else {
            String httpResponse = "400 Bad request";
            httpExchange.sendResponseHeaders(400, httpResponse.length());
            httpExchange.getResponseBody().write(httpResponse.getBytes(StandardCharsets.US_ASCII));
        }
    }

    public Map<String, String> queryToMap(String query) {
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
