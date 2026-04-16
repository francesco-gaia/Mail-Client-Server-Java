module MailServer {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.base;

    // Apre il pacchetto principale (dove c'è MainServer)
    opens server to javafx.fxml;

    // *** QUESTA È LA RIGA CHE MANCAVA ***
    // Apre il pacchetto dove hai messo il Controller (come dice l'errore)
    opens server.controller to javafx.fxml;

    // Apre il model per Gson (per il JSON)
    opens server.model to com.google.gson;

    // Esporta il tutto
    exports server;
    exports server.model;
}