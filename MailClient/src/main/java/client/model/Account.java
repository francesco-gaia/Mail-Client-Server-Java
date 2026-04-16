package client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Account {
    private final String accName;

    private final ObservableList<Email> mailList;

    private final SimpleStringProperty connectionStatus;

    public Account(String emailAddress) {
        this.accName = emailAddress;
        this.mailList = FXCollections.observableArrayList();
        this.connectionStatus = new SimpleStringProperty("Non connesso");
    }

    public ObservableList<Email> getMailList() { return mailList; }
    public StringProperty connectionStatusProperty() { return connectionStatus; }

    public String getEmail() { return accName; }
    public void setStatus(String status) { connectionStatus.set(status); }
    public String getStatus(){ return connectionStatus.get(); }

    public void addMail(Email mail) {
        mailList.addFirst(mail); // Aggiunge in cima (le più recenti in alto)
    }

    public void removeMail(Email email){
        mailList.remove(email);
    }
}