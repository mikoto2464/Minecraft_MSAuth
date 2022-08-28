package xyz.charlie35.mcauth.server.requester;

import org.json.JSONObject;
import xyz.charlie35.mcauth.server.MsAuthApplication;
import xyz.charlie35.mcauth.server.exception.AuthenticationException;
import xyz.charlie35.mcauth.server.model.TokenPair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static xyz.charlie35.mcauth.server.util.StringUtil.urlEncodeUTF8;

public class MsTokenRequester {
    public static TokenPair getFor(String authCode) throws IOException, AuthenticationException {
        try {
            Map<String, String> arguments = new HashMap<>();
            arguments.put("client_id", MsAuthApplication.CLIENT_ID);
            arguments.put("client_secret", MsAuthApplication.CLIENT_SECRET);
            arguments.put("code", authCode);
            arguments.put("grant_type", "authorization_code");
            arguments.put("redirect_uri", MsAuthApplication.REDIRECT_URI);
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : arguments.entrySet())
                sj.add(urlEncodeUTF8(entry.getKey()) + "="
                        + urlEncodeUTF8(entry.getValue()));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            URL url = new URL("https://login.live.com/oauth20_token.srf");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);

            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

            BufferedReader reader;
            if (http.getResponseCode()!=200) {
                reader = new BufferedReader(new InputStreamReader(http.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            }
            String lines = reader.lines().collect(Collectors.joining());

            JSONObject json = new JSONObject(lines);
            if (json.keySet().contains("error")) {
                throw new AuthenticationException(json.getString("error") + ": " + json.getString("error_description"));
            }
            return new TokenPair(json.getString("access_token"), json.getString("refresh_token"));
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static TokenPair getForUserPass(String authCode) throws IOException, AuthenticationException {
        try {
            Map<String, String> arguments = new HashMap<>();
            arguments.put("client_id", "00000000402b5328");
            arguments.put("code", authCode);
            arguments.put("grant_type", "authorization_code");
            arguments.put("redirect_uri", "https://login.live.com/oauth20_desktop.srf");
            arguments.put("scope","service::user.auth.xboxlive.com::MBI_SSL");
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : arguments.entrySet())
                sj.add(urlEncodeUTF8(entry.getKey()) + "="
                        + urlEncodeUTF8(entry.getValue()));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            URL url = new URL("https://login.live.com/oauth20_token.srf");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);


            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

            BufferedReader reader;
            if (http.getResponseCode()!=200) {
                reader = new BufferedReader(new InputStreamReader(http.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            }
            String lines = reader.lines().collect(Collectors.joining());

            JSONObject json = new JSONObject(lines);
            if (json.keySet().contains("error")) {
                throw new AuthenticationException(json.getString("error") + ": " + json.getString("error_description"));
            }
            return new TokenPair(json.getString("access_token"), json.getString("refresh_token"));
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public static TokenPair refreshFor(String code) throws AuthenticationException, IOException {
        try {
            Map<String, String> arguments = new HashMap<>();
            arguments.put("client_id", MsAuthApplication.CLIENT_ID);
            arguments.put("client_secret", MsAuthApplication.CLIENT_SECRET);
            arguments.put("refresh_token", code);
            arguments.put("grant_type", "refresh_token");
            arguments.put("redirect_uri", MsAuthApplication.REDIRECT_URI);
            StringJoiner sj = new StringJoiner("&");
            for (Map.Entry<String, String> entry : arguments.entrySet())
                sj.add(urlEncodeUTF8(entry.getKey()) + "="
                        + urlEncodeUTF8(entry.getValue()));
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            URL url = new URL("https://login.live.com/oauth20_token.srf");
            URLConnection con = url.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST"); // PUT is another valid option
            http.setDoOutput(true);


            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
            }

            BufferedReader reader;
            if (http.getResponseCode()!=200) {
                reader = new BufferedReader(new InputStreamReader(http.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
            }
            String lines = reader.lines().collect(Collectors.joining());

            JSONObject json = new JSONObject(lines);
            if (json.keySet().contains("error")) {
                throw new AuthenticationException(json.getString("error") + ": " + json.getString("error_description"));
            }
            return new TokenPair(json.getString("access_token"), json.getString("refresh_token"));
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
