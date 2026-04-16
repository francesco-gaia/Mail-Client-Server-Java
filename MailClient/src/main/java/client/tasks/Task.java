package client.tasks;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class Task implements Runnable {
    protected String mailAcc;
    protected static String serverIP = "127.0.0.1";
    protected static int port = 8189;
    protected static String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    protected Socket socket;
    protected BufferedReader in;
    protected BufferedWriter out;
    protected Consumer<String> status_handler;

    public Task(String email, Consumer<String> status_handler){
        mailAcc = email;
        this.status_handler = status_handler;
    }

    protected static boolean checkMail(String mail){
        boolean valid;
        if (mail == null) {
            valid = false;
        } else {
            valid = mail.matches(regex);
        }
        return valid;
    }

    protected String connect(){
        String ret = "";
        try {
            socket = new Socket(serverIP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            ret = "Errore di connessione: "+e.getMessage();
        }
        return ret;
    }
    protected abstract String work(String toSend);
    protected void closeConnection(){
        try{
            if(in != null)
                in.close();
        } catch (IOException _) {}
        try {
            if(out != null)
                out.close();
        } catch (IOException _) {}
        try{
            socket.close();
        } catch (IOException _) {}
    }
}
