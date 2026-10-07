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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
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

    // Larguras preferidas dos painéis
    private static final double LEFT_PREF_WIDTH = 300.0;
    private static final double RIGHT_PREF_WIDTH = 420.0;
    private static final double GRAPH_MIN_WIDTH = 560.0;

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
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Falha ao carregar o backbone.txt");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            // controlPanel pode ainda nao existir, mas se existir, marca ERRO
            // (o erro ocorre antes da criacao do controlPanel, entao so alerta)
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

        // Painel direito: tabela + log (com scroll vertical)
        VBox rightPane = new VBox(5, resultsTable.getNode(), eventLogPane);
        VBox.setVgrow(resultsTable.getNode(), Priority.ALWAYS);
        VBox.setVgrow(eventLogPane, Priority.ALWAYS);
        rightPane.setMinWidth(RIGHT_PREF_WIDTH);
        rightPane.setPrefWidth(RIGHT_PREF_WIDTH);
        rightPane.setMaxWidth(RIGHT_PREF_WIDTH);

        // Painel esquerdo: controles (largura fixa)
        controlPanel.setMinWidth(LEFT_PREF_WIDTH);
        controlPanel.setPrefWidth(LEFT_PREF_WIDTH);
        controlPanel.setMaxWidth(LEFT_PREF_WIDTH);

        // Grafo: protagonista, largura minima 560
        graphPane.setMinWidth(GRAPH_MIN_WIDTH);

        // SplitPane com 3 painéis: controles | grafo | resultados+log
        SplitPane splitPane = new SplitPane();
        Theme.styleSplitPane(splitPane);
        splitPane.setDividerPositions(0.2, 0.55);
        splitPane.getItems().addAll(controlPanel, graphPane, rightPane);

        // MenuBar com Arquivo e Ajuda (inclui Sobre)
        MenuBar menuBar = new MenuBar();
        Theme.styleMenuBar(menuBar);
        Menu fileMenu = new Menu("ARQUIVO");
        Theme.styleMenu(fileMenu);
        MenuItem exitItem = new MenuItem("SAIR");
        Theme.styleMenuItem(exitItem);
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().add(exitItem);

        Menu helpMenu = new Menu("AJUDA");
        Theme.styleMenu(helpMenu);
        MenuItem aboutItem = new MenuItem("SOBRE");
        Theme.styleMenuItem(aboutItem);
        aboutItem.setOnAction(e -> AboutDialog.show());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        // Root com MenuBar no topo
        VBox root = new VBox(menuBar, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // Variaveis CSS do tema escuro (Gruvbox) aplicadas na raiz
        root.setStyle(
            "-fx-base: #282828;" +
            "-fx-background: #1d2021;" +
            "-fx-control-inner-background: #1d2021;" +
            "-fx-control-inner-background-alt: #282828;" +
            "-fx-text-base-color: #ebdbb2;" +
            "-fx-accent: #d65d0e;" +
            "-fx-focus-color: #fe8019;" +
            "-fx-faint-focus-color: transparent;"
        );

        Scene scene = new Scene(root, 1280, 760);
        Theme.apply(scene);

        // Estilizar scrollbars do SplitPane apos a cena ser definida
        splitPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Theme.styleScrollBars(splitPane);
            }
        });

        primaryStage.setTitle("Roteamento por Inundacao");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(680);
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