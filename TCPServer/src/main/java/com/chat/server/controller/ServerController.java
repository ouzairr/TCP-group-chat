package com.chat.server.controller;

import com.chat.server.model.ServerModel;
import com.chat.server.view.ServerView;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ServerController wires the Model and View together.
 * Reads configuration from server.properties and coordinates callbacks.
 */
public class ServerController {

    private final ServerModel model;
    private final ServerView view;
    private int port;

    public ServerController(ServerModel model, ServerView view) {
        this.model = model;
        this.view = view;
        loadConfig();
        bindCallbacks();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/server.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            System.err.println("Could not load server.properties, using defaults.");
        }
        port = Integer.parseInt(props.getProperty("server.port", "3000"));
    }

    private void bindCallbacks() {
        model.setOnLog(msg -> Platform.runLater(() -> view.appendLog(msg)));
        model.setOnClientConnected(username -> Platform.runLater(() -> view.addUser(username)));
        model.setOnClientDisconnected(username -> Platform.runLater(() -> view.removeUser(username)));
    }

    public void startServer() {
        try {
            model.start(port);
        } catch (IOException e) {
            Platform.runLater(() -> view.appendLog("ERROR: Could not start server - " + e.getMessage()));
        }
    }

    public void stopServer() {
        model.stop();
    }
}
