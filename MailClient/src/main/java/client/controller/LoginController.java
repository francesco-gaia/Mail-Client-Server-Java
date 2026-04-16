package client.controller;

import client.tasks.CheckTask;
import client.tasks.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class LoginController {
    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    protected void onLoginButtonClick() {
        String email = emailField.getText().trim();
        emailExist(email);
    }

    private void emailExist(String email) {
        Consumer<Boolean> loginHandler = ok -> {
            if(ok) Platform.runLater(() -> openMainView(email));
        };
        Consumer<String> labelUpdate = status -> {
            Platform.runLater(() -> showError(status));
        };
        Task checkTask = new CheckTask(email, labelUpdate, loginHandler);
        Thread checkThread = new Thread(checkTask);
        checkThread.start();
    }

    private void openMainView(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.initData(email);

            Stage stage = (Stage) loginButton.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Mail Client - " + email);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossibile caricare schermata principale");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        emailField.requestFocus();
    }
}