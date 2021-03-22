package io.github.mosadie.mcsde.core;

import com.sun.net.httpserver.HttpServer;
import io.github.mosadie.mcsde.core.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MCSDECore {

    private int port;
    private final EffectExecutor executor;

    private HttpServer server;

    public MCSDECore(int port, EffectExecutor executor) {
        this.port = port;
        this.executor = executor;
    }

    public boolean initServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        server.createContext("/", new RootHandler(this));
        server.createContext("/joinserver", new JoinServerHandler(this));
        server.createContext("/setskinlayervisibility", new SkinLayerHandler(this));
        server.createContext("/sendchat", new SendChatMessageHandler(this));
        server.createContext("/receivechat", new ReceiveChatMessageHandler(this));
        server.createContext("/showtitle", new ShowTitleHandler(this));
        server.createContext("/showactionmessage", new ShowActionMessageHandler(this));

        server.setExecutor(null);

        server.start();

        return true;
    }

    public EffectExecutor getExecutor() {
        return executor;
    }

    boolean checkTrust(String id) {
        //TODO add trust system
        return true;
    }
}
