/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: ControlPanel
* Funcao...........: Painel de controle com botoes, seletores e configuracoes
*************************************************************** */
package view;

import controller.SimulationController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.Node;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;
import model.Graph;
import model.RunResult;
import util.NodeLabelUtil;
import util.Statistics;
import java.util.List;

public class ControlPanel extends VBox {
  private SimulationController controller;
  private final Graph graph;

  // Estado da simulacao para controle centralizado dos botoes
  public enum EstadoSimulacao {
    OCIOSO, RODANDO, PAUSADO, ENCERRADO, LOTE
  }
  private EstadoSimulacao estadoAtual = EstadoSimulacao.OCIOSO;

  private ComboBox<Integer> versionCombo;
  private ComboBox<Integer> sourceCombo;
  private ComboBox<Integer> targetCombo;
  private Spinner<Integer> ttlSpinner;
  private Spinner<Integer> windowSpinner;
  private Spinner<Integer> batchCountSpinner;
  private Slider speedSlider;
  private TextField seedField;
  private TextField replayField;
  private Button startBtn;
  private Button pauseBtn;
  private Button resetBtn;
  private Button stepBtn;
  private Button randomSeedBtn;
  private Button copyReplayBtn;
  private CheckBox logScaleCheck;
  private ResultsTable resultsTable;

  // Lote
  private Spinner<Integer> batchSpinner;
  private Button batchRunBtn;
  private Button batchCancelBtn;
  private Label batchProgressLabel;
  private Runnable onCancelCallback;

  // Callbacks para compatibilidade com controller antigo
  private Runnable onStartCallback;
  private Runnable onPauseCallback;
  private Runnable onResetCallback;
  private Runnable onEndpointsChangeCallback;
  private Runnable onBatchCallback;
  private Runnable onStepCallback;
  private Runnable onRandomSeedCallback;

  // Labels do overlay
  private Label overlayPacketsLabel;
  private Label overlayTimeLabel;
  private Label overlaySnapshotLabel;
  private Label overlayFinishedLabel;

  // Labels da secao MEDICAO (sempre visiveis)
  private Label measEstadoLabel;
  private Label measConfigLabel;
  private Label measPacotesLabel;
  private Label measTempoLabel;
  private Label measEmTransitoLabel;
  private Label measDestinoLabel;

  // Indicador de estado (quadrado + texto)
  private final StackPane statusIndicator = new StackPane();
  private final Rectangle statusSquare = new Rectangle(14, 14);
  private final Label statusText = new Label();

  public ControlPanel(SimulationController controller, Graph graph) {
    this.controller = controller;
    this.graph = graph;
    setSpacing(12);
    setPadding(new Insets(12));
    setMinWidth(360);
    setPrefWidth(380);
    setMaxWidth(380);
    Theme.stylePanel(this);
    build();
    // Estado inicial: PRONTO
    setState("PRONTO");
  }

  /* ***************************************************************
  * Metodo: setController
  * Funcao: Define o controller (para criacao tardia).
  * Parametros: controller - SimulationController
  * Retorno: void
  *************************************************************** */
  public void setController(SimulationController controller) {
    this.controller = controller;
  }

  /* ***************************************************************
  * Metodo: setResultsTable
  * Funcao: Define a tabela de resultados para o grafico ler os dados.
  * Parametros: resultsTable - ResultsTable
  * Retorno: void
  *************************************************************** */
  public void setResultsTable(ResultsTable resultsTable) {
    this.resultsTable = resultsTable;
    if (resultsTable != null) {
      resultsTable.setOnChange(this::refreshChart);
      refreshChart();
    }
  }

