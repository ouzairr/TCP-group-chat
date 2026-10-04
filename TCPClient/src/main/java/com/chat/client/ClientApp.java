package com.chat.client;

import com.chat.client.controller.ClientController;
import com.chat.client.model.ClientModel;
import com.chat.client.view.ClientView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point: java TCPClient <ServerIPAddress> <PortNumber>
 * Example: java TCPClient localhost 3000
 */
public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        ClientModel model = new ClientModel();
        ClientView view = new ClientView(primaryStage);
        ClientController controller = new ClientController(model, view);

        // Override host/port from command-line args if provided
        Parameters params = getParameters();
        if (params.getRaw().size() >= 2) {
            try {
                String host = params.getRaw().get(0);
                int port = Integer.parseInt(params.getRaw().get(1));
                controller.overrideServer(host, port);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port argument; using config file.");
            }
        }

        view.setController(controller);
        view.showLoginScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
