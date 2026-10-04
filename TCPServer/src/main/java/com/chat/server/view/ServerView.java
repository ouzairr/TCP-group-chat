package com.chat.server.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.Random;

/**
 * ServerView: JavaFX UI for the server application.
 * Only handles display — no business logic.
 */
public class ServerView {

    private final TextArea logArea = new TextArea();
    private final ListView<String> userListView = new ListView<>();
    private final Random random = new Random();

    // Random pastel colors for user list cells
    private static final String[] PASTEL_COLORS = {
        "#FFB3BA", "#FFDFBA", "#FFFFBA", "#BAFFC9",
        "#BAE1FF", "#E8BAFF", "#FFC8BA", "#BAF5FF"
    };

    public void buildAndShow(Stage stage) {
        // ── Title Bar ──────────────────────────────────────────────────
        Label titleLabel = new Label("TCP Chat Server");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: white;");

        Circle statusDot = new Circle(8, Color.web("#00e676"));

        Label statusLabel = new Label("Running");
        statusLabel.setStyle("-fx-text-fill: #00e676; -fx-font-size: 13px;");

        HBox statusBox = new HBox(6, statusDot, statusLabel);
        statusBox.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox titleBar = new HBox(12, titleLabel, spacer, statusBox);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(12, 16, 12, 16));
        titleBar.setStyle("-fx-background-color: #1a237e;");

        // ── Log Area ───────────────────────────────────────────────────
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle(
            "-fx-font-family: 'Courier New'; -fx-font-size: 13px;" +
            "-fx-control-inner-background: #1e1e2e; -fx-text-fill: #cdd6f4;"
        );
        logArea.setPrefHeight(400);

        Label logLabel = new Label("Server Log");
        logLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a237e;");

        VBox logBox = new VBox(6, logLabel, logArea);
        logBox.setPadding(new Insets(10));

        // ── User List ──────────────────────────────────────────────────
        userListView.setPrefWidth(200);
        userListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);
                if (empty || username == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("● " + username);
                    String color = PASTEL_COLORS[Math.abs(username.hashCode()) % PASTEL_COLORS.length];
                    setStyle("-fx-background-color: " + color + "; -fx-font-size: 13px; -fx-padding: 6px;");
                }
            }
        });

        Label usersLabel = new Label("Connected Users");
        usersLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1a237e;");

        VBox usersBox = new VBox(6, usersLabel, userListView);
        usersBox.setPadding(new Insets(10));
        VBox.setVgrow(userListView, Priority.ALWAYS);

        // ── Main Layout ────────────────────────────────────────────────
        HBox centerBox = new HBox(10, logBox, usersBox);
        HBox.setHgrow(logBox, Priority.ALWAYS);
        centerBox.setPadding(new Insets(0, 10, 10, 10));

        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(centerBox);
        root.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(root, 780, 500);
        stage.setTitle("TCP Chat Server");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> System.exit(0));
        stage.show();
    }

    public void appendLog(String message) {
        logArea.appendText(message + "\n");
    }

    public void addUser(String username) {
        if (!userListView.getItems().contains(username)) {
            userListView.getItems().add(username);
        }
    }

    public void removeUser(String username) {
        userListView.getItems().remove(username);
    }
}
