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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Graph;
import util.BackboneLoader;
import view.AboutDialog;
import view.ResultsTable;

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
        ResultsTable resultsTable = new ResultsTable();
        ComparisonChart comparisonChart = new ComparisonChart(resultsTable);
        EventLogPane eventLogPane = new EventLogPane();

        SimulationController controller = new SimulationController(controlPanel, graphPane, resultsTable);

        // Liga o log de eventos
        controller.setLogConsumer(lines -> {
            eventLogPane.addLines(lines);
            eventLogPane.setDroppedCount(controller.getDroppedLogLines());
        });

        // Liga callback de mudanca da tabela para redesenhar grafico
        resultsTable.setOnChange(() -> comparisonChart.redraw());

        // Liga botao Sortear semente
        controlPanel.setOnRandomSeed(() -> {
            long newSeed = new java.util.Random().nextLong();
            controlPanel.getSeedField().setText(String.valueOf(newSeed));
            controller.setSeed(newSeed);
            controlPanel.updateReplayCommand();
        });

        // Liga botao passo a passo
        controlPanel.setOnStep(() -> controller.step());

        // Liga redesenho do grafico apos mudancas na tabela
        // O ResultsTable nao tem listener, entao o ComparisonChart
        // sera notificado via wrapper nos metodos addResult/addFailure/clear
        // Por simplicidade, vamos fazer o ComparisonChart observar
        // chamando redraw() apos cada operacao na tabela.
        // O ResultsTable ja tem refresh() nos metodos, entao podemos
        // estender ou usar um callback. Vamos usar uma abordagem simples:
        // o ComparisonChart redesenha quando a tabela e atualizada.
        // Para isso, vamos adicionar um listener no ResultsTable ou
        // chamar redraw() do controller apos cada addResult/addFailure.
        // Aqui vamos apenas criar o grafico; o controller nao conhece o grafico.
        // Vamos fazer o ResultsTable notificar o ComparisonChart via callback.

        BorderPane root = new BorderPane();

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("Arquivo");
        MenuItem reloadItem = new MenuItem("Recarregar backbone");
        reloadItem.setOnAction(e -> {
            try {
                Graph newGraph = BackboneLoader.load(Paths.get("backbone.txt"));
                graphPane.setGraph(newGraph);
                controlPanel.updateForNewGraph(newGraph);
                resultsTable.clear();
                comparisonChart.redraw();
                eventLogPane.clear();
                controller.reset();
                controlPanel.setStatus("Backbone recarregado.");
            } catch (Exception ex) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("Falha ao recarregar backbone");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
        MenuItem exitItem = new MenuItem("Sair");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(reloadItem, exitItem);

        Menu helpMenu = new Menu("Ajuda");
        MenuItem aboutItem = new MenuItem("Sobre");
        aboutItem.setOnAction(e -> AboutDialog.show());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        // Area inferior: log em cima, tabela + grafico lado a lado embaixo
        HBox tableAndChart = new HBox(8.0);
        tableAndChart.getChildren().addAll(resultsTable.getNode(), comparisonChart);
        HBox.setHgrow(resultsTable.getNode(), javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(comparisonChart, javafx.scene.layout.Priority.ALWAYS);

        VBox bottomBox = new VBox(4.0);
        bottomBox.getChildren().addAll(eventLogPane, tableAndChart);
        VBox.setVgrow(eventLogPane, javafx.scene.layout.Priority.ALWAYS);
        VBox.setVgrow(tableAndChart, javafx.scene.layout.Priority.ALWAYS);

        root.setTop(menuBar);
        root.setCenter(graphPane);
        root.setRight(controlPanel);
        root.setBottom(bottomBox);

        stage.setTitle("Roteamento por Inundacao");
        stage.setScene(new Scene(root, 1100.0, 800.0));
        stage.show();
    }//fim do metodo start

}//fim da classe FloodingApp