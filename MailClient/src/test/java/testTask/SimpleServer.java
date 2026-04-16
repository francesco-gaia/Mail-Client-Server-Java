package testTask;

import client.model.Email;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SimpleServer {
    public static void main(String[] args){
        try {
            ServerSocket socketServer = new ServerSocket(8189);
            System.out.println("Server in attesa");
            while (true){
                Socket socket = socketServer.accept();
                System.out.println("Connesso con "+socket.getLocalAddress());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                System.out.println("Attesa messaggio");
                String received = reader.readLine();
                System.out.println("Ricevuto : "+received);
                String response = handleResponse(received);
                System.out.println("Risposta a "+socket.getLocalAddress()+" : "+response);
                writer.write(response+"\n");
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore "+e.getMessage());
        }
    }

    private static String handleResponse(String received){
        String ret ="";
        Email simpleEmail;
        System.out.println("Request received: "+received);
        String[] tokens = received.split(" ", 3);
        System.out.println("Token size "+tokens.length);
        switch (tokens[0]){
            case "CHECK":
                System.out.println("Received CHECK request");
                if (tokens.length != 2)
                    ret = "ERR UNKNOWN_COMMAND";
                else
                    ret = "OK";
                break;
            case "NEW":
                System.out.println("Received NEW request");
                if(tokens.length != 2)
                    ret = "ERR UNKNOWN_COMMAND";
                else {
                    simpleEmail = new Email("t0", "serverNEW@test.com", new ArrayList<>(), "serverSubject", "Servertext", "ServerDate");
                    List<Email> mailList = new ArrayList<>();
                    mailList.add(simpleEmail);
                    Gson gson = new Gson();
                    ret = "OK "+gson.toJson(mailList);
                }
                break;
            case "OLD":
                System.out.println("Received OLD request");
                if(tokens.length != 3)
                    ret = "ERR UNKNOWN_COMMAND";
                else {
                    simpleEmail = new Email("t0", "serverOLD@test.com", new ArrayList<>(), "serverSubject", "Servertext", "ServerDate");
                    List<Email> mailList = new ArrayList<>();
                    mailList.add(simpleEmail);
                    Gson gson = new Gson();
                    ret = "OK "+gson.toJson(mailList);
                }
                break;
            case "SEND":
                System.out.println("Received SEND request");
                if(tokens.length != 3)
                    ret = "ERR UNKNOWN_COMMAND";
                else{
                    System.out.println("The send request is from "+tokens[1]);
                    Gson gson = new Gson();
                    Email receivedEmail = null;
                    try {
                        receivedEmail = gson.fromJson(tokens[2], Email.class);
                    } catch (JsonSyntaxException e) {
                        System.out.println("Richiesta SEND mal formata json");
                        ret = "ERR UNKNOWN_COMMAND";
                    }
                    if(receivedEmail != null) {
                        System.out.println("Received Mail: " + receivedEmail);
                        ret = "OK";
                    }
                }
                break;
            case "REMOVE":
                System.out.println("Received REMOVE request");
                if (tokens.length != 3)
                    ret = "ERR UNKNOWN_COMMAND";
                else{
                    System.out.println("The remove request is from "+tokens[1]);
                    System.out.println("The mail to be removed has ID: "+tokens[2]);
                    ret = "OK";
                }
                break;
            default:
                ret= "ERR UNKNOWN_COMMAND";
                break;
        }
        return ret;
    }
}
