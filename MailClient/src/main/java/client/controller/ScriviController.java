package client.controller;

import client.model.Email;
import client.tasks.SendTask;
import client.tasks.Task;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ScriviController {

    @FXML private Label titolo;
    @FXML private TextField textRecipient;
    @FXML private TextField textSubject;
    @FXML private TextArea textMessage;
    @FXML private Label errorLabel;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    private String senderEmail;
    private ExecutorService sendPool;

    public void setSendPool(ExecutorService sendPool) {
        this.sendPool = sendPool;
    }

    public void setSender(String email) {
        this.senderEmail = email;
    }

    public void setFields(String recipient, String subject) {
        if(recipient != null) textRecipient.setText(recipient);
        if(subject != null) textSubject.setText(subject);
    }
    @FXML
    protected void handleSend() {
        String re = textRecipient.getText();
        String subject = textSubject.getText();
        String text = textMessage.getText();

        if (re.isEmpty()) {
            showError("Inserisci almeno un destinatario!");
            return;
        }

        if(subject.isEmpty()){
            showError("L'oggetto non deve essere vuoto");
            return;
        }
        String[] listaRe = re.split("[,\\s]+");
        List<String> recipients = new ArrayList<>();
        for (String email : listaRe) {
            email = email.trim();
            if (email.isEmpty()) continue; // Salta spazi vuoti extra
            if (!PATTERN.matcher(email).matches()) {
                showError("Email non valida: " + email);
                return;
            }
            recipients.add(email);
        }
        if (recipients.isEmpty()) {
            showError("Nessun destinatario valido trovato.");
            return;
        }
        System.out.println("INVIO MAIL DA: " + senderEmail);
        Email toSend = new Email(senderEmail, senderEmail, recipients, subject, text, LocalDate.now().toString());
        Task send = getTask(toSend);
        sendPool.execute(send);
    }

    private Task getTask(Email toSend) {
        Consumer<String> sendHandler = status -> {
            Platform.runLater(() -> showError(status));
        };
        Consumer<Boolean> disableHandler = ok -> {
            if(ok) {
                Platform.runLater(() -> {
                    showSuccess("Mail inviata con successo!");
                    textRecipient.setDisable(true);
                    textSubject.setDisable(true);
                    textMessage.setDisable(true);

                    PauseTransition delay = new PauseTransition(Duration.seconds(2));
                    delay.setOnFinished(_ -> closeWindow());
                    delay.play();
                });
            }
        };
        return new SendTask(senderEmail, toSend, sendHandler, disableHandler);
    }

    public void setWindowTitle(String title) {
        if (titolo != null) {
            titolo.setText(title);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) textRecipient.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void showSuccess(String msg) {
        errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    public void inoltrato(String message){
        if(message != null && !message.isEmpty()){
            textMessage.setText("\n\n--------------------\n"+message);
            textMessage.positionCaret(0);
        }
    }
}