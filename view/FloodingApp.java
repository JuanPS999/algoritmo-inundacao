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
        ResultsTable resultsTable = new ResultsTable();
        ComparisonChart comparisonChart = new ComparisonChart(resultsTable);
        EventLogPane eventLogPane = new EventLogPane();

        // ControlPanel precisa do controller, mas controller precisa do controlPanel
        // Vamos criar o controlPanel sem controller primeiro, depois criar controller
        // e setar no controlPanel via reflexao ou criando depois
        ControlPanel controlPanel = new ControlPanel(null, graph);
        SimulationController controller = new SimulationController(controlPanel, graphPane, resultsTable);
        controlPanel.setController(controller);

        // Agora seta o controller no controlPanel
        // Precisamos acessar o campo privado controller do ControlPanel
        // Como nao tem setter, vamos usar reflexao ou adicionar um metodo
        // Por simplicidade, vamos adicionar um metodo setController no ControlPanel
        // Mas para evitar mudancas grandes, vamos criar o controlPanel depois
        // Melhor: vamos refazer a ordem

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

        // Contadores sobrepostos no canto superior esquerdo (D3)
        VBox overlay = new VBox(4.0);
        overlay.setPadding(new Insets(12.0));
        overlay.getStyleClass().add("contador-overlay");
        overlay.setMaxWidth(280.0);
        overlay.setMouseTransparent(true);

        Label lblPackets = new Label("Pacotes");
        lblPackets.getStyleClass().add("contador-rotulo");
        Label lblPacketsValue = new Label("0");
        lblPacketsValue.getStyleClass().add("contador-valor");

        Label lblTime = new Label("Tempo virtual");
        lblTime.getStyleClass().add("contador-rotulo");
        Label lblTimeValue = new Label("0 ms");
        lblTimeValue.getStyleClass().add("contador-valor");

        Label lblSnapshot = new Label("Snapshot: --");
        lblSnapshot.getStyleClass().add("contador-snapshot");

        Label lblFinished = new Label("");
        lblFinished.getStyleClass().add("contador-finalizada");
        lblFinished.setVisible(false);
        lblFinished.setManaged(false);

        overlay.getChildren().addAll(
            lblPackets, lblPacketsValue,
            lblTime, lblTimeValue,
            lblSnapshot,
            lblFinished
        );
        StackPane.setAlignment(overlay, javafx.geometry.Pos.TOP_LEFT);
        StackPane.setMargin(overlay, new Insets(16.0));
        centerPane.getChildren().add(overlay);

        // Guarda referencias para atualizacao via controller
        controlPanel.setOverlayLabels(lblPacketsValue, lblTimeValue, lblSnapshot, lblFinished);

        root.setCenter(centerPane);

        // Direita: ControlPanel
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

        Scene scene = new Scene(root, 1100.0, 700.0);
        Theme.apply(scene);

        stage.setTitle("Roteamento por Inundacao");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }//fim do metodo start

}//fim da classe FloodingApp