/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: ControlPanel
* Funcao...........: Painel de controle com botoes, seletores e configuracoes
*************************************************************** */
package view;

import controller.SimulationController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Graph;
import model.RunResult;
import util.NodeLabelUtil;

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

  public ControlPanel(SimulationController controller, Graph graph) {
    this.controller = controller;
    this.graph = graph;
    getStyleClass().add("control-panel");
    setSpacing(16);
    setPadding(new Insets(12));
    setMinWidth(360);
    setPrefWidth(360);
    setMaxWidth(360);
    build();
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
  * Metodo: build
  * Funcao: Constrói a interface do painel de controle
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void build() {
    // Abas
    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.getStyleClass().add("control-tabpane");

    // Aba Cenario
    Tab scenarioTab = new Tab("Cenario");
    scenarioTab.setContent(createScenarioTab());
    tabPane.getTabs().add(scenarioTab);

    // Aba Execucao
    Tab executionTab = new Tab("Execucao");
    executionTab.setContent(createExecutionTab());
    tabPane.getTabs().add(executionTab);

    // Aba Resultados
    Tab resultsTab = new Tab("Resultados");
    resultsTab.setContent(createResultsTab());
    tabPane.getTabs().add(resultsTab);

    getChildren().add(tabPane);
    VBox.setVgrow(tabPane, Priority.ALWAYS);
  }

  /* ***************************************************************
  * Metodo: createScenarioTab
  * Funcao: Cria a aba de cenario com versao, origem, destino, TTL, janela
  * Parametros: nenhum
  * Retorno: VBox com o conteudo da aba
  *************************************************************** */
  private VBox createScenarioTab() {
    VBox box = new VBox(12);
    box.getStyleClass().add("secao");
    box.setPadding(new Insets(12));
    box.setFillWidth(true);

    // Versao
    Label versionLabel = new Label("Versao");
    versionLabel.getStyleClass().add("secao-titulo");
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
    versionCombo.setOnAction(e -> controller.onVersionChanged(versionCombo.getValue()));

    // Origem
    Label sourceLabel = new Label("Transmissor");
    sourceLabel.getStyleClass().add("secao-titulo");
    sourceCombo = new ComboBox<>();
    for (int i = 1; i <= graph.getNodeCount(); i++) {
      sourceCombo.getItems().add(i);
    }
    sourceCombo.setValue(1);
    sourceCombo.setMaxWidth(Double.MAX_VALUE);
    sourceCombo.setConverter(new StringConverter<Integer>() {
      @Override
      public String toString(Integer id) {
        return id == null ? "" : NodeLabelUtil.toLabel(id);
      }

      @Override
      public Integer fromString(String s) {
        return NodeLabelUtil.fromLabel(s);
      }
    });
    sourceCombo.setOnAction(e -> controller.onSourceChanged(sourceCombo.getValue()));

    // Destino
    Label targetLabel = new Label("Receptor");
    targetLabel.getStyleClass().add("secao-titulo");
    targetCombo = new ComboBox<>();
    for (int i = 1; i <= graph.getNodeCount(); i++) {
      targetCombo.getItems().add(i);
    }
    targetCombo.setValue(graph.getNodeCount());
    targetCombo.setMaxWidth(Double.MAX_VALUE);
    targetCombo.setConverter(new StringConverter<Integer>() {
      @Override
      public String toString(Integer id) {
        return id == null ? "" : NodeLabelUtil.toLabel(id);
      }

      @Override
      public Integer fromString(String s) {
        return NodeLabelUtil.fromLabel(s);
      }
    });
    targetCombo.setOnAction(e -> controller.onTargetChanged(targetCombo.getValue()));

    // TTL
    Label ttlLabel = new Label("TTL");
    ttlLabel.getStyleClass().add("secao-titulo");
    int diameter = graph.diameter();
    ttlSpinner = new Spinner<>(1, 100, diameter);
    ttlSpinner.setEditable(true);
    ttlSpinner.setMaxWidth(Double.MAX_VALUE);
    ttlSpinner.valueProperty().addListener((obs, o, n) -> controller.onTtlChanged(n));

    // Janela V4 (ms)
    Label windowLabel = new Label("Janela V4 (ms)");
    windowLabel.getStyleClass().add("secao-titulo");
    windowSpinner = new Spinner<>(0, 10000, 0);
    windowSpinner.setEditable(true);
    windowSpinner.setMaxWidth(Double.MAX_VALUE);
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
    box.getStyleClass().add("secao");
    box.setPadding(new Insets(12));
    box.setFillWidth(true);

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

    startBtn = new Button("Iniciar");
    startBtn.setMaxWidth(Double.MAX_VALUE);
    startBtn.setOnAction(e -> {
      if (onStartCallback != null) onStartCallback.run();
    });
    GridPane.setFillWidth(startBtn, true);

    pauseBtn = new Button("Pausar");
    pauseBtn.setMaxWidth(Double.MAX_VALUE);
    pauseBtn.setOnAction(e -> {
      if (onPauseCallback != null) onPauseCallback.run();
    });
    pauseBtn.setDisable(true);
    GridPane.setFillWidth(pauseBtn, true);

    resetBtn = new Button("Resetar");
    resetBtn.setMaxWidth(Double.MAX_VALUE);
    resetBtn.setOnAction(e -> {
      if (onResetCallback != null) onResetCallback.run();
    });
    GridPane.setFillWidth(resetBtn, true);

    stepBtn = new Button("Proximo evento");
    stepBtn.setMaxWidth(Double.MAX_VALUE);
    stepBtn.setOnAction(e -> {
      if (onStepCallback != null) onStepCallback.run();
    });
    stepBtn.setDisable(true);
    GridPane.setFillWidth(stepBtn, true);

    buttonGrid.add(startBtn, 0, 0);
    buttonGrid.add(pauseBtn, 1, 0);
    buttonGrid.add(resetBtn, 0, 1);
    buttonGrid.add(stepBtn, 1, 1);

    // Velocidade
    Label speedLabel = new Label("Velocidade");
    speedLabel.getStyleClass().add("secao-titulo");
    speedSlider = new Slider(0.25, 10.0, 2.0);
    speedSlider.setShowTickLabels(true);
    speedSlider.setShowTickMarks(true);
    speedSlider.setMajorTickUnit(1.0);
    speedSlider.setMinorTickCount(3);
    speedSlider.setBlockIncrement(0.25);
    speedSlider.valueProperty().addListener((obs, o, n) -> controller.onSpeedChanged(n.doubleValue()));

    // Semente
    Label seedLabel = new Label("Semente");
    seedLabel.getStyleClass().add("secao-titulo");
    HBox seedBox = new HBox(8);
    seedField = new TextField();
    seedField.setPromptText("Deixe vazio para aleatorio");
    HBox.setHgrow(seedField, Priority.ALWAYS);
    randomSeedBtn = new Button("Sortear");
    randomSeedBtn.setOnAction(e -> {
      long seed = System.currentTimeMillis() ^ (long) (Math.random() * 1000000);
      seedField.setText(Long.toString(seed));
      controller.onSeedChanged(seed);
      if (onRandomSeedCallback != null) onRandomSeedCallback.run();
    });
    seedBox.getChildren().addAll(seedField, randomSeedBtn);

    // Reproducao
    Label replayLabel = new Label("Reproducao");
    replayLabel.getStyleClass().add("secao-titulo");
    HBox replayBox = new HBox(8);
    replayField = new TextField();
    replayField.setEditable(false);
    replayField.setPromptText("Semente usada na ultima execucao");
    HBox.setHgrow(replayField, Priority.ALWAYS);
    copyReplayBtn = new Button("Copiar");
    copyReplayBtn.setOnAction(e -> {
      javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
      javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
      content.putString(replayField.getText());
      clipboard.setContent(content);
    });
    replayBox.getChildren().addAll(replayField, copyReplayBtn);

    box.getChildren().addAll(
      buttonGrid,
      speedLabel, speedSlider,
      seedLabel, seedBox,
      replayLabel, replayBox
    );
    return box;
  }

  /* ***************************************************************
  * Metodo: createResultsTab
  * Funcao: Cria a aba de resultados com tabela e grafico de comparacao
  * Parametros: nenhum
  * Retorno: VBox com o conteudo da aba
  *************************************************************** */
  private VBox createResultsTab() {
    VBox box = new VBox(12);
    box.getStyleClass().add("secao");
    box.setPadding(new Insets(12));
    box.setFillWidth(true);

    Label resultsLabel = new Label("Resultados por versao");
    resultsLabel.getStyleClass().add("secao-titulo");

    // CheckBox escala log
    logScaleCheck = new CheckBox("Escala log");
    logScaleCheck.setSelected(true);
    logScaleCheck.setOnAction(e -> controller.onLogScaleChanged(logScaleCheck.isSelected()));

    box.getChildren().addAll(resultsLabel, logScaleCheck);
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
    return sourceCombo.getValue();
  }

  /* ***************************************************************
  * Metodo: getDestinationId
  * Funcao: Retorna o id do receptor selecionado
  * Parametros: nenhum
  * Retorno: int - id do no
  *************************************************************** */
  public int getDestinationId() {
    return targetCombo.getValue();
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
  * Metodo: getBatchCount
  * Funcao: Retorna a contagem de lote (padrao 30)
  * Parametros: nenhum
  * Retorno: int - contagem
  *************************************************************** */
  public int getBatchCount() {
    return 30;
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
  * Funcao: Atualiza contador de pacotes no overlay
  * Parametros: count - numero de pacotes gerados
  * Retorno: void
  *************************************************************** */
  public void setCounter(long count) {
    if (overlayPacketsLabel != null) {
      Platform.runLater(() -> overlayPacketsLabel.setText(Long.toString(count)));
    }
  }

  /* ***************************************************************
  * Metodo: setVirtualTime
  * Funcao: Atualiza tempo virtual no overlay
  * Parametros: time - tempo em ms
  * Retorno: void
  *************************************************************** */
  public void setVirtualTime(long time) {
    if (overlayTimeLabel != null) {
      Platform.runLater(() -> overlayTimeLabel.setText(time + " ms"));
    }
  }

  /* ***************************************************************
  * Metodo: showSnapshot
  * Funcao: Mostra snapshot no overlay
  * Parametros: counter - contador no snapshot; time - tempo do snapshot
  * Retorno: void
  *************************************************************** */
  public void showSnapshot(long counter, long time) {
    if (overlaySnapshotLabel != null) {
      Platform.runLater(() -> overlaySnapshotLabel.setText("Snapshot: " + counter + " @ " + time + " ms"));
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
    // No-op para compatibilidade
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
        pauseBtn.setDisable(true);
        pauseBtn.setText("Pausar");
        resetBtn.setDisable(true);
        stepBtn.setDisable(false);
        break;
      case RODANDO:
        startBtn.setDisable(true);
        pauseBtn.setDisable(false);
        pauseBtn.setText("Pausar");
        resetBtn.setDisable(false);
        stepBtn.setDisable(true);
        break;
      case PAUSADO:
        startBtn.setDisable(true);
        pauseBtn.setDisable(false);
        pauseBtn.setText("Continuar");
        resetBtn.setDisable(false);
        stepBtn.setDisable(false);
        break;
      case ENCERRADO:
        startBtn.setDisable(false);
        pauseBtn.setDisable(true);
        pauseBtn.setText("Pausar");
        resetBtn.setDisable(false);
        stepBtn.setDisable(true);
        break;
      case LOTE:
        startBtn.setDisable(true);
        pauseBtn.setDisable(true);
        pauseBtn.setText("Pausar");
        resetBtn.setDisable(true);
        stepBtn.setDisable(true);
        break;
    }
  }

  /* ***************************************************************
  * Metodo: setOverlayLabels
  * Funcao: Define os labels do overlay do cartao de contadores
  * Parametros: packets - label de pacotes; time - label de tempo; snapshot - label de snapshot; finished - label de finalizada
  * Retorno: void
  *************************************************************** */
  private Label overlayFinishedLabel;

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