package server.model;

import java.util.ArrayList;
import java.util.List;

public class Account {
    // Nome dell'account (es: mario.rossi@mail.it)
    private final String username;
    // La lista delle mail ricevute (Inbox)
    private List<Email> inbox;
    private int mailID;

    public Account(String username, int lastID) {
        this.username = username;
        this.inbox = new ArrayList<>();
        mailID = lastID;
    }

    public String getUsername() {
        return username;
    }

    public synchronized List<Email> getInbox() {
        return inbox;
    }

    public synchronized List<Email> getLastNMails(int n){
        if(n > inbox.size())
            n = inbox.size();
        List<Email> ret = new ArrayList<>(inbox.subList(inbox.size()-n, inbox.size()));

        for(Email mail : ret) {
            mail.setRead(true);
        }

        return ret;
    }

    public synchronized void addEmail(Email email) {
        email.setId(username.substring(0, 1)+mailID++);
        this.inbox.add(email);
    }
}