package server.tasks;

import server.ServerManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class SchedulerTask implements Runnable {

    private final ServerManager serverManager;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private volatile boolean isRunning;

    public SchedulerTask(ServerManager serverManager, ServerSocket serverSocket, ExecutorService threadPool) {
        this.serverManager = serverManager;
        this.serverSocket = serverSocket;
        this.threadPool = threadPool;
        this.isRunning = true;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                // Il server si blocca qui aspettando un client
                Socket clientSocket = serverSocket.accept();

                // Appena arriva un client, crea un WorkTask e lo passa al thread pool
                WorkTask worker = new WorkTask(clientSocket, serverManager);
                threadPool.execute(worker);

            } catch (IOException e) {
                if (isRunning) {
                    serverManager.log("Errore di connessione: " + e.getMessage());
                }
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            serverManager.log("Errore chiusura scheduler: " + e.getMessage());
        }
    }
}