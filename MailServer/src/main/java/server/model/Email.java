package server.model;

import java.util.List;
import java.util.UUID;

public class Email {

    // Attributi
    private String id;
    private String sender;
    private List<String> recipients;
    private String subject;
    private String text;
    private String sendDate; // Opzionale, se vuoi gestire la data

    // Nuovo campo per tracciare se è stata scaricata col comando NEW
    private boolean isRead;

    // Costruttore
    public Email(String sender, List<String> recipients, String subject, String text) {
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.text = text;
        this.id = UUID.randomUUID().toString(); // ID temporaneo, il server lo sovrascriverà
        this.isRead = false; // Di default una mail nuova non è letta
    }

    // --- GETTERS E SETTERS ---

    public String getId() {
        return id;
    }

    // QUESTO È IL METODO CHE MANCAVA
    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    // --- Metodi per lo stato di lettura (Protocollo NEW) ---

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return subject + " (Da: " + sender + ")";
    }
}