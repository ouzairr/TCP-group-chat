package com.chat.server.model;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * ServerModel handles all socket communication and business logic.
 * Completely independent of the JavaFX View layer (Separation of Concerns).
 */
public class ServerModel {

    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private boolean running = false;

    // Callbacks to notify the View without depending on it
    private Consumer<String> onLog;
    private Consumer<String> onClientConnected;
    private Consumer<String> onClientDisconnected;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void setOnLog(Consumer<String> cb) { this.onLog = cb; }
    public void setOnClientConnected(Consumer<String> cb) { this.onClientConnected = cb; }
    public void setOnClientDisconnected(Consumer<String> cb) { this.onClientDisconnected = cb; }

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        log("Server Started on port " + port);
        log("Waiting for clients...");

        Thread acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    Thread t = new Thread(handler);
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    if (running) log("Error accepting client: " + e.getMessage());
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}
    }

    private void log(String msg) {
        if (onLog != null) onLog.accept("[" + LocalTime.now().format(TIME_FMT) + "] " + msg);
    }

    private void broadcast(String message, String senderUsername) {
        for (ClientHandler handler : clients.values()) {
            if (!handler.getUsername().equals(senderUsername)) {
                handler.sendMessage(message);
            }
        }
    }

    private void broadcastToAll(String message) {
        for (ClientHandler handler : clients.values()) {
            handler.sendMessage(message);
        }
    }

    public List<String> getConnectedUsers() {
        return new ArrayList<>(clients.keySet());
    }

    // ─── Inner class: handles one client connection on its own thread ───────────
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private String username = "";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        String getUsername() { return username; }

        void sendMessage(String msg) {
            if (out != null) out.println(msg);
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                this.out = writer;

                // First message from client is the username
                username = in.readLine();
                if (username == null) return;

                boolean readOnly = username.isBlank();
                if (readOnly) {
                    username = "Guest_" + socket.getPort();
                    sendMessage("SERVER:READ_ONLY");
                } else {
                    sendMessage("SERVER:WELCOME:" + username);
                    clients.put(username, this);
                    log("Welcome " + username + " [" + socket.getInetAddress().getHostAddress() + "]");
                    if (onClientConnected != null) onClientConnected.accept(username);
                    broadcastToAll("SERVER:JOIN:" + username);
                }

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("end") || line.equalsIgnoreCase("bye")) {
                        break;
                    } else if (line.equalsIgnoreCase("allUsers")) {
                        String userList = String.join(", ", clients.keySet());
                        sendMessage("SERVER:USERS:" + userList);
                    } else {
                        if (!readOnly) {
                            String formatted = "[" + LocalTime.now().format(TIME_FMT) + "] "
                                    + username + ": " + line;
                            log(username + " says: " + line);
                            broadcast(formatted, username);
                        }
                    }
                }

                // Disconnection
                clients.remove(username);
                log(username + " disconnected.");
                if (onClientDisconnected != null) onClientDisconnected.accept(username);
                broadcastToAll("SERVER:LEAVE:" + username);

            } catch (IOException e) {
                clients.remove(username);
                log(username + " lost connection.");
                if (onClientDisconnected != null && !username.isBlank()) {
                    onClientDisconnected.accept(username);
                    broadcastToAll("SERVER:LEAVE:" + username);
                }
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }
    }
}
