package client.model;

import java.util.List;

public class Email {

    private String id;
    private String sender;
    private List<String> recipients;
    private String subject;
    private String text;
    private String sendDate;

    public Email() {}

    public Email(String id, String sender, List<String> recipients, String subject, String text, String sendDate) {
        this.id = id;
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.text = text;
        this.sendDate = sendDate;
    }

    public String getId() { return id; }
    public String getSender() { return sender; }
    public List<String> getRecipients() { return recipients; }
    public String getSubject() { return subject; }
    public String getText() { return text; }
    public String getSendDate() { return sendDate; }
    public void setSendDate(String sendDate){ this.sendDate = sendDate; }

    @Override
    public String toString() {
        return sender + " "+recipients+ " : " + subject + " (" + sendDate + ")";
    }
}