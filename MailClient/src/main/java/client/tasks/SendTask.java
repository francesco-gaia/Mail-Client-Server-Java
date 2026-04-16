package client.tasks;

import client.model.Email;
import com.google.gson.Gson;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class SendTask extends Task{
    private final Email email;
    private final Consumer<Boolean> setControlsDisabledHandler;

    public SendTask(String mail, Email email, Consumer<String> status_handler, Consumer<Boolean> disableHandler){
        super(mail, status_handler);
        this.email = email;
        this.setControlsDisabledHandler = disableHandler;
    }

    public void run(){
        if(hasValidRecipients()) {
            Gson gson = new Gson();
            String toSend = "SEND " + email.getSender() + " " + gson.toJson(email);
            String err;
            if(!(err = connect()).isEmpty()) {
                setControlsDisabledHandler.accept(false);
                status_handler.accept(err);
            }else{
                String ret = work(toSend);
                if(!ret.equals("OK")) {
                    setControlsDisabledHandler.accept(false);
                    status_handler.accept(ret);
                }else{
                    setControlsDisabledHandler.accept(true);
                    status_handler.accept("Mail inviata correttamente");
                }
                closeConnection();
            }
        }else {
            setControlsDisabledHandler.accept(false);
            status_handler.accept("Destinatari mal scritti");
        }
    }

    private boolean hasValidRecipients(){
        boolean ret = true;
        List<String> mails = email.getRecipients();
        Iterator<String> it = mails.iterator();
        String value;
        while(it.hasNext() && ret){
            value = it.next();
            if(!Task.checkMail(value))
                ret = false;
        }
        return ret;
    }

    protected String work(String toSend){
        String status = "OK";
        String response;
        try {
            out.write(toSend+"\n");
            out.flush();
            response = in.readLine();
            String[] tokens = response.split(" ");
            if(tokens.length == 3 && tokens[0].equals("ERR")){
                status = "Errore, destinatario/i non riconosciuto/i";
            } else if (tokens.length == 2  || (tokens.length == 1 && !tokens[0].equals("OK"))) {
                status = "Errore, messaggio dal server non riconosciuto";
            }
        }catch (IOException e){
            status = "Errore nella comunicazione con server";
        }
        return status;
    }
}