  /* ***************************************************************
  * Metodo: refreshChart
  * Funcao: Atualiza o grafico de barras lendo media, desvio e N
  *         da ResultsTable para V1..V4. Com N=0 a barra fica vazia.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void refreshChart() {
    if (resultsTable == null || chartData == null) {
      return;
    }
    chartData.getData().clear();
    for (int v = 1; v <= 4; v++) {
      List<Long> samples = resultsTable.getSamples(v);
      int n = samples.size();
      if (n > 0) {
        double mean = Statistics.mean(samples);
        chartData.getData().add(new XYChart.Data<>("V" + v, mean));
      }
      // N=0: nao adiciona ponto -> barra vazia
    }
  }

  /* ***************************************************************
  * Metodo: build
  * Funcao: Constroi a interface do painel de controle
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void build() {
    // Indicador de estado no topo
    statusSquare.setArcWidth(2);
    statusSquare.setArcHeight(2);
    statusText.setFont(Theme.FONT_12_BOLD);
    statusText.setTextFill(Theme.TEXT_COLOR);
    statusIndicator.setAlignment(Pos.CENTER_LEFT);
    statusIndicator.getChildren().addAll(statusSquare, statusText);
    StackPane.setMargin(statusText, new Insets(0, 0, 0, 18));
    getChildren().add(statusIndicator);

    // Toggle buttons para selecao de aba (substitui TabPane header)
    ToggleGroup tabGroup = new ToggleGroup();
    HBox tabBar = new HBox();
    tabBar.setFillHeight(true);
    tabBar.setSpacing(0);

    // Criar conteudos das tres abas
    VBox scenarioContent = createScenarioTab();
    VBox executionContent = createExecutionTab();
    VBox resultsContent = createResultsTab();

    // StackPane para alternar conteudos
    StackPane contentStack = new StackPane();
    contentStack.getChildren().addAll(scenarioContent, executionContent, resultsContent);
    scenarioContent.setVisible(true);
    scenarioContent.setManaged(true);
    executionContent.setVisible(false);
    executionContent.setManaged(false);
    resultsContent.setVisible(false);
    resultsContent.setManaged(false);

    // ToggleButton CENARIO
    ToggleButton scenarioBtn = new ToggleButton("CENARIO");
    scenarioBtn.setToggleGroup(tabGroup);
    scenarioBtn.setSelected(true);
    scenarioBtn.setMaxWidth(Double.MAX_VALUE);
    scenarioBtn.setMinWidth(Region.USE_PREF_SIZE);
    scenarioBtn.setPadding(new Insets(6, 8, 6, 8));
    HBox.setHgrow(scenarioBtn, Priority.ALWAYS);
    Theme.styleToggleButton(scenarioBtn, true);

    // ToggleButton EXECUCAO
    ToggleButton executionBtn = new ToggleButton("EXECUCAO");
    executionBtn.setToggleGroup(tabGroup);
    executionBtn.setMaxWidth(Double.MAX_VALUE);
    executionBtn.setMinWidth(Region.USE_PREF_SIZE);
    executionBtn.setPadding(new Insets(6, 8, 6, 8));
    HBox.setHgrow(executionBtn, Priority.ALWAYS);
    Theme.styleToggleButton(executionBtn, false);

    // ToggleButton RESULTADOS
    ToggleButton resultsBtn = new ToggleButton("RESULTADOS");
    resultsBtn.setToggleGroup(tabGroup);
    resultsBtn.setMaxWidth(Double.MAX_VALUE);
    resultsBtn.setMinWidth(Region.USE_PREF_SIZE);
    resultsBtn.setPadding(new Insets(6, 8, 6, 8));
    HBox.setHgrow(resultsBtn, Priority.ALWAYS);
    Theme.styleToggleButton(resultsBtn, false);

    tabBar.getChildren().addAll(scenarioBtn, executionBtn, resultsBtn);

    // Listener para troca de aba
    tabGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
        boolean isScenario = newToggle == scenarioBtn;
        boolean isExecution = newToggle == executionBtn;
        boolean isResults = newToggle == resultsBtn;

        scenarioContent.setVisible(isScenario);
        scenarioContent.setManaged(isScenario);
        executionContent.setVisible(isExecution);
        executionContent.setManaged(isExecution);
        resultsContent.setVisible(isResults);
        resultsContent.setManaged(isResults);

        Theme.styleToggleButton(scenarioBtn, isScenario);
        Theme.styleToggleButton(executionBtn, isExecution);
        Theme.styleToggleButton(resultsBtn, isResults);
    });

    // Container das abas (barra + conteudo)
    VBox tabsContainer = new VBox(tabBar, contentStack);
    VBox.setVgrow(contentStack, Priority.ALWAYS);

    getChildren().add(tabsContainer);
    VBox.setVgrow(tabsContainer, Priority.ALWAYS);

    // Sessao MEDICAO (sempre visivel, abaixo das abas)
    VBox measurementSection = createMeasurementSection();
    getChildren().add(measurementSection);
  }

  /* ***************************************************************
  * Metodo: setState
  * Funcao: Atualiza o indicador visual de estado (quadrado + texto).
  *         Estados: PRONTO (#665c54), EXECUTANDO (laranja #fe8019),
  *         PAUSADO (amarelo-sujo #d79921), ENCERRADO (bege #a89984),
  *         ERRO (vermelho #fb4934).
  * Parametros: state - nome do estado
  * Retorno: void
  *************************************************************** */
  public void setState(String state) {
    Color squareColor;
    String text;
    switch (state) {
      case "PRONTO":
        squareColor = Theme.INACTIVE_COLOR;
        text = "PRONTO";
        break;
      case "EXECUTANDO":
        squareColor = Theme.ORANGE_ACCENT_COLOR;
        text = "EXECUTANDO";
        break;
      case "PAUSADO":
        squareColor = Theme.YELLOW_COLOR;
        text = "PAUSADO";
        break;
      case "ENCERRADO":
        squareColor = Theme.TEXT_SECONDARY_COLOR;
        text = "ENCERRADO";
        break;
      case "ERRO":
        squareColor = Theme.RED_COLOR;
        text = "ERRO";
        break;
      case "LOTE":
        squareColor = Theme.RX_COLOR_OBJ;
        text = "LOTE";
        break;
      default:
        squareColor = Theme.INACTIVE_COLOR;
        text = state;
    }
    statusSquare.setFill(squareColor);
    statusText.setText(text);
    if (measEstadoLabel != null) {
      measEstadoLabel.setText(text);
    }
  }

