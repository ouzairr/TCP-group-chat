package com.chat.client.view;

import com.chat.client.controller.ClientController;
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

/**
 * ClientView: login screen then chat screen.
 * All UI logic only — delegates actions to ClientController.
 */
public class ClientView {

    private final Stage stage;
    private ClientController controller;  // set after construction

    // Chat UI controls
    private TextArea chatArea;
    private TextField inputField;
    private Button sendButton;
    private Label statusLabel;
    private Circle statusDot;

    public ClientView(Stage stage) {
        this.stage = stage;
    }

    public void setController(ClientController controller) {
        this.controller = controller;
    }

    // ─── Login Screen ────────────────────────────────────────────────────────────
    public void showLoginScreen() {
        Label title = new Label("TCP Group Chat");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: #1a237e;");

        Label subtitle = new Label("Enter a username to chat, or leave blank to observe.");
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (leave blank for read-only)");
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        Button connectBtn = new Button("Connect");
        connectBtn.setStyle(
            "-fx-background-color: #1a237e; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-padding: 8 24 8 24; -fx-cursor: hand; -fx-background-radius: 6;"
        );

        Label hint = new Label("Tip: type 'allUsers' to list users, 'bye' to disconnect.");
        hint.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        Runnable doConnect = () -> {
            controller.connect(usernameField.getText().trim());
        };

        connectBtn.setOnAction(e -> doConnect.run());
        usernameField.setOnAction(e -> doConnect.run());

        VBox box = new VBox(14, title, subtitle, usernameField, connectBtn, hint);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(60));
        box.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(box, 480, 340);
        stage.setTitle("TCP Chat – Connect");
        stage.setScene(scene);
        stage.show();
    }

    // ─── Chat Screen ─────────────────────────────────────────────────────────────
    public void showChatScreen(boolean readOnly, String displayName) {
        // Title bar
        Label titleLabel = new Label("TCP Group Chat — " + displayName);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: white;");

        statusDot = new Circle(7, readOnly ? Color.web("#ffd600") : Color.web("#00e676"));
        statusLabel = new Label(readOnly ? "READ-ONLY" : "Online");
        statusLabel.setStyle("-fx-text-fill: " + (readOnly ? "#ffd600" : "#00e676") + "; -fx-font-size: 13px;");

        HBox statusBox = new HBox(6, statusDot, statusLabel);
        statusBox.setAlignment(Pos.CENTER);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox titleBar = new HBox(12, titleLabel, sp, statusBox);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 16, 10, 16));
        titleBar.setStyle("-fx-background-color: #1a237e;");

        // Chat area
        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setStyle(
            "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;" +
            "-fx-control-inner-background: #f8f9ff;"
        );
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // Input row
        inputField = new TextField();
        inputField.setPromptText(readOnly ? "Read-only mode — cannot send messages" : "Type a message...");
        inputField.setDisable(readOnly);
        inputField.setStyle("-fx-font-size: 13px; -fx-padding: 8px;");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        sendButton = new Button("SEND");
        sendButton.setDisable(readOnly);
        sendButton.setStyle(
            "-fx-background-color: #1a237e; -fx-text-fill: white;" +
            "-fx-font-size: 13px; -fx-padding: 8 18 8 18; -fx-cursor: hand; -fx-background-radius: 6;"
        );

        Runnable doSend = () -> {
            String text = inputField.getText().trim();
            if (!text.isBlank()) {
                controller.sendMessage(text);
                inputField.clear();
            }
        };

        sendButton.setOnAction(e -> doSend.run());
        inputField.setOnAction(e -> doSend.run());

        HBox inputRow = new HBox(8, inputField, sendButton);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(8, 10, 10, 10));

        // Root
        VBox center = new VBox(chatArea);
        center.setPadding(new Insets(8, 10, 0, 10));

        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(center);
        root.setBottom(inputRow);
        root.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(root, 620, 480);
        stage.setTitle("TCP Chat – " + displayName);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> controller.disconnect());
    }

    public void appendMessage(String msg) {
        if (chatArea != null) chatArea.appendText(msg + "\n");
    }

    public void setStatus(String status) {
        if (statusLabel == null) return;
        statusLabel.setText(status);
        if ("Online".equals(status)) {
            statusLabel.setStyle("-fx-text-fill: #00e676; -fx-font-size: 13px;");
            statusDot.setFill(Color.web("#00e676"));
        } else if ("READ-ONLY".equals(status)) {
            statusLabel.setStyle("-fx-text-fill: #ffd600; -fx-font-size: 13px;");
            statusDot.setFill(Color.web("#ffd600"));
        } else {
            statusLabel.setStyle("-fx-text-fill: #ff5252; -fx-font-size: 13px;");
            statusDot.setFill(Color.web("#ff5252"));
        }
    }

    public void onDisconnected() {
        setStatus("Offline");
        appendMessage("--- Disconnected from server ---");
        if (sendButton != null) sendButton.setDisable(true);
        if (inputField != null) inputField.setDisable(true);
    }

    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle("Connection Error");
        alert.showAndWait();
    }
}
