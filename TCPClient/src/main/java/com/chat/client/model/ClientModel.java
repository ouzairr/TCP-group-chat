package com.chat.client.model;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * ClientModel manages the TCP socket connection and message I/O.
 * Completely independent of the JavaFX View (Separation of Concerns).
 */
public class ClientModel {

    private Socket socket;
    private PrintWriter out;
    private String username;
    private boolean readOnly = false;
    private boolean connected = false;

    // Callbacks → View
    private Consumer<String> onMessageReceived;
    private Consumer<String> onStatusChange;
    private Runnable onDisconnected;

    public void setOnMessageReceived(Consumer<String> cb) { this.onMessageReceived = cb; }
    public void setOnStatusChange(Consumer<String> cb)    { this.onStatusChange = cb; }
    public void setOnDisconnected(Runnable cb)             { this.onDisconnected = cb; }

    public boolean isReadOnly() { return readOnly; }
    public boolean isConnected() { return connected; }
    public String getUsername() { return username; }

    /**
     * Connect to the server, send username, then spin up a listener thread.
     */
    public void connect(String host, int port, String enteredUsername) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        this.username = enteredUsername == null ? "" : enteredUsername.trim();

        // Send username (empty string = read-only)
        out.println(this.username);
        connected = true;

        // Background thread to read server messages
        Thread reader = new Thread(this::listenLoop);
        reader.setDaemon(true);
        reader.start();
    }

    private void listenLoop() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                handleServerMessage(line);
            }
        } catch (IOException e) {
            if (connected) notify("Connection to server lost.");
        } finally {
            connected = false;
            if (onDisconnected != null) onDisconnected.run();
        }
    }

    private void handleServerMessage(String raw) {
        if (raw.startsWith("SERVER:READ_ONLY")) {
            readOnly = true;
            notify("You are in READ-ONLY MODE (no username provided).");
            if (onStatusChange != null) onStatusChange.accept("READ-ONLY");
        } else if (raw.startsWith("SERVER:WELCOME:")) {
            String name = raw.substring("SERVER:WELCOME:".length());
            notify("Welcome to the chat, " + name + "!");
            if (onStatusChange != null) onStatusChange.accept("Online");
        } else if (raw.startsWith("SERVER:JOIN:")) {
            String who = raw.substring("SERVER:JOIN:".length());
            notify("→ " + who + " has joined the chat.");
        } else if (raw.startsWith("SERVER:LEAVE:")) {
            String who = raw.substring("SERVER:LEAVE:".length());
            notify("← " + who + " has left the chat.");
        } else if (raw.startsWith("SERVER:USERS:")) {
            String list = raw.substring("SERVER:USERS:".length());
            notify("Active users: " + list);
        } else {
            notify(raw);
        }
    }

    private void notify(String msg) {
        if (onMessageReceived != null) onMessageReceived.accept(msg);
    }

    public void sendMessage(String text) {
        if (out != null && !text.isBlank()) {
            out.println(text);
        }
    }

    public void disconnect() {
        sendMessage("bye");
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        if (onDisconnected != null) onDisconnected.run();
    }
}
