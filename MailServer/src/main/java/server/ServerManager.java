package server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import server.model.Account;
import server.model.Email;
import server.tasks.SchedulerTask;

public class ServerManager {

    private static final String DATA_FILE = "MailServer/src/main/resources/mail_data.json";

    private List<Account> accounts;
    private final ObservableList<String> logList;

    private ExecutorService threadPool;
    private SchedulerTask scheduler;
    private ServerSocket serverSocket;

    public ServerManager() {
        this.logList = FXCollections.observableArrayList();
        this.accounts = new ArrayList<>();
        loadData();
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newFixedThreadPool(10);

            scheduler = new SchedulerTask(this, serverSocket, threadPool);
            new Thread(scheduler).start();

            log("SERVER ONLINE (Porta " + port + ")");

        } catch (IOException e) {
            log("Errore Start: " + e.getMessage());
        }
    }

    public void stopServer() {
        if (scheduler != null) scheduler.stop();

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) { /* Ignora */ }

        saveData();
        log("SERVER OFFLINE");
    }

    // --- LOGICA ---

    public synchronized Account getAccount(String email) {
        for (Account acc : accounts) {
            if (acc.getUsername().equals(email)) {
                return acc;
            }
        }
        return null;
    }

    // METODO PER INVIARE EMAIL (Con ID automatico server-side)
    public synchronized boolean dispatchEmail(Email email) {
        boolean delivered = false;

        for (String recipient : email.getRecipients()) {
            Account acc = getAccount(recipient);
            if (acc != null) {
                email.setRead(false);
                acc.addEmail(email);
                delivered = true;
            }
        }

        if (delivered) {
            saveData();
            log("[MSG] " + email.getSender() + " -> " + email.getRecipients() + " (ID: " + email.getId() + ")");
        } else {
            log("[ERR] Consegna fallita da " + email.getSender() + ": destinatari non trovati.");
        }
        return delivered;
    }

    // --- METODO PER IL PROTOCOLLO REMOVE ---
    // Ritorna: 1 = OK, -1 = Utente non trovato, 0 = Mail non trovata
    public synchronized int removeEmail(String userEmail, String emailId) {
        Account acc = getAccount(userEmail);
        if (acc == null) return -1; // ERR USER_NOT_RECOGNIZED

        // Cerca la mail con quell'ID e rimuovila
        boolean removed = acc.getInbox().removeIf(email -> email.getId().equals(emailId));

        if (removed) {
            saveData();
            log("[DEL] Mail rimossa da " + userEmail);
            return 1; // OK
        }
        return 0; // ERR MAIL_NOT_FOUND
    }

    // --- PERSISTENZA ---

    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            throw new RuntimeException("File json non trovato"+file.getAbsolutePath());
        }

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Account>>(){}.getType();
            this.accounts = gson.fromJson(reader, listType);

            if (this.accounts == null) throw new RuntimeException("Errore lettura da file json");
            else log("[SYS] Utenti caricati: " + accounts.size());

        } catch (IOException e) {
            log("[ERR] JSON corrotto, reset dati.");
            //initializeDummyData();
        }
    }

    // Ora è public così WorkTask può chiamarlo dopo il NEW
    public void saveData() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(accounts, writer);
        } catch (IOException e) {
            log("[ERR] Salvataggio: " + e.getMessage());
        }
    }

    /*private void initializeDummyData() {
        accounts = new ArrayList<>();
        accounts.add(new Account("mario.rossi@mail.it"));
        accounts.add(new Account("davide.giaccherini@mail.it"));
        accounts.add(new Account("test@mail.it"));
        saveData();
    }*/

    // --- LOGGING ---

    public void log(String msg) {
        Platform.runLater(() -> {
            logList.add(msg);
        });
    }

    public ObservableList<String> getLogList() {
        return logList;
    }

}