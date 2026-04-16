package server.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import server.ServerManager;

public class ServerController {

    @FXML
    private ListView<String> logListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnStop;

    private ServerManager serverManager;
    private boolean isRunning = false;

    // Metodo chiamato dal MainServer per passare il modello
    public void setModel(ServerManager serverManager) {
        this.serverManager = serverManager;
        // Collega la lista grafica con i log del server
        if (this.logListView != null) {
            this.logListView.setItems(serverManager.getLogList());

            // Auto-scroll verso il basso quando arriva un nuovo log
            serverManager.getLogList().addListener((javafx.collections.ListChangeListener<String>) c -> {
                Platform.runLater(() -> logListView.scrollTo(serverManager.getLogList().size() - 1));
            });
        }
    }

    @FXML
    public void onStartClick() {
        if (!isRunning && serverManager != null) {
            serverManager.startServer(8189);
            updateStatus(true);
        }
    }

    @FXML
    public void onStopClick() {
        if (isRunning && serverManager != null) {
            serverManager.stopServer();
            updateStatus(false);
        }
    }

    private void updateStatus(boolean running) {
        this.isRunning = running;
        if (running) {
            statusLabel.setText("Stato: ONLINE (Porta 8189)");
            statusLabel.setTextFill(Color.web("#2ecc71")); // Verde
            btnStart.setDisable(true);
            btnStop.setDisable(false);
        } else {
            statusLabel.setText("Stato: OFFLINE");
            statusLabel.setTextFill(Color.web("#e74c3c")); // Rosso
            btnStart.setDisable(false);
            btnStop.setDisable(true);
        }
    }
}