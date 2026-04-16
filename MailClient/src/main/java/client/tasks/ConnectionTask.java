package client.tasks;

import client.model.Email;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.List;

public class ConnectionTask extends Task{
    private boolean firstExec;
    private final Consumer<List<Email>> mails_handler;
    private final static int nRequests = 10;

    public ConnectionTask(String mail, boolean firstExec, Consumer<List<Email>> mails_handler, Consumer<String> status_handler){
        super(mail, status_handler);
        this.firstExec = firstExec;
        this.mails_handler = mails_handler;
        this.status_handler = status_handler;
    }

    public void run(){
        String status = connect();
        String toSend;
        if(status.isEmpty()){
            if(firstExec) {
                toSend = "OLD " + mailAcc + " " + nRequests;
            }else
                toSend = "NEW "+mailAcc;
            status = work(toSend);
            if(status.compareTo("OK") != 0)
                status_handler.accept(status);
            else
                status_handler.accept("Connesso");
            closeConnection();
        }else
            status_handler.accept(status);

    }

    protected String work(String toSend){
        String ret;
        String received;
        try {
            out.write(toSend+"\n");
            out.flush();
            received = in.readLine();
            ret = validateMsg(received);
            if(ret.compareTo("OK") == 0) {
                List<Email> emailList;
                emailList = extractList(received.split(" ", 2)[1]);
                firstExec = false;
                if(emailList != null)
                    mails_handler.accept(emailList);
                else
                    ret = "Errore formattazione messaggio ricevuto da server";
            }else
                throw new IOException("Errore comunicazione");

        } catch (IOException e) {
            ret = "Errore di comunicazione con server";
        }

        return ret;
    }

    private String validateMsg(String answer){
        String ret;
        String[] tokens = answer.split(" ", 2);
        if(tokens.length != 2){
            ret = "Connessione fallita: Errore comunicazione con server";
        }else{
            if(tokens[0].compareTo("OK") == 0){
                ret = "OK";
            }else{
                ret = "Connessione fallita: "+tokens[0] +" "+ tokens[1];
            }
        }
        return ret;
    }
    private List<Email> extractList(String msg){
        List<Email> mails;
        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Email>>() {}.getType();
            mails = gson.fromJson(msg, listType);
        } catch (JsonSyntaxException e) {
            mails = null;
        }
        return mails;
    }
}
