/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: FloodingApp
* Funcao...........: Ponto de entrada da aplicacao GUI (Marco 3). Lanaca o GraphPane para desenhar o grafo.
*************************************************************** */
package view;

import model.Graph;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principal da aplicacao GUI. Inicia a janela com o grafo carregado do backbone.
 */
public class FloodingApp extends Application {

    /* ***************************************************************
* Metodo: main
* Funcao: Ponto de entrada da aplicacao. Lanca a GUI JavaFX.
* Parametros: args - argumentos da linha de comando.
* Retorno: void
*************************************************************** */
public static void main(String[] args) {
        launch(args);
    }

    /* ***************************************************************
* Metodo: start
* Funcao: Configura e mostra a janela principal com o GraphPane.
* Parametros: primaryStage - janela principal.
* Retorno: void
*************************************************************** */
@Override
public void start(Stage primaryStage) {
        GraphPane graphPane = new GraphPane();
        Scene scene = new Scene(graphPane, 800, 600);
        primaryStage.setTitle("Roteamento por Inundacao - Marco 3");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}