  /* ***************************************************************
  * Metodo: createScenarioTab
  * Funcao: Cria a aba de cenario com versao, origem, destino, TTL, janela
  * Parametros: nenhum
  * Retorno: VBox com o conteudo da aba
  *************************************************************** */
  private VBox createScenarioTab() {
    VBox box = new VBox(12);
    box.setPadding(new Insets(12));
    box.setFillWidth(true);
    Theme.stylePanel(box);

    // Versao
    Label versionLabel = new Label("VERSAO");
    Theme.styleSectionTitle(versionLabel);
    versionCombo = new ComboBox<>();
    versionCombo.getItems().addAll(1, 2, 3, 4);
    versionCombo.setValue(1);
    versionCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<Integer>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          switch (item) {
            case 1: setText("V1 - todas"); break;
            case 2: setText("V2 - sem a de chegada"); break;
            case 3: setText("V3 - com TTL"); break;
            case 4: setText("V4 - TTL + janela"); break;
            default: setText(item.toString());
          }
        }
      }
    });
    versionCombo.setButtonCell(new javafx.scene.control.ListCell<Integer>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          switch (item) {
            case 1: setText("V1 - todas"); break;
            case 2: setText("V2 - sem a de chegada"); break;
            case 3: setText("V3 - com TTL"); break;
            case 4: setText("V4 - TTL + janela"); break;
            default: setText(item.toString());
          }
        }
      }
    });
    versionCombo.setMaxWidth(Double.MAX_VALUE);
    Theme.styleComboBox(versionCombo);
    versionCombo.setOnAction(e -> controller.onVersionChanged(versionCombo.getValue()));

    // Origem
    Label sourceLabel = new Label("TRANSMISSOR");
    Theme.styleSectionTitle(sourceLabel);
    sourceCombo = new ComboBox<>();
    // 8 opcoes: 0->A, 1->B, 2->C, 3->D, 4->E, 5->F, 6->G, 7->H
    for (int i = 0; i < 8; i++) {
      sourceCombo.getItems().add(i);
    }
    sourceCombo.setValue(0); // 0 -> A
    sourceCombo.setMaxWidth(Double.MAX_VALUE);
    sourceCombo.setConverter(new StringConverter<Integer>() {
      @Override
      public String toString(Integer id) {
        if (id == null) {
          return "";
        }
        // Mapeamento 0-based: 0->A, 1->B, 2->C, 3->D, 4->E, 5->F, 6->G, 7->H
        switch (id) {
          case 0: return "A";
          case 1: return "B";
          case 2: return "C";
          case 3: return "D";
          case 4: return "E";
          case 5: return "F";
          case 6: return "G";
          case 7: return "H";
          default: return id.toString();
        }
      }

      @Override
      public Integer fromString(String s) {
        if (s == null || s.isEmpty()) {
          return null;
        }
        // Converte letra para valor 0-based: A->0, B->1, ..., H->7
        switch (s) {
          case "A": return 0;
          case "B": return 1;
          case "C": return 2;
          case "D": return 3;
          case "E": return 4;
          case "F": return 5;
          case "G": return 6;
          case "H": return 7;
          default: return null;
        }
      }
    });
    Theme.styleComboBox(sourceCombo);
    sourceCombo.setOnAction(e -> controller.onSourceChanged(sourceCombo.getValue()));

    // Destino
    Label targetLabel = new Label("RECEPTOR");
    Theme.styleSectionTitle(targetLabel);
    targetCombo = new ComboBox<>();
    // 8 opcoes: 0->A, 1->B, 2->C, 3->D, 4->E, 5->F, 6->G, 7->H
    for (int i = 0; i < 8; i++) {
      targetCombo.getItems().add(i);
    }
    targetCombo.setValue(7); // 7 -> H
    targetCombo.setMaxWidth(Double.MAX_VALUE);
    targetCombo.setConverter(new StringConverter<Integer>() {
      @Override
      public String toString(Integer id) {
        if (id == null) {
          return "";
        }
        // Mapeamento 0-based: 0->A, 1->B, 2->C, 3->D, 4->E, 5->F, 6->G, 7->H
        switch (id) {
          case 0: return "A";
          case 1: return "B";
          case 2: return "C";
          case 3: return "D";
          case 4: return "E";
          case 5: return "F";
          case 6: return "G";
          case 7: return "H";
          default: return id.toString();
        }
      }

      @Override
      public Integer fromString(String s) {
        if (s == null || s.isEmpty()) {
          return null;
        }
        // Converte letra para valor 0-based: A->0, B->1, ..., H->7
        switch (s) {
          case "A": return 0;
          case "B": return 1;
          case "C": return 2;
          case "D": return 3;
          case "E": return 4;
          case "F": return 5;
          case "G": return 6;
          case "H": return 7;
          default: return null;
        }
      }
    });
    Theme.styleComboBox(targetCombo);
    targetCombo.setOnAction(e -> controller.onTargetChanged(targetCombo.getValue()));

    // TTL
    Label ttlLabel = new Label("TTL");
    Theme.styleSectionTitle(ttlLabel);
    int diameter = graph.diameter();
    ttlSpinner = new Spinner<>(1, 100, diameter);
    ttlSpinner.setEditable(true);
    ttlSpinner.setMaxWidth(Double.MAX_VALUE);
    Theme.styleSpinner(ttlSpinner);
    ttlSpinner.valueProperty().addListener((obs, o, n) -> controller.onTtlChanged(n));

    // Janela V4 (ms)
    Label windowLabel = new Label("JANELA V4 (MS)");
    Theme.styleSectionTitle(windowLabel);
    windowSpinner = new Spinner<>(0, 10000, 0);
    windowSpinner.setEditable(true);
    windowSpinner.setMaxWidth(Double.MAX_VALUE);
    Theme.styleSpinner(windowSpinner);
    windowSpinner.valueProperty().addListener((obs, o, n) -> controller.onWindowChanged(n));

    box.getChildren().addAll(
      versionLabel, versionCombo,
      sourceLabel, sourceCombo,
      targetLabel, targetCombo,
      ttlLabel, ttlSpinner,
      windowLabel, windowSpinner
    );
    return box;
  }

  /* ***************************************************************
  * Metodo: createExecutionTab
  * Funcao: Cria a aba de execucao com botoes, velocidade, semente, reproducao
  * Parametros: nenhum
  * Retorno: VBox com o conteudo da aba
  *************************************************************** */
  private VBox createExecutionTab() {
    VBox box = new VBox(12);
    box.setPadding(new Insets(12));
    box.setFillWidth(true);
    Theme.stylePanel(box);

    // Botoes de controle em GridPane 2x2
    GridPane buttonGrid = new GridPane();
    buttonGrid.setHgap(8);
    buttonGrid.setVgap(8);
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setPercentWidth(50);
    col1.setFillWidth(true);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setPercentWidth(50);
    col2.setFillWidth(true);
    buttonGrid.getColumnConstraints().addAll(col1, col2);

    startBtn = new Button("INICIAR");
    startBtn.setMaxWidth(Double.MAX_VALUE);
    startBtn.setOnAction(e -> {
      if (onStartCallback != null) onStartCallback.run();
    });
    GridPane.setFillWidth(startBtn, true);
    Theme.styleFlatButtonFilled(startBtn);

    pauseBtn = new Button("PAUSAR");
    pauseBtn.setMaxWidth(Double.MAX_VALUE);
    pauseBtn.setOnAction(e -> {
      if (onPauseCallback != null) onPauseCallback.run();
    });
    pauseBtn.setDisable(true);
    GridPane.setFillWidth(pauseBtn, true);
    Theme.styleFlatButtonOutlined(pauseBtn);

    resetBtn = new Button("RESETAR");
    resetBtn.setMaxWidth(Double.MAX_VALUE);
    resetBtn.setOnAction(e -> {
      if (onResetCallback != null) onResetCallback.run();
    });
    GridPane.setFillWidth(resetBtn, true);
    Theme.styleFlatButtonOutlined(resetBtn);

    stepBtn = new Button("PROXIMO EVENTO");
    stepBtn.setMaxWidth(Double.MAX_VALUE);
    stepBtn.setWrapText(true);
    stepBtn.setOnAction(e -> {
      if (onStepCallback != null) onStepCallback.run();
    });
    stepBtn.setDisable(true);
    GridPane.setFillWidth(stepBtn, true);
    Theme.styleFlatButtonOutlined(stepBtn);

    buttonGrid.add(startBtn, 0, 0);
    buttonGrid.add(pauseBtn, 1, 0);
    buttonGrid.add(resetBtn, 0, 1);
    buttonGrid.add(stepBtn, 1, 1);

    // Velocidade
    Label speedLabel = new Label("VELOCIDADE");
    Theme.styleSectionTitle(speedLabel);
    speedSlider = new Slider(0.25, 10.0, 2.0);
    speedSlider.setShowTickLabels(true);
    speedSlider.setShowTickMarks(true);
    speedSlider.setMajorTickUnit(2.0);
    speedSlider.setMinorTickCount(7);
    speedSlider.setBlockIncrement(0.25);
    speedSlider.setSnapToTicks(false);
    Theme.styleSlider(speedSlider);
    speedSlider.valueProperty().addListener((obs, o, n) -> controller.onSpeedChanged(n.doubleValue()));

    // Semente
    Label seedLabel = new Label("SEMENTE");
    Theme.styleSectionTitle(seedLabel);
    HBox seedBox = new HBox(8);
    seedField = new TextField();
    seedField.setPromptText("ALEATORIA");
    HBox.setHgrow(seedField, Priority.ALWAYS);
    Theme.styleSeedField(seedField);
    randomSeedBtn = new Button("SORTEAR");
    randomSeedBtn.setOnAction(e -> {
      long seed = System.currentTimeMillis() ^ (long) (Math.random() * 1000000);
      seedField.setText(Long.toString(seed));
      controller.onSeedChanged(seed);
      if (onRandomSeedCallback != null) onRandomSeedCallback.run();
    });
    Theme.styleFlatButtonOutlined(randomSeedBtn);
    seedBox.getChildren().addAll(seedField, randomSeedBtn);

    // Reproducao
    Label replayLabel = new Label("REPRODUCAO");
    Theme.styleSectionTitle(replayLabel);
    HBox replayBox = new HBox(8);
    replayField = new TextField();
    replayField.setEditable(false);
    replayField.setPromptText("ULTIMA");
    HBox.setHgrow(replayField, Priority.ALWAYS);
    Theme.styleReplayField(replayField);
    copyReplayBtn = new Button("COPIAR");
    copyReplayBtn.setOnAction(e -> {
      javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
      javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
      content.putString(replayField.getText());
      clipboard.setContent(content);
    });
    Theme.styleFlatButtonOutlined(copyReplayBtn);
    replayBox.getChildren().addAll(replayField, copyReplayBtn);

    // Lote
    Label batchLabel = new Label("LOTE");
    Theme.styleSectionTitle(batchLabel);
    HBox batchBox = new HBox(8);
    batchSpinner = new Spinner<Integer>(1, 1000, 30);
    batchSpinner.setPrefWidth(80);
    Theme.styleSpinner(batchSpinner);
    batchRunBtn = new Button("EXECUTAR LOTE (V1-V4)");
    batchRunBtn.setMaxWidth(Double.MAX_VALUE);
    batchRunBtn.setOnAction(e -> {
      if (onBatchCallback != null) onBatchCallback.run();
    });
    HBox.setHgrow(batchRunBtn, Priority.ALWAYS);
    Theme.styleFlatButtonFilled(batchRunBtn);
    batchCancelBtn = new Button("CANCELAR");
    batchCancelBtn.setDisable(true);
    batchCancelBtn.setOnAction(e -> {
      if (onCancelCallback != null) onCancelCallback.run();
    });
    Theme.styleFlatButtonOutlined(batchCancelBtn);
    batchProgressLabel = new Label("");
    batchProgressLabel.setMinWidth(100);
    batchProgressLabel.setFont(Theme.FONT_12);
    batchProgressLabel.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + ";");
    batchBox.getChildren().addAll(batchSpinner, batchRunBtn, batchCancelBtn, batchProgressLabel);

    box.getChildren().addAll(
      buttonGrid,
      speedLabel, speedSlider,
      seedLabel, seedBox,
      replayLabel, replayBox,
      batchLabel, batchBox
    );
    return box;
  }

  /* ***************************************************************
  * Metodo: createResultsTab
  * Funcao: Cria a aba de resultados com tabela e grafico de comparacao
  * Parametros: nenhum
  * Retorno: VBox com o conteudo da aba
  *************************************************************** */
  private BarChart<String, Number> resultsChart;
  private XYChart.Series<String, Number> chartData;

  private VBox createResultsTab() {
    VBox box = new VBox(12);
    box.setPadding(new Insets(12));
    box.setFillWidth(true);
    Theme.stylePanel(box);

    Label resultsLabel = new Label("RESULTADOS POR VERSAO");
    Theme.styleSectionTitle(resultsLabel);

    // CheckBox escala log
    logScaleCheck = new CheckBox("Escala log");
    logScaleCheck.setSelected(true);
    Theme.styleCheckBox(logScaleCheck);
    logScaleCheck.setOnAction(e -> {
      controller.onLogScaleChanged(logScaleCheck.isSelected());
    });

    // Chart de comparativo de versoes
    resultsChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    resultsChart.setTitle("");
    resultsChart.setLegendVisible(false);
    resultsChart.setAlternativeRowFillVisible(false);
    resultsChart.setHorizontalGridLinesVisible(true);
    resultsChart.setVerticalGridLinesVisible(false);
    // Estilo: fundo painel, bordas, cores de texto
    resultsChart.setStyle(
      "-fx-background-color: " + Theme.PANEL + ";" +
      "-fx-background-radius: 0;" +
      "-fx-base: " + Theme.PANEL + ";" +
      "-fx-control-inner-background: " + Theme.PANEL + ";"
    );
    CategoryAxis xAxis = (CategoryAxis) resultsChart.getXAxis();
    xAxis.setStyle("-fx-label-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 11px; -fx-font-family: Monospaced;");
    xAxis.setCategories(javafx.collections.FXCollections.observableArrayList("V1", "V2", "V3", "V4"));
    NumberAxis yAxis = (NumberAxis) resultsChart.getYAxis();
    yAxis.setStyle("-fx-label-fill: " + Theme.TEXT_SECONDARY + "; -fx-font-size: 11px; -fx-font-family: Monospaced;");
    yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#ebdbb2"));
    yAxis.setLabel("Tempo (ms)");

    chartData = new XYChart.Series<>();
    chartData.setName("Media");
    resultsChart.getData().add(chartData);

    // Cores: barras em laranja queimado #d65d0e, texto em #ebdbb2
    for (Node node : resultsChart.lookupAll(".default-color0-tick")) {
      if (node instanceof javafx.scene.control.Control) {
        ((javafx.scene.control.Control) node).setStyle("-fx-background-color: " + Theme.ORANGE_FILL + "; -fx-text-fill: " + Theme.TEXT + ";");
      }
    }
    for (Node node : resultsChart.lookupAll(".chart-bar")) {
      node.setStyle("-fx-background-color: " + Theme.ORANGE_FILL + "; -fx-background-radius: 0;");
    }
    for (Node node : resultsChart.lookupAll(".axis-tick-mark")) {
      node.setStyle("-fx-background-color: " + Theme.TEXT_SECONDARY + ";");
    }
    for (Node node : resultsChart.lookupAll(".axis-label")) {
      node.setStyle("-fx-text-fill: " + Theme.TEXT_SECONDARY + ";");
    }

    box.getChildren().addAll(resultsLabel, logScaleCheck, resultsChart);
    return box;
  }

  /* ***************************************************************
  * Metodo: createMeasurementSection
  * Funcao: Cria a secao MEDICAO sempre visivel abaixo das abas
  * Parametros: nenhum
  * Retorno: VBox com a secao de medicao
  *************************************************************** */
  private VBox createMeasurementSection() {
    VBox box = new VBox(8);
    box.setPadding(new Insets(12));
    box.setFillWidth(true);
    Theme.stylePanel(box);

    // Titulo da secao
    Label titleLabel = new Label("MEDICAO");
    Theme.styleMeasurementTitle(titleLabel);

    // Linha ESTADO (reaproveita statusIndicator)
    HBox estadoRow = new HBox(12);
    estadoRow.setAlignment(Pos.CENTER_LEFT);
    Label estadoTitle = new Label("ESTADO");
    Theme.styleMeasurementTitle(estadoTitle);
    estadoTitle.setMinWidth(160);
    estadoRow.getChildren().addAll(estadoTitle, statusIndicator);

    // Linha CONFIGURACAO ATIVA
    HBox configRow = new HBox(12);
    configRow.setAlignment(Pos.CENTER_LEFT);
    Label configTitle = new Label("CONFIGURACAO ATIVA");
    Theme.styleMeasurementTitle(configTitle);
    configTitle.setMinWidth(160);
    measConfigLabel = new Label("--");
    Theme.styleMeasurementValue(measConfigLabel);
    configRow.getChildren().addAll(configTitle, measConfigLabel);

    // Linha PACOTES GERADOS
    HBox pacotesRow = new HBox(12);
    pacotesRow.setAlignment(Pos.CENTER_LEFT);
    Label pacotesTitle = new Label("PACOTES GERADOS");
    Theme.styleMeasurementTitle(pacotesTitle);
    pacotesTitle.setMinWidth(160);
    measPacotesLabel = new Label("0");
    Theme.styleMeasurementValue(measPacotesLabel);
    pacotesRow.getChildren().addAll(pacotesTitle, measPacotesLabel);

    // Linha TEMPO VIRTUAL (ms)
    HBox tempoRow = new HBox(12);
    tempoRow.setAlignment(Pos.CENTER_LEFT);
    Label tempoTitle = new Label("TEMPO VIRTUAL (MS)");
    Theme.styleMeasurementTitle(tempoTitle);
    tempoTitle.setMinWidth(160);
    measTempoLabel = new Label("0 ms");
    Theme.styleMeasurementValue(measTempoLabel);
    tempoRow.getChildren().addAll(tempoTitle, measTempoLabel);

    // Linha EM TRANSITO
    HBox transitoRow = new HBox(12);
    transitoRow.setAlignment(Pos.CENTER_LEFT);
    Label transitoTitle = new Label("EM TRANSITO");
    Theme.styleMeasurementTitle(transitoTitle);
    transitoTitle.setMinWidth(160);
    measEmTransitoLabel = new Label("0");
    Theme.styleMeasurementValue(measEmTransitoLabel);
    transitoRow.getChildren().addAll(transitoTitle, measEmTransitoLabel);

    // Bloco DESTINO ATINGIDO (destacado com borda esquerda laranja)
    VBox destinoBox = new VBox(4);
    Theme.styleDestinoAtingido(destinoBox);
    Label destinoTitle = new Label("DESTINO ATINGIDO");
    Theme.styleMeasurementTitle(destinoTitle);
    measDestinoLabel = new Label("--");
    Theme.styleMeasurementValue(measDestinoLabel);
    destinoBox.getChildren().addAll(destinoTitle, measDestinoLabel);

    box.getChildren().addAll(
      titleLabel,
      estadoRow, configRow, pacotesRow, tempoRow, transitoRow,
      destinoBox
    );
    return box;
  }

  // --- Getters para compatibilidade com SimulationController ---

  /* ***************************************************************
  * Metodo: getVersion
  * Funcao: Retorna a versao selecionada
  * Parametros: nenhum
  * Retorno: int - versao (1 a 4)
  *************************************************************** */
  public int getVersion() {
    return versionCombo.getValue();
  }

  /* ***************************************************************
  * Metodo: getSourceId
  * Funcao: Retorna o id do transmissor selecionado
  * Parametros: nenhum
  * Retorno: int - id do no
  *************************************************************** */
  public int getSourceId() {
    Integer val = sourceCombo.getValue();
    if (val == null) return 1;
    // Converte 0-based para 1-based (grafo usa ids 1..N)
    return val + 1;
  }

  /* ***************************************************************
  * Metodo: getDestinationId
  * Funcao: Retorna o id do receptor selecionado
  * Parametros: nenhum
  * Retorno: int - id do no
  *************************************************************** */
  public int getDestinationId() {
    Integer val = targetCombo.getValue();
    if (val == null) return 1;
    // Converte 0-based para 1-based (grafo usa ids 1..N)
    return val + 1;
  }

  /* ***************************************************************
  * Metodo: getTtl
  * Funcao: Retorna o TTL selecionado
  * Parametros: nenhum
  * Retorno: int - TTL
  *************************************************************** */
  public int getTtl() {
    return ttlSpinner.getValue();
  }

  /* ***************************************************************
  * Metodo: getWindowMs
  * Funcao: Retorna a janela V4 em milissegundos
  * Parametros: nenhum
  * Retorno: long - janela em ms
  *************************************************************** */
  public long getWindowMs() {
    return windowSpinner.getValue();
  }

  /* ***************************************************************
  * Metodo: getSeed
  * Funcao: Retorna a semente do campo de texto
  * Parametros: nenhum
  * Retorno: long - semente
  *************************************************************** */
  public long getSeed() {
    String text = seedField.getText();
    if (text == null || text.trim().isEmpty()) {
      return System.nanoTime();
    }
    try {
      return Long.parseLong(text.trim());
    } catch (NumberFormatException e) {
      return System.nanoTime();
    }
  }

  /* ***************************************************************
  * Metodo: getSpeed
  * Funcao: Retorna o fator de velocidade do slider
  * Parametros: nenhum
  * Retorno: double - fator de velocidade
  *************************************************************** */
  public double getSpeed() {
    return speedSlider.getValue();
  }

  // --- Callbacks ---

  /* ***************************************************************
  * Metodo: setOnStart
  * Funcao: Define callback para botao Iniciar
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnStart(Runnable callback) {
    this.onStartCallback = callback;
  }

  /* ***************************************************************
  * Metodo: setOnPause
  * Funcao: Define callback para botao Pausar
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnPause(Runnable callback) {
    this.onPauseCallback = callback;
  }

  /* ***************************************************************
  * Metodo: setOnReset
  * Funcao: Define callback para botao Resetar
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnReset(Runnable callback) {
    this.onResetCallback = callback;
  }

  /* ***************************************************************
  * Metodo: setOnEndpointsChange
  * Funcao: Define callback para mudanca de endpoints
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnEndpointsChange(Runnable callback) {
    this.onEndpointsChangeCallback = callback;
  }

  /* ***************************************************************
  * Metodo: setOnBatch
  * Funcao: Define callback para botao Lote
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnBatch(Runnable callback) {
    this.onBatchCallback = callback;
  }

  /* ***************************************************************
  * Metodo: setOnCancel
  * Funcao: Define callback para botao Cancelar lote
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnCancel(Runnable callback) {
    this.onCancelCallback = callback;
  }

  /* ***************************************************************
  * Metodo: getBatchCount
  * Funcao: Devolve o valor do spinner N execucoes
  * Parametros: nenhum
  * Retorno: int
  *************************************************************** */
  public int getBatchCount() {
    return batchSpinner != null ? batchSpinner.getValue() : 30;
  }

  /* ***************************************************************
  * Metodo: setBatchProgress
  * Funcao: Atualiza o label de progresso do lote (ex.: "V3  12/30")
  * Parametros: text - texto a exibir
  * Retorno: void
  *************************************************************** */
  public void setBatchProgress(String text) {
    if (batchProgressLabel != null) {
      batchProgressLabel.setText(text);
    }
  }

  /* ***************************************************************
  * Metodo: setBatchRunning
  * Funcao: Habilita/desabilita botoes do lote conforme estado
  * Parametros: running - true se lote em execucao
  * Retorno: void
  *************************************************************** */
  public void setBatchRunning(boolean running) {
    if (batchRunBtn != null) batchRunBtn.setDisable(running);
    if (batchCancelBtn != null) batchCancelBtn.setDisable(!running);
    if (batchSpinner != null) batchSpinner.setDisable(running);
  }

  /* ***************************************************************
  * Metodo: setOnStep
  * Funcao: Define callback para botao Proximo evento
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnStep(Runnable callback) {
    this.onStepCallback = callback;
  }

  /* ***************************************************************
  * Metodo: setOnRandomSeed
  * Funcao: Define callback para botao Sortear
  * Parametros: callback - Runnable a executar
  * Retorno: void
  *************************************************************** */
  public void setOnRandomSeed(Runnable callback) {
    this.onRandomSeedCallback = callback;
  }

  /* ***************************************************************
  * Metodo: updateReplayCommand
  * Funcao: Atualiza o campo de reproducao (compatibilidade)
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void updateReplayCommand() {
    // O campo replayField e atualizado automaticamente via setReplaySeed
  }

  /* ***************************************************************
  * Metodo: updateForNewGraph
  * Funcao: Atualiza os combos de origem/destino para novo grafo
  * Parametros: newGraph - novo grafo carregado
  * Retorno: void
  *************************************************************** */
  public void updateForNewGraph(Graph newGraph) {
    int currentSource = sourceCombo.getValue();
    int currentTarget = targetCombo.getValue();
    sourceCombo.getItems().clear();
    targetCombo.getItems().clear();
    for (int i = 1; i <= newGraph.getNodeCount(); i++) {
      sourceCombo.getItems().add(i);
      targetCombo.getItems().add(i);
    }
    if (sourceCombo.getItems().contains(currentSource)) {
      sourceCombo.setValue(currentSource);
    } else {
      sourceCombo.setValue(1);
    }
    if (targetCombo.getItems().contains(currentTarget)) {
      targetCombo.setValue(currentTarget);
    } else {
      targetCombo.setValue(newGraph.getNodeCount());
    }
    int diameter = newGraph.diameter();
    ttlSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, diameter));
  }

  /* ***************************************************************
  * Metodo: setStatus
  * Funcao: Define mensagem de status e atualiza label de finalizada se necessario
  * Parametros: msg - mensagem
  * Retorno: void
  *************************************************************** */
  public void setStatus(String msg) {
    if (overlayFinishedLabel != null) {
      final String message = msg;
      Platform.runLater(() -> {
        if (message.contains("Encerrada") && message.contains("pacotes na rede")) {
          overlayFinishedLabel.setText("Encerrada: nao ha mais pacotes na rede.");
          overlayFinishedLabel.setVisible(true);
          overlayFinishedLabel.setManaged(true);
        } else if (message.contains("Encerrada sem atingir")) {
          overlayFinishedLabel.setText("Encerrada sem atingir o destino (rede esvaziou).");
          overlayFinishedLabel.setVisible(true);
          overlayFinishedLabel.setManaged(true);
        } else {
          overlayFinishedLabel.setVisible(false);
          overlayFinishedLabel.setManaged(false);
        }
      });
    }
  }

  /* ***************************************************************
  * Metodo: setCounter
  * Funcao: Atualiza contador de pacotes no overlay e na secao MEDICAO
  * Parametros: count - numero de pacotes gerados
  * Retorno: void
  *************************************************************** */
  public void setCounter(long count) {
    if (overlayPacketsLabel != null) {
      Platform.runLater(() -> overlayPacketsLabel.setText(Long.toString(count)));
    }
    if (measPacotesLabel != null) {
      Platform.runLater(() -> measPacotesLabel.setText(Long.toString(count)));
    }
  }

  /* ***************************************************************
  * Metodo: setVirtualTime
  * Funcao: Atualiza tempo virtual no overlay e na secao MEDICAO
  * Parametros: time - tempo em ms
  * Retorno: void
  *************************************************************** */
  public void setVirtualTime(long time) {
    if (overlayTimeLabel != null) {
      Platform.runLater(() -> overlayTimeLabel.setText(time + " ms"));
    }
    if (measTempoLabel != null) {
      Platform.runLater(() -> measTempoLabel.setText(time + " ms"));
    }
  }

  /* ***************************************************************
  * Metodo: setEmTransito
  * Funcao: Atualiza contador de pacotes em transito na secao MEDICAO
  * Parametros: count - numero de pacotes em transito
  * Retorno: void
  *************************************************************** */
  public void setEmTransito(int count) {
    if (measEmTransitoLabel != null) {
      Platform.runLater(() -> measEmTransitoLabel.setText(Integer.toString(count)));
    }
  }

  /* ***************************************************************
  * Metodo: setConfiguracaoAtiva
  * Funcao: Atualiza texto de configuracao ativa na secao MEDICAO
  * Parametros: config - string formatada (ex.: "V3 A->H TTL 3")
  * Retorno: void
  *************************************************************** */
  public void setConfiguracaoAtiva(String config) {
    if (measConfigLabel != null) {
      Platform.runLater(() -> measConfigLabel.setText(config));
    }
  }

  /* ***************************************************************
  * Metodo: setDestinoAtingido
  * Funcao: Atualiza bloco DESTINO ATINGIDO na secao MEDICAO
  * Parametros: count - pacotes gerados; time - tempo em ms
  * Retorno: void
  *************************************************************** */
  public void setDestinoAtingido(long count, long time) {
    if (measDestinoLabel != null) {
      Platform.runLater(() -> measDestinoLabel.setText(count + " pacotes gerados em " + time + " ms"));
    }
  }

  /* ***************************************************************
  * Metodo: showSnapshot
  * Funcao: Mostra snapshot no overlay e na secao MEDICAO
  * Parametros: counter - contador no snapshot; time - tempo do snapshot
  * Retorno: void
  *************************************************************** */
  public void showSnapshot(long counter, long time) {
    if (overlaySnapshotLabel != null) {
      Platform.runLater(() -> overlaySnapshotLabel.setText("Snapshot: " + counter + " @ " + time + " ms"));
    }
    if (measDestinoLabel != null && counter > 0) {
      Platform.runLater(() -> measDestinoLabel.setText(counter + " pacotes gerados em " + time + " ms"));
    }
  }

  /* ***************************************************************
  * Metodo: setNextEventTime
  * Funcao: Define proximo tempo de evento (compatibilidade)
  * Parametros: time - tempo do proximo evento
  * Retorno: void
  *************************************************************** */
  public void setNextEventTime(long time) {
    // No-op
  }

  /* ***************************************************************
  * Metodo: setEstado
  * Funcao: Define o estado da simulacao e atualiza os botoes.
  * Parametros: estado - novo estado
  * Retorno: void
  *************************************************************** */
  public void setEstado(EstadoSimulacao estado) {
    this.estadoAtual = estado;
    atualizarBotoes();
  }

  /* ***************************************************************
  * Metodo: getEstado
  * Funcao: Retorna o estado atual da simulacao.
  * Parametros: nenhum
  * Retorno: EstadoSimulacao - estado atual
  *************************************************************** */
  public EstadoSimulacao getEstado() {
    return estadoAtual;
  }

  /* ***************************************************************
  * Metodo: setRunning
  * Funcao: Atualiza estado dos botoes para executando/pausado (compatibilidade).
  *         DEPRECADO: use setEstado().
  * Parametros: running - true se executando, false se pausado
  * Retorno: void
  *************************************************************** */
  public void setRunning(boolean running) {
    if (running) {
      setEstado(EstadoSimulacao.RODANDO);
    } else {
      // Se nao estava rodando, volta para ocioso ou encerrado
      if (estadoAtual == EstadoSimulacao.RODANDO) {
        setEstado(EstadoSimulacao.ENCERRADO);
      }
    }
  }

  /* ***************************************************************
  * Metodo: setPaused
  * Funcao: Atualiza estado pausado dos botoes (compatibilidade).
  *         DEPRECADO: use setEstado().
  * Parametros: paused - true se pausado
  * Retorno: void
  *************************************************************** */
  public void setPaused(boolean paused) {
    if (paused) {
      setEstado(EstadoSimulacao.PAUSADO);
    } else {
      if (estadoAtual == EstadoSimulacao.PAUSADO) {
        setEstado(EstadoSimulacao.RODANDO);
      }
    }
  }

  /* ***************************************************************
  * Metodo: resetDisplay
  * Funcao: Reseta a exibicao dos contadores e botoes.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void resetDisplay() {
    if (overlayPacketsLabel != null) overlayPacketsLabel.setText("0");
    if (overlayTimeLabel != null) overlayTimeLabel.setText("0 ms");
    if (overlaySnapshotLabel != null) overlaySnapshotLabel.setText("Snapshot: --");
    if (overlayFinishedLabel != null) {
      overlayFinishedLabel.setVisible(false);
      overlayFinishedLabel.setManaged(false);
    }
    if (measPacotesLabel != null) measPacotesLabel.setText("0");
    if (measTempoLabel != null) measTempoLabel.setText("0 ms");
    if (measEmTransitoLabel != null) measEmTransitoLabel.setText("0");
    if (measConfigLabel != null) measConfigLabel.setText("--");
    if (measDestinoLabel != null) measDestinoLabel.setText("--");
    setEstado(EstadoSimulacao.OCIOSO);
  }

  /* ***************************************************************
  * Metodo: updateCounters
  * Funcao: Atualiza contadores de pacotes e snapshot (compatibilidade com FloodingApp antigo).
  * Parametros: generatedCount - pacotes gerados; snapshotCounter - valor do snapshot
  * Retorno: void
  *************************************************************** */
  public void updateCounters(long generatedCount, long snapshotCounter) {
    setCounter(generatedCount);
    if (snapshotCounter > 0) {
      showSnapshot(snapshotCounter, 0);
    }
  }

  /* ***************************************************************
  * Metodo: onSimulationEnded
  * Funcao: Notifica fim de simulacao com resultado (compatibilidade).
  * Parametros: result - resultado da execucao
  * Retorno: void
  *************************************************************** */
  public void onSimulationEnded(RunResult result) {
    if (result != null && result.isReached()) {
      setStatus("Encerrada: destino atingido. Snapshot = " + result.getSnapshotCounter());
    } else {
      setStatus("Encerrada sem atingir o destino (rede esvaziou).");
    }
  }

  /* ***************************************************************
  * Metodo: reset
  * Funcao: Alias para resetDisplay (compatibilidade com FloodingApp antigo).
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void reset() {
    resetDisplay();
  }

  /* ***************************************************************
  * Metodo: setBatchProgress
  * Funcao: Atualiza progresso do lote (compatibilidade).
  * Parametros: current - atual; total - total
  * Retorno: void
  *************************************************************** */
  public void setBatchProgress(int current, int total) {
    if (batchProgressLabel != null) {
      batchProgressLabel.setText("V" + current + "/" + total);
    }
  }

  /* ***************************************************************
  * Metodo: atualizarBotoes
  * Funcao: Atualiza o estado dos botoes com base no estado explicito
  *         da simulacao.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void atualizarBotoes() {
    switch (estadoAtual) {
      case OCIOSO:
        startBtn.setDisable(false);
        Theme.styleFlatButtonFilled(startBtn);
        pauseBtn.setDisable(true);
        pauseBtn.setText("PAUSAR");
        Theme.styleButtonDisabled(pauseBtn);
        resetBtn.setDisable(true);
        Theme.styleButtonDisabled(resetBtn);
        stepBtn.setDisable(false);
        Theme.styleFlatButtonOutlined(stepBtn);
        break;
      case RODANDO:
        startBtn.setDisable(true);
        Theme.styleButtonDisabled(startBtn);
        pauseBtn.setDisable(false);
        pauseBtn.setText("PAUSAR");
        Theme.styleFlatButtonOutlined(pauseBtn);
        resetBtn.setDisable(false);
        Theme.styleFlatButtonOutlined(resetBtn);
        stepBtn.setDisable(true);
        Theme.styleButtonDisabled(stepBtn);
        break;
      case PAUSADO:
        startBtn.setDisable(true);
        Theme.styleButtonDisabled(startBtn);
        pauseBtn.setDisable(false);
        pauseBtn.setText("CONTINUAR");
        Theme.styleFlatButtonOutlined(pauseBtn);
        resetBtn.setDisable(false);
        Theme.styleFlatButtonOutlined(resetBtn);
        stepBtn.setDisable(false);
        Theme.styleFlatButtonOutlined(stepBtn);
        break;
      case ENCERRADO:
        startBtn.setDisable(false);
        Theme.styleFlatButtonFilled(startBtn);
        pauseBtn.setDisable(true);
        pauseBtn.setText("PAUSAR");
        Theme.styleButtonDisabled(pauseBtn);
        resetBtn.setDisable(false);
        Theme.styleFlatButtonOutlined(resetBtn);
        stepBtn.setDisable(true);
        Theme.styleButtonDisabled(stepBtn);
        break;
      case LOTE:
        startBtn.setDisable(true);
        Theme.styleButtonDisabled(startBtn);
        pauseBtn.setDisable(true);
        pauseBtn.setText("PAUSAR");
        Theme.styleButtonDisabled(pauseBtn);
        resetBtn.setDisable(true);
        Theme.styleButtonDisabled(resetBtn);
        stepBtn.setDisable(true);
        Theme.styleButtonDisabled(stepBtn);
        break;
    }
  }

  /* ***************************************************************
  * Metodo: setOverlayLabels
  * Funcao: Define os labels do overlay do cartao de contadores
  * Parametros: packets - label de pacotes; time - label de tempo; snapshot - label de snapshot; finished - label de finalizada
  * Retorno: void
  *************************************************************** */
  public void setOverlayLabels(Label packets, Label time, Label snapshot, Label finished) {
    this.overlayPacketsLabel = packets;
    this.overlayTimeLabel = time;
    this.overlaySnapshotLabel = snapshot;
    this.overlayFinishedLabel = finished;
  }

  /* ***************************************************************
  * Metodo: setReplaySeed
  * Funcao: Define a semente de reproducao no campo readonly
  * Parametros: seed - valor da semente
  * Retorno: void
  *************************************************************** */
  public void setReplaySeed(long seed) {
    replayField.setText(Long.toString(seed));
  }

  /* ***************************************************************
  * Metodo: getVersionCombo
  * Funcao: Retorna o ComboBox de versao
  * Parametros: nenhum
  * Retorno: ComboBox<Integer>
  *************************************************************** */
  public ComboBox<Integer> getVersionCombo() {
    return versionCombo;
  }

  /* ***************************************************************
  * Metodo: getSourceCombo
  * Funcao: Retorna o ComboBox de origem
  * Parametros: nenhum
  * Retorno: ComboBox<Integer>
  *************************************************************** */
  public ComboBox<Integer> getSourceCombo() {
    return sourceCombo;
  }

  /* ***************************************************************
  * Metodo: getTargetCombo
  * Funcao: Retorna o ComboBox de destino
  * Parametros: nenhum
  * Retorno: ComboBox<Integer>
  *************************************************************** */
  public ComboBox<Integer> getTargetCombo() {
    return targetCombo;
  }

  /* ***************************************************************
  * Metodo: getTtlSpinner
  * Funcao: Retorna o Spinner de TTL
  * Parametros: nenhum
  * Retorno: Spinner<Integer>
  *************************************************************** */
  public Spinner<Integer> getTtlSpinner() {
    return ttlSpinner;
  }

  /* ***************************************************************
  * Metodo: getWindowSpinner
  * Funcao: Retorna o Spinner de janela V4
  * Parametros: nenhum
  * Retorno: Spinner<Integer>
  *************************************************************** */
  public Spinner<Integer> getWindowSpinner() {
    return windowSpinner;
  }

  /* ***************************************************************
  * Metodo: getBatchCountSpinner
  * Funcao: Retorna o Spinner de contagem de lote
  * Parametros: nenhum
  * Retorno: Spinner<Integer>
  *************************************************************** */
  public Spinner<Integer> getBatchCountSpinner() {
    return batchCountSpinner;
  }

  /* ***************************************************************
  * Metodo: getSpeedSlider
  * Funcao: Retorna o Slider de velocidade
  * Parametros: nenhum
  * Retorno: Slider
  *************************************************************** */
  public Slider getSpeedSlider() {
    return speedSlider;
  }

  /* ***************************************************************
  * Metodo: getSeedField
  * Funcao: Retorna o TextField de semente
  * Parametros: nenhum
  * Retorno: TextField
  *************************************************************** */
  public TextField getSeedField() {
    return seedField;
  }

  /* ***************************************************************
  * Metodo: getReplayField
  * Funcao: Retorna o TextField de reproducao
  * Parametros: nenhum
  * Retorno: TextField
  *************************************************************** */
  public TextField getReplayField() {
    return replayField;
  }

  /* ***************************************************************
  * Metodo: getStartBtn
  * Funcao: Retorna o botao Iniciar
  * Parametros: nenhum
  * Retorno: Button
  *************************************************************** */
  public Button getStartBtn() {
    return startBtn;
  }

  /* ***************************************************************
  * Metodo: getPauseBtn
  * Funcao: Retorna o botao Pausar
  * Parametros: nenhum
  * Retorno: Button
  *************************************************************** */
  public Button getPauseBtn() {
    return pauseBtn;
  }

  /* ***************************************************************
  * Metodo: getResetBtn
  * Funcao: Retorna o botao Resetar
  * Parametros: nenhum
  * Retorno: Button
  *************************************************************** */
  public Button getResetBtn() {
    return resetBtn;
  }

  /* ***************************************************************
  * Metodo: getStepBtn
  * Funcao: Retorna o botao Proximo evento
  * Parametros: nenhum
  * Retorno: Button
  *************************************************************** */
  public Button getStepBtn() {
    return stepBtn;
  }

  /* ***************************************************************
  * Metodo: getLogScaleCheck
  * Funcao: Retorna o CheckBox de escala log
  * Parametros: nenhum
  * Retorno: CheckBox
  *************************************************************** */
  public CheckBox getLogScaleCheck() {
    return logScaleCheck;
  }
}//fim da classe ControlPanel