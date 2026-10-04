package com.chat.server;

import com.chat.server.controller.ServerController;
import com.chat.server.model.ServerModel;
import com.chat.server.view.ServerView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point: java TCPServer
 * (Command-line args are optional; config comes from server.properties)
 */
public class ServerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ServerModel model = new ServerModel();
        ServerView view = new ServerView();
        ServerController controller = new ServerController(model, view);

        view.buildAndShow(primaryStage);
        controller.startServer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
