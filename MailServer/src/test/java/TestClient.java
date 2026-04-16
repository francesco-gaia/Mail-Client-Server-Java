import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient {

    // Porta del server (deve coincidere con quella nel ServerLauncher)
    private static final int PORT = 8189;

    public static void main(String[] args) {
        System.out.println("--- INIZIO TEST SERVER (Protocollo Aggiornato) ---");

        // 1. TEST LOGIN (CHECK)
        System.out.println("\n[1] VERIFICA UTENTI");
        sendRequest("CHECK mario.rossi@mail.it");
        sendRequest("CHECK utente.falso@mail.it");

        // 2. TEST INVIO MAIL (SEND)
        // Nota: L'ID che metto nel JSON ("999") verrà ignorato dal server, che ne calcolerà uno nuovo.
        System.out.println("\n[2] INVIO MAIL");
        String jsonMail = "{\"sender\":\"mario.rossi@mail.it\",\"recipients\":[\"test@mail.it\"],\"subject\":\"Test ID Server\",\"text\":\"Questa mail avrà un ID automatico.\",\"id\":\"temp\"}";
        sendRequest("SEND mario.rossi@mail.it " + jsonMail);

        // 3. TEST RICEZIONE STORICO (OLD)
        // Chiediamo le ultime 10 mail
        System.out.println("\n[3] RICHIESTA STORICO (OLD)");
        sendRequest("OLD test@mail.it 10");

        // 4. TEST RICEZIONE NUOVE (NEW)
        // Questo dovrebbe scaricare la mail appena inviata e segnarla come letta sul server
        System.out.println("\n[4] RICHIESTA NUOVE (NEW)");
        sendRequest("NEW test@mail.it");

        // 5. TEST RICEZIONE NUOVE (NEW) - SECONDA VOLTA
        // Ora dovrebbe restituire una lista vuota [] perché le mail sono state segnate come lette al passo 4
        System.out.println("\n[5] RICHIESTA NUOVE (NEW - Seconda volta)");
        sendRequest("NEW test@mail.it");

        // 6. TEST CANCELLAZIONE (REMOVE)
        // NOTA: Per testare questo devi guardare l'ID che esce dal passo 3 o 4 e scriverlo qui sotto al posto di '1'
        System.out.println("\n[6] CANCELLAZIONE (REMOVE)");
        // Provo a cancellare l'ID '1' (assumendo sia la prima mail). Se non esiste darà ERR MAIL_NOT_FOUND
        sendRequest("REMOVE test@mail.it 1");

        System.out.println("\n--- FINE TEST ---");
    }

    // Metodo che apre una connessione NUOVA per ogni richiesta (Socket non persistente)
    private static void sendRequest(String command) {
        try (Socket socket = new Socket("localhost", PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("[CLIENT] Invio: " + (command.length() > 80 ? command.substring(0, 80) + "..." : command));

            // Invia comando
            out.println(command);

            // Legge risposta
            String response = in.readLine();
            System.out.println("[SERVER] Risp.: " + response);

        } catch (IOException e) {
            System.out.println("ERRORE: Impossibile connettersi al server. È avviato? (" + e.getMessage() + ")");
        }
    }
}