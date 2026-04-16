package server.tasks;

import java.net.Socket;

public abstract class Task implements Runnable {
    protected Socket socket;

    public Task(Socket socket) {
        this.socket = socket;
    }

    // Metodo utile per chiudere il socket alla fine del lavoro
    protected void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}