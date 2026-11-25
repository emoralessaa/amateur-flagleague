package com.flagleague;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Ventana temporal de comprobación de JavaFX/FXML.
 * Emilio sustituirá esta vista por su navegación y sus controladores.
 * Parra coordinará la inicialización compartida mediante AppBootstrap.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL vista = MainApp.class.getResource("/fxml/dashboard.fxml");
        if (vista == null) {
            throw new IllegalStateException("No se encontró /fxml/dashboard.fxml");
        }

        Parent raiz = FXMLLoader.load(vista);
        Scene escena = new Scene(raiz, 900, 560);
        URL estilos = MainApp.class.getResource("/css/flagleague.css");
        if (estilos != null) {
            escena.getStylesheets().add(estilos.toExternalForm());
        }
        stage.setTitle("FlagLeague Manager — Base JavaFX");
        stage.setScene(escena);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
