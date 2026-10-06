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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0d1117;");

        // MenuBar no topo
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
        root.setTop(menuBar);

        // Centro: GraphPane (elemento dominante) com contadores sobrepostos
        StackPane centerPane = new StackPane();
        centerPane.getChildren().add(graphPane);

        // Contadores sobrepostos no canto superior esquerdo
        VBox overlay = new VBox(4.0);
        overlay.setPadding(new Insets(12.0));
        overlay.setStyle("-fx-background-color: rgba(28, 36, 48, 0.9); " +
                "-fx-border-color: #2b3544; -fx-border-radius: 8; " +
                "-fx-background-radius: 8;");
        overlay.setMaxWidth(220.0);
        overlay.setMouseTransparent(true);

        Label lblPackets = new Label("Pacotes: 0");
        lblPackets.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
        Label lblTime = new Label("Tempo: 0 ms");
        lblTime.setStyle("-fx-text-fill: #e6edf3; -fx-font-size: 13px;");
        Label lblSnapshot = new Label("Snapshot: --");
        lblSnapshot.setStyle("-fx-text-fill: #4c9aff; -fx-font-size: 13px; -fx-font-weight: bold;");

        overlay.getChildren().addAll(lblPackets, lblTime, lblSnapshot);
        StackPane.setAlignment(overlay, javafx.geometry.Pos.TOP_LEFT);
        StackPane.setMargin(overlay, new Insets(16.0));
        centerPane.getChildren().add(overlay);

        // Guarda referencias para atualizacao via controller
        // Vamos expor metodos no GraphPane ou usar propriedades
        // Por simplicidade, o controller atualiza labels via reflection
        // ou podemos adicionar um callback. Vamos fazer o controller
        // atualizar diretamente os labels.
        controlPanel.setOverlayLabels(lblPackets, lblTime, lblSnapshot);

        root.setCenter(centerPane);

        // Direita: ControlPanel ja organizado em secoes
        controlPanel.getStyleClass().add("secao");
        root.setRight(controlPanel);

        // Inferior: TabPane com abas Log, Resultados, Comparacao
        TabPane bottomTabs = new TabPane();
        bottomTabs.setPrefHeight(240.0);
        bottomTabs.setMinHeight(240.0);
        bottomTabs.setMaxHeight(240.0);
        bottomTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabLog = new Tab("Log", eventLogPane);
        Tab tabResultados = new Tab("Resultados", resultsTable.getNode());
        Tab tabComparacao = new Tab("Comparacao", comparisonChart);

        bottomTabs.getTabs().addAll(tabLog, tabResultados, tabComparacao);
        root.setBottom(bottomTabs);

        Scene scene = new Scene(root, 1100.0, 800.0);
        Theme.apply(scene);

        stage.setTitle("Roteamento por Inundacao");
        stage.setScene(scene);
        stage.show();
    }//fim do metodo start

}//fim da classe FloodingApp