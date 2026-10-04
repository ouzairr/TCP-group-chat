package com.chat.client.controller;

import com.chat.client.model.ClientModel;
import com.chat.client.view.ClientView;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ClientController wires the ClientModel (logic) to the ClientView (UI).
 * Loads connection config from client.properties.
 */
public class ClientController {

    private final ClientModel model;
    private final ClientView view;

    private String serverHost;
    private int serverPort;

    public ClientController(ClientModel model, ClientView view) {
        this.model = model;
        this.view = view;
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/client.properties")) {
            if (is != null) props.load(is);
        } catch (IOException e) {
            System.err.println("Could not load client.properties, using defaults.");
        }
        serverHost = props.getProperty("server.host", "localhost");
        serverPort = Integer.parseInt(props.getProperty("server.port", "3000"));
    }

    /**
     * Called from the login screen. username may be blank → read-only mode.
     */
    public void connect(String username) {
        // Override host/port from command-line args if provided
        model.setOnMessageReceived(msg -> Platform.runLater(() -> view.appendMessage(msg)));
        model.setOnStatusChange(status -> Platform.runLater(() -> view.setStatus(status)));
        model.setOnDisconnected(() -> Platform.runLater(view::onDisconnected));

        try {
            model.connect(serverHost, serverPort, username);
            Platform.runLater(() -> view.showChatScreen(model.isReadOnly(), username.isBlank() ? "Guest" : username));
        } catch (IOException e) {
            Platform.runLater(() -> view.showError("Cannot connect to server: " + e.getMessage()));
        }
    }

    public void overrideServer(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
    }

    public void sendMessage(String text) {
        if (text.equalsIgnoreCase("end") || text.equalsIgnoreCase("bye")) {
            model.disconnect();
        } else {
            model.sendMessage(text);
            // Echo own message locally
            if (!model.isReadOnly()) {
                view.appendMessage("You: " + text);
            }
        }
    }

    public void disconnect() {
        model.disconnect();
    }
}
