package server.tasks;

import com.google.gson.Gson;
import server.ServerManager;
import server.model.Account;
import server.model.Email;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkTask extends Task {

    private final ServerManager serverManager;
    private final Gson gson;

    public WorkTask(Socket socket, ServerManager serverManager) {
        super(socket);
        this.serverManager = serverManager;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // LOG APERTURA CONNESSIONE
            serverManager.log("[CONN] Client connesso: " + socket.getInetAddress());

            String request = in.readLine();
            if (request != null) {
                String response = handleRequest(request);
                out.println(response);

                // LOG ERRORI GENERICI NEL PROTOCOLLO
                if (response.startsWith("ERR")) {
                    serverManager.log("[PROTO-ERR] Richiesta: " + request + " -> Risp: " + response);
                }
            }

        } catch (IOException e) {
            serverManager.log("[ERR] Disconnessione imprevista: " + e.getMessage());
        } finally {
            // LOG CHIUSURA CONNESSIONE
            serverManager.log("[CONN] Connessione chiusa.");
            closeSocket();
        }
    }

    private String handleRequest(String request) {
        // Divide in max 3 parti (COMANDO ARGOMENTO {JSON_O_ID})
        String[] tokens = request.split(" ", 3);
        String command = tokens[0];

        switch (command) {

            // 1. LOGIN
            case "CHECK":
                if (tokens.length < 2) return "ERR_ARGS";
                boolean exists = serverManager.getAccount(tokens[1]) != null;
                return exists ? "OK" : "ERR USER_NOT_RECOGNIZED";

            // 2. RICEZIONE (Ultime n mail)
            case "OLD":
                if (tokens.length < 3) return "ERR_ARGS";
                Account accOld = serverManager.getAccount(tokens[1]);
                if (accOld == null) return "ERR USER_NOT_RECOGNIZED";

                try {
                    int n = Integer.parseInt(tokens[2]);
                    return "OK " + gson.toJson(accOld.getLastNMails(n));
                } catch (NumberFormatException e) {
                    return "ERR_ARGS";
                }

                // 3. RICEZIONE (Nuove mail - per ora uguale a OLD senza n)
            case "NEW":
                if (tokens.length < 2) return "ERR_ARGS";
                Account accNew = serverManager.getAccount(tokens[1]);
                if (accNew != null) {
                    // 1. Prendi solo le mail NON lette
                    List<Email> newMails = new ArrayList<>();
                    for (Email e : accNew.getInbox()) {
                        if (!e.isRead()) {
                            newMails.add(e);
                            e.setRead(true); // 2. Segna come lette (spedite al client)
                        }
                    }

                    // 3. Salva lo stato (importante!)
                    if (!newMails.isEmpty()) {
                        serverManager.saveData(); // Aggiungi metodo public saveData() in ServerManager se è private
                    }

                    return "OK " + gson.toJson(newMails);
                }
                return "ERR USER_NOT_RECOGNIZED";

            // 4. INVIO
            case "SEND":
                if (tokens.length < 3) return "ERR_ARGS";
                try {
                    Email email = gson.fromJson(tokens[2], Email.class);
                    email.setSendDate(LocalDate.now().toString());
                    boolean sent = serverManager.dispatchEmail(email);
                    return sent ? "OK" : "ERR " + email.getSender() + " USER_NOT_RECOGNIZED";
                } catch (Exception e) {
                    return "ERR_JSON_FORMAT";
                }

                // 5. CANCELLAZIONE (Questo è quello che falliva!)
            case "REMOVE":
                if (tokens.length < 3) return "ERR_ARGS";
                String userEmail = tokens[1];
                String emailId = tokens[2];

                int result = serverManager.removeEmail(userEmail, emailId);

                if (result == 1) return "OK";
                if (result == -1) return "ERR " + userEmail + " USER_NOT_RECOGNIZED";
                return "ERR MAIL_NOT_FOUND";

            default:
                return "ERR UNKNOWN_COMMAND";
        }
    }
}