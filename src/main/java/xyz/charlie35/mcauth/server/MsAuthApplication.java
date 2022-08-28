package xyz.charlie35.mcauth.server;

import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import xyz.charlie35.mcauth.server.handler.CachedTokenHandler;
import xyz.charlie35.mcauth.server.handler.OAuthHandler;
import xyz.charlie35.mcauth.server.handler.UserPassHandler;
import xyz.charlie35.mcauth.server.model.AuthInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MsAuthApplication {
    public static ConcurrentHashMap<UUID, AuthInfo> authCache = new ConcurrentHashMap<>();
    public static final int THREADS = 50;
    public static String CLIENT_ID;
    public static String CLIENT_SECRET;
    public static String REDIRECT_URI;
    public static int PORT = 80;
    public static final int TOKEN_STORE_TIME_MS = 30 * 1000;

    public static void main(String @NotNull [] args) throws IOException {
        System.out.println("Starting Minecraft auth webserver");
        System.out.println("-- Copyright charlie353535");
        System.out.println("-- Modify mikoto2464");

        if (args.length < 3) {
            throw new NullPointerException("No such arguments." +
                    "Arguments should be like: <client ID> <client secret> <redirect URI> <port>");
        } else {
            CLIENT_ID = args[0];
            CLIENT_SECRET = args[1];
            REDIRECT_URI = args[2];
        }

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS);

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", Integer.parseInt(args[3])), 0);
        server.createContext("/auth", new OAuthHandler());
        server.createContext("/userPass", new UserPassHandler());
        server.createContext("/get", new CachedTokenHandler());
        server.setExecutor(threadPoolExecutor);
        server.start();
        System.out.println("Server has already started on port " + PORT + " [" + REDIRECT_URI + "]");
    }
}
