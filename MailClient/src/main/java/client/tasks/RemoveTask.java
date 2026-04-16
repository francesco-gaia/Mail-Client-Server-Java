package client.tasks;

import java.io.IOException;
import java.util.function.Consumer;

public class RemoveTask extends Task{
    private final String mailId;

    public RemoveTask(String mailAcc, Consumer<String> status_handler, String mailId){
        super(mailAcc, status_handler);
        this.mailId = mailId;
    }

    public void run(){
        String status = connect();
        String toSend;
        if(status.isEmpty()){
            toSend = "REMOVE "+mailAcc+" "+mailId;
            status = work(toSend);
            if(status.isEmpty())
                status_handler.accept("Mail rimossa correttamente");
            else
                status_handler.accept("Mail non rimossa dal server mail");
            closeConnection();
        }else
            status_handler.accept(status);
    }

    public String work(String toSend){
        String ret = "";
        String received;
        try {
            out.write(toSend+"\n");
            out.flush();
            received = in.readLine();
            if (received.compareTo("OK") != 0)
                ret = "Errore di comunicazione con server";
        } catch (IOException e) {
            ret = "Errore di connessione con server";
        }
        return ret;
    }
}
