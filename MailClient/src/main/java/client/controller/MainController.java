package client.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import client.tasks.ConnectionTask;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import client.model.Account;
import client.model.Email;
import client.tasks.RemoveTask;
import client.tasks.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;


public class MainController {

    @FXML
    private Label labelUserAccount;

    @FXML
    private Label labelConnectionStatus;

    @FXML
    private ListView<Email> emailListView;

    private Account clientAccount;
    // Pannello dettagli (inizialmente nascosto)
    @FXML
    private VBox detailsPane;

    @FXML
    private Label labelSender;

    @FXML
    private Label labelSubject;

    @FXML
    private Label labelDate;

    @FXML
    private Label labelStatusMessage;

    @FXML
    private TextArea textMessageBody;

    @FXML
    private Button buttonLogout;

    private static final int nThreads = 1;

    private ScheduledExecutorService scheduledPool;

    private static final long initialScheduleDelay = 0;
    private static final long periodBetweenExecutions = 10;

    private ExecutorService sendPool;

    /**
     * Metodo chiamato automaticamente da JavaFX dopo il caricamento del file FXML.
     * Qui inizializziamo i listener.
     */
    @FXML
    public void initialize() {
        detailsPane.setVisible(false);

        scheduledPool = Executors.newScheduledThreadPool(1);
        sendPool = Executors.newFixedThreadPool(nThreads);

        //implementazione chiusura finestra threadPool.shutdown()
        emailListView.sceneProperty().addListener((_, _, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((_, _, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnCloseRequest(_ -> {
                            System.out.println("Chiusura applicazione...");
                            if (scheduledPool != null) scheduledPool.shutdownNow();
                            if (sendPool != null) sendPool.shutdownNow();
                            Platform.exit();
                            System.exit(0);
                        });
                    }
                });
            }
        });
        //fine shutdown
        emailListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Email email, boolean empty) {
                super.updateItem(email, empty);

                if (empty || email == null) {
                    setText(null);
                } else {
                    setText(email.getSender() + " - " + email.getSubject() + " (" + email.getSendDate() + ")");
                }
            }
        });

        // Gestione del click sulla lista
        emailListView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            if (newValue != null) {
                showEmailDetails(newValue);
            }
        });
    }

    /**
     * Mostra i dettagli della mail selezionata nel pannello di destra.
     */
    private void showEmailDetails(Email email) {
        detailsPane.setVisible(true);
        labelSender.setText(email.getSender());
        labelSubject.setText(email.getSubject());
        labelDate.setText(email.getSendDate());
        textMessageBody.setText(email.getText());
    }

    private void openScriviView(String windowTitle, String re, String subject, String text){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ScriviView.fxml"));
            Parent root = loader.load();
            ScriviController controller = loader.getController();
            controller.setWindowTitle(windowTitle);
            controller.setSender(labelUserAccount.getText());
            controller.setFields(re, subject);
            controller.inoltrato(text);
            controller.setSendPool(sendPool);
            Stage stage = new Stage();
            stage.setTitle(windowTitle);
            stage.setScene(new Scene(root));
            stage.show();
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore nell'apertura della finestra Scrivi: " + e.getMessage());
        }
    }

    @FXML
    protected void handleNewMail() {
        openScriviView("Nuova mail", "", "", "");
    }

    @FXML
    protected void handleReply() {
        Email email = emailListView.getSelectionModel().getSelectedItem();
        if (email != null) {
            String re = email.getSender();
            String subject = "Re: " + email.getSubject();
            String text = "Messaggio di " + re + ":\n" + email.getText();
            openScriviView("Rispondi", re, subject, text);
        }
    }

    @FXML
    protected void handleReplyAll() {
            Email email = emailListView.getSelectionModel().getSelectedItem();

            if (email != null) {
                //Set per evitare doppioni
            java.util.Set<String> re = new java.util.LinkedHashSet<>();

            re.add(email.getSender());
            if (email.getRecipients() != null) {
                re.addAll(email.getRecipients());
            }
            re.remove(clientAccount.getEmail());
            String recipientsString = String.join(", ", re);
            String subject = "Re: " + email.getSubject();
            String text = "Messaggio di " + re + ":\n" + email.getText();
            openScriviView("Rispondi a tutti", recipientsString, subject, text);
        }
    }

    @FXML
    protected void handleForward() {
        Email email = emailListView.getSelectionModel().getSelectedItem();
        if (email != null) {
            String re = email.getSender();
            String subject = "Inoltrato: " + email.getSubject();
            String text = "Messaggio di " + re + ":\n" + email.getText();
            openScriviView("Inoltra", "", subject, text);
        }
    }

    @FXML
    protected void handleDelete() {
        System.out.println("Elimina mail...");
        int selectedIndex = emailListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex >= 0) {
            Email selected = emailListView.getSelectionModel().getSelectedItem();

            clientAccount.removeMail(selected);
            detailsPane.setVisible(false);

            Consumer<String> responseHandler = status -> {
                Platform.runLater(() -> {
                    String msg = status.toLowerCase();

                    if (msg.contains("rimossa") || msg.contains("eliminata") || msg.equals("ok")) {
                        showStatusMessage("Mail eliminata correttamente", "green"); //
                        return;
                    }
                    showStatusMessage("Errore: " + status, "red");
                    clientAccount.addMail(selected); //ripristino mail in caso di errore
                });
            };

            Task removeMail = new RemoveTask(clientAccount.getEmail(), responseHandler, selected.getId());
            sendPool.execute(removeMail);
        }
    }

    @FXML
    protected void handleLogout() {
        sendPool.shutdown();
        scheduledPool.shutdown();
        try {
            // Carica la LoginView
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginView.fxml"));
            Parent root = loader.load();

            // Ottiene la finestra attuale e cambia la scena
            Stage stage = (Stage) buttonLogout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mail Client - Login");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!sendPool.isTerminated())
            sendPool.shutdownNow();
        if(!scheduledPool.isTerminated())
            scheduledPool.shutdownNow();
    }

    private void showStatusMessage(String message, String color) {
        if (labelStatusMessage != null) {
            labelStatusMessage.setText(message);
            labelStatusMessage.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            labelStatusMessage.setVisible(true);
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(event -> labelStatusMessage.setVisible(false));
            delay.play();
        }
    }

    private void startConnectionTask() {
        Consumer<List<Email>> mailsAdd_handler = newMails -> {
            if(!newMails.isEmpty()){
                Platform.runLater(() -> {
                    for (Email mail : newMails) {
                        clientAccount.addMail(mail);
                    }
                });
            }
        };
        Consumer<String> connectionStatus_update = status -> {
            if(status.compareTo(clientAccount.getStatus()) != 0)
                    Platform.runLater(() -> clientAccount.setStatus(status));
        };
        Task connectionTask = new ConnectionTask(clientAccount.getEmail(), true, mailsAdd_handler, connectionStatus_update);
        scheduledPool.scheduleAtFixedRate(connectionTask, initialScheduleDelay , periodBetweenExecutions, TimeUnit.SECONDS);
    }

    // Metodo che il LoginController potrà chiamare per passare l'email dell'utente
    public void initData(String userEmail) {
        this.clientAccount = new Account(userEmail);
        labelUserAccount.setText(userEmail);
        emailListView.setItems(this.clientAccount.getMailList());

        labelConnectionStatus.textProperty().bind(clientAccount.connectionStatusProperty());
        clientAccount.connectionStatusProperty().addListener((_, _, newStatus) -> {
            if ("Connesso".equals(newStatus)) {
                labelConnectionStatus.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;"); 
            } else {
                labelConnectionStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });

        startConnectionTask();
    }
}
