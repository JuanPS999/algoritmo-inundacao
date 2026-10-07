/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: FloodingApp
* Funcao...........: Aplicacao JavaFX: carrega o backbone.txt e monta a interface grafica
*************************************************************** */
package view;

import controller.SimulationController;
import model.Graph;
import util.BackboneLoader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FloodingApp extends Application {

    private Graph graph;
    private SimulationController controller;
    private GraphPane graphPane;
    private ControlPanel controlPanel;
    private ResultsTable resultsTable;
    private EventLogPane eventLogPane;

    /* ***************************************************************
    * Metodo: start
    * Funcao: Inicializa a aplicacao JavaFX
    * Parametros: primaryStage - palco principal
    * Retorno: void
    *************************************************************** */
    @Override
    public void start(Stage primaryStage) {
        try {
            graph = BackboneLoader.load(Paths.get("backbone.txt"));
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Falha ao carregar o backbone.txt");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
            return;
        }

        graphPane = new GraphPane(graph);
        resultsTable = new ResultsTable();
        eventLogPane = new EventLogPane();
        controlPanel = new ControlPanel(null, graph);
        controller = new SimulationController(controlPanel, graphPane, resultsTable);
        controlPanel.setController(controller);

        // Conecta log de eventos
        controller.setLogConsumer(eventLogPane::addLines);

        BorderPane root = new BorderPane();
        root.setCenter(graphPane);
        root.setLeft(controlPanel);
        root.setRight(new VBox(5, new Label("Resultados"), resultsTable.getNode(),
                new Label("Log de Eventos"), eventLogPane));
        VBox.setVgrow(resultsTable.getNode(), Priority.ALWAYS);
        VBox.setVgrow(eventLogPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        Theme.apply(scene);
        primaryStage.setTitle("Roteamento por Inundacao");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(620);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.reset();
            }
        });
    }//fim do metodo start

    /* ***************************************************************
    * Metodo: getController
    * Funcao: Retorna o controlador de simulacao
    * Parametros: nenhum
    * Retorno: SimulationController
    *************************************************************** */
    public SimulationController getController() {
        return controller;
    }//fim do metodo getController

    /* ***************************************************************
    * Metodo: getGraphPane
    * Funcao: Retorna o painel do grafo
    * Parametros: nenhum
    * Retorno: GraphPane
    *************************************************************** */
    public GraphPane getGraphPane() {
        return graphPane;
    }//fim do metodo getGraphPane

    /* ***************************************************************
    * Metodo: getResultsTable
    * Funcao: Retorna a tabela de resultados
    * Parametros: nenhum
    * Retorno: ResultsTable
    *************************************************************** */
    public ResultsTable getResultsTable() {
        return resultsTable;
    }//fim do metodo getResultsTable

    /* ***************************************************************
    * Metodo: getEventLogPane
    * Funcao: Retorna o painel de log de eventos
    * Parametros: nenhum
    * Retorno: EventLogPane
    *************************************************************** */
    public EventLogPane getEventLogPane() {
        return eventLogPane;
    }//fim do metodo getEventLogPane

    /* ***************************************************************
    * Metodo: main
    * Funcao: Ponto de entrada da aplicacao
    * Parametros: args - argumentos da linha de comando
    * Retorno: void
    *************************************************************** */
    public static void main(String[] args) {
        launch(args);
    }//fim do metodo main
}//fim da classe FloodingApp