package flooding.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Hello world JavaFX — Marco 0.
 * Classe principal da aplicacao de roteamento por inundacao.
 */
public class FloodingApp extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("Roteamento por Inundacao - ambiente OK");
        stage.setScene(new Scene(new StackPane(label), 480, 200));
        stage.setTitle("Flooding - Marco 0");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
