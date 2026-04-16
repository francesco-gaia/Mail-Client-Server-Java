package client.tasks;

import java.io.IOException;
import java.util.function.Consumer;

public class CheckTask extends Task{
    private final Consumer<Boolean> successHandler;
    public CheckTask(String email, Consumer<String> status_handler, Consumer<Boolean> successHandler){
        super(email, status_handler);
        this.successHandler = successHandler;
    }

    public void run(){
        if(checkMail(mailAcc)) {
            String status;
            status = connect();
            if (status.isEmpty()) {
                String msg = "CHECK " + mailAcc + "\n";
                status = work(msg);
                if (status.compareTo("OK") == 0)
                    successHandler.accept(true);
                else
                    successHandler.accept(false);
                status_handler.accept(status);
                closeConnection();
            } else {
                status_handler.accept(status);
                successHandler.accept(false);
            }
        }else{
            status_handler.accept("Formato email non valido! es. nome@mail.com");
            successHandler.accept(false);
        }
    }

    protected String work(String toSend){
        String received;
        String ret;
        try{
            out.write(toSend);
            out.flush();
            received = in.readLine();
            ret = validateMsg(received);
        } catch (IOException e) {
            ret = "Errore di comunicazione con server";
        }
        return ret;
    }

    private String validateMsg(String msg){
        String ret = "OK";
        if(msg.compareTo("OK") != 0){
            String[] tokens = msg.split(" ");
            if(tokens.length == 2)
                ret = "Errore: "+tokens[1];
            else
                ret = "Errore formattazione messaggio server";
        }
        return ret;
    }

}
