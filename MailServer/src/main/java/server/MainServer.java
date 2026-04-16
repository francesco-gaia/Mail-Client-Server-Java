package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import server.controller.ServerController;

public class MainServer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Inizializza il cervello del server (Model)
        ServerManager serverManager = new ServerManager();

        // 2. Carica la grafica (View)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServerView.fxml"));
        Parent root = loader.load();

        // 3. Ottieni il controller e passagli il model
        ServerController controller = loader.getController();
        controller.setModel(serverManager);

        // 4. Avvia il server di rete (Socket) - Lo faremo partire subito
        // Nota: Usiamo una porta standard, es. 8189
        //serverManager.startServer(8189);

        // 5. Mostra la finestra
        stage.setTitle("Mail Server");
        stage.setScene(new Scene(root));

        // Importante: Chiudi tutto quando chiudi la finestra
        stage.setOnCloseRequest(event -> {
            serverManager.stopServer();
            System.exit(0);
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}