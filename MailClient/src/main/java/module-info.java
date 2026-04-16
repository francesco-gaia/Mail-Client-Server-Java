module MailClient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires com.google.gson;

    // Apre il pacchetto principale "client" (dove c'è il Main)
    opens client to javafx.fxml;
    opens client.model to com.google.gson;

    // *** QUESTA È LA RIGA CHE MANCAVA ***
    // Apre il pacchetto dei controller a JavaFX
    opens client.controller to javafx.fxml;

    // Esporta il pacchetto principale per poter avviare l'app
    exports client;
}