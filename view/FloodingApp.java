/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: FloodingApp
* Funcao...........: Aplicacao JavaFX: carrega o backbone.txt e monta a
*                    janela com o grafo, os controles e o controller.
*************************************************************** */
package view;

import controller.SimulationController;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import model.Graph;
import util.BackboneLoader;

public class FloodingApp extends Application {

  /* ***************************************************************
  * Metodo: main
  * Funcao: ponto de entrada alternativo; lanca a aplicacao JavaFX.
  * Parametros: args = argumentos da linha de comando
  * Retorno: void
  *************************************************************** */
  public static void main(String[] args) {
    launch(args);
  }//fim do metodo main

  /* ***************************************************************
  * Metodo: start
  * Funcao: carrega o grafo e monta a janela principal.
  * Parametros: stage = janela principal fornecida pelo JavaFX
  * Retorno: void
  *************************************************************** */
  @Override
  public void start(Stage stage) {
    Graph graph;
    try {
      graph = BackboneLoader.load(Paths.get("backbone.txt"));
    }//fim do try
    catch (Exception exception) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Erro");
      alert.setHeaderText("Falha ao carregar o backbone.txt");
      alert.setContentText(String.valueOf(exception.getMessage()));
      alert.showAndWait();
      Platform.exit();
      return;
    }//fim do catch
    GraphPane graphPane = new GraphPane(graph);
    ControlPanel controlPanel = new ControlPanel(graph);
    new SimulationController(controlPanel, graphPane);
    BorderPane root = new BorderPane();
    root.setCenter(graphPane);
    root.setRight(controlPanel);
    stage.setTitle("Roteamento por Inundacao");
    stage.setScene(new Scene(root, 1100.0, 650.0));
    stage.show();
  }//fim do metodo start

}//fim da classe FloodingApp
