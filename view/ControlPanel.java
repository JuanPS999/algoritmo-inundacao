/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: ControlPanel
* Funcao...........: Painel lateral com os controles da simulacao:
*                    versao, transmissor, receptor, TTL, velocidade,
*                    botoes, contador de pacotes e snapshot.
*************************************************************** */
package view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Graph;

public class ControlPanel extends VBox {

  private static final String[] VERSION_NAMES = {
    "1 - Todas as interfaces",
    "2 - Exceto a de chegada",
    "3 - Exceto a de chegada + TTL",
    "4 - Versao 3 + otimizacao"
  };
  private static final String NO_SNAPSHOT = "Destino ainda nao atingido";

  private final ComboBox<String> versionBox = new ComboBox<String>();
  private final ComboBox<String> sourceBox = new ComboBox<String>();
  private final ComboBox<String> destinationBox = new ComboBox<String>();
  private final Spinner<Integer> ttlSpinner;
  private final Spinner<Integer> batchCountSpinner;
  private final Slider speedSlider = new Slider(0.25, 10.0, 2.0);
  private final Label speedLabel = new Label();
  private final Button startButton = new Button("Iniciar");
  private final Button pauseButton = new Button("Pausar");
  private final Button resetButton = new Button("Resetar");
  private final Button batchButton = new Button("Executar N vezes");
  private final Label counterLabel = new Label();
  private final Label timeLabel = new Label();
  private final Label snapshotLabel = new Label(NO_SNAPSHOT);
  private final Label statusLabel = new Label("Pronto.");
  private final Label batchProgressLabel = new Label("0/0");

  /* ***************************************************************
  * Metodo: ControlPanel
  * Funcao: construtor; monta todos os controles do painel.
  * Parametros: graph = grafo (rotulos dos nos e diametro para o TTL)
  * Retorno: nao se aplica
  *************************************************************** */
  public ControlPanel(Graph graph) {
    super(8.0);
    setPadding(new Insets(12.0));
    setPrefWidth(280.0);

    for (int i = 0; i < VERSION_NAMES.length; i++) {
      versionBox.getItems().add(VERSION_NAMES[i]);
    }//fim do for
    versionBox.getSelectionModel().select(2);

    for (int id = 1; id <= graph.getNodeCount(); id++) {
      String label = graph.getNode(id).getLabel();
      sourceBox.getItems().add(label);
      destinationBox.getItems().add(label);
    }//fim do for
    sourceBox.getSelectionModel().select(0);
    destinationBox.getSelectionModel().select(
        Math.min(3, graph.getNodeCount() - 1));

    ttlSpinner = new Spinner<Integer>(1, 99, Math.max(1, graph.diameter()));
    ttlSpinner.setEditable(true);

    batchCountSpinner = new Spinner<Integer>(1, 1000, 30);
    batchCountSpinner.setEditable(true);
    batchCountSpinner.setMaxWidth(Double.MAX_VALUE);

    versionBox.setMaxWidth(Double.MAX_VALUE);
    sourceBox.setMaxWidth(Double.MAX_VALUE);
    destinationBox.setMaxWidth(Double.MAX_VALUE);
    ttlSpinner.setMaxWidth(Double.MAX_VALUE);
    speedSlider.setMaxWidth(Double.MAX_VALUE);
    batchCountSpinner.setMaxWidth(Double.MAX_VALUE);

    speedSlider.valueProperty().addListener(
        (observable, oldValue, newValue) -> {
          updateSpeedLabel();
        });//fim do listener de velocidade
    updateSpeedLabel();

    pauseButton.setDisable(true);
    batchButton.setDisable(false);
    HBox buttons = new HBox(6.0, startButton, pauseButton, resetButton);
    HBox batchBox = new HBox(6.0, batchButton, batchProgressLabel);

    snapshotLabel.setWrapText(true);
    snapshotLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
    statusLabel.setWrapText(true);
    resetDisplay();

    getChildren().addAll(titleLabel("Versao do algoritmo"), versionBox,
        titleLabel("Transmissor"), sourceBox,
        titleLabel("Receptor"), destinationBox,
        titleLabel("TTL inicial (versoes 3 e 4)"), ttlSpinner,
        speedLabel, speedSlider, buttons,
        titleLabel("Execucao em lote"), batchCountSpinner, batchBox,
        titleLabel("Medicao"), counterLabel, timeLabel, snapshotLabel,
        statusLabel);
  }//fim do construtor ControlPanel

  /* ***************************************************************
  * Metodo: setOnStart
  * Funcao: registra a acao do botao Iniciar.
  * Parametros: action = acao a executar
  * Retorno: void
  *************************************************************** */
  public void setOnStart(Runnable action) {
    startButton.setOnAction(event -> action.run());
  }//fim do metodo setOnStart

  /* ***************************************************************
  * Metodo: setOnPause
  * Funcao: registra a acao do botao Pausar/Continuar.
  * Parametros: action = acao a executar
  * Retorno: void
  *************************************************************** */
  public void setOnPause(Runnable action) {
    pauseButton.setOnAction(event -> action.run());
  }//fim do metodo setOnPause

  /* ***************************************************************
  * Metodo: setOnReset
  * Funcao: registra a acao do botao Resetar.
  * Parametros: action = acao a executar
  * Retorno: void
  *************************************************************** */
  public void setOnReset(Runnable action) {
    resetButton.setOnAction(event -> action.run());
  }//fim do metodo setOnReset

  /* ***************************************************************
  * Metodo: setOnEndpointsChange
  * Funcao: avisa quando transmissor ou receptor mudam.
  * Parametros: action = acao a executar
  * Retorno: void
  *************************************************************** */
  public void setOnEndpointsChange(Runnable action) {
    sourceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      action.run();
    });//fim do listener do transmissor
    destinationBox.valueProperty().addListener(
        (observable, oldValue, newValue) -> {
          action.run();
        });//fim do listener do receptor
  }//fim do metodo setOnEndpointsChange

  /* ***************************************************************
  * Metodo: getVersion
  * Funcao: informa a versao escolhida.
  * Parametros: nenhum
  * Retorno: int de 1 a 4
  *************************************************************** */
  public int getVersion() {
    return versionBox.getSelectionModel().getSelectedIndex() + 1;
  }//fim do metodo getVersion

  /* ***************************************************************
  * Metodo: getSourceId
  * Funcao: informa o id do transmissor escolhido.
  * Parametros: nenhum
  * Retorno: int com o id do no (comeca em 1)
  *************************************************************** */
  public int getSourceId() {
    return sourceBox.getSelectionModel().getSelectedIndex() + 1;
  }//fim do metodo getSourceId

  /* ***************************************************************
  * Metodo: getDestinationId
  * Funcao: informa o id do receptor escolhido.
  * Parametros: nenhum
  * Retorno: int com o id do no (comeca em 1)
  *************************************************************** */
  public int getDestinationId() {
    return destinationBox.getSelectionModel().getSelectedIndex() + 1;
  }//fim do metodo getDestinationId

  /* ***************************************************************
  * Metodo: getTtl
  * Funcao: le o TTL inicial (aceita o valor digitado no campo).
  * Parametros: nenhum
  * Retorno: int com o TTL, no minimo 1
  *************************************************************** */
  public int getTtl() {
    try {
      int typed = Integer.parseInt(ttlSpinner.getEditor().getText().trim());
      return Math.max(1, typed);
    }//fim do try
    catch (NumberFormatException exception) {
      return ttlSpinner.getValue().intValue();
    }//fim do catch
  }//fim do metodo getTtl

  /* ***************************************************************
  * Metodo: getBatchCount
  * Funcao: informa o numero de execucoes do lote.
  * Parametros: nenhum
  * Retorno: int com o numero de execucoes (minimo 1)
  *************************************************************** */
  public int getBatchCount() {
    try {
      int typed = Integer.parseInt(batchCountSpinner.getEditor().getText().trim());
      return Math.max(1, typed);
    }//fim do try
    catch (NumberFormatException exception) {
      return batchCountSpinner.getValue().intValue();
    }//fim do catch
  }//fim do metodo getBatchCount

  /* ***************************************************************
  * Metodo: setOnBatch
  * Funcao: registra a acao do botao Executar N vezes.
  * Parametros: action = acao a executar
  * Retorno: void
  *************************************************************** */
  public void setOnBatch(Runnable action) {
    batchButton.setOnAction(event -> action.run());
  }//fim do metodo setOnBatch

  /* ***************************************************************
  * Metodo: setBatchProgress
  * Funcao: atualiza o rotulo de progresso do lote.
  * Parametros: current = execucao atual; total = total de execucoes
  * Retorno: void
  *************************************************************** */
  public void setBatchProgress(int current, int total) {
    batchProgressLabel.setText(current + "/" + total);
  }//fim do metodo setBatchProgress

  /* ***************************************************************
  * Metodo: getSpeed
  * Funcao: informa o fator de velocidade escolhido no slider.
  * Parametros: nenhum
  * Retorno: double entre 0.25 e 10.0
  *************************************************************** */
  public double getSpeed() {
    return speedSlider.getValue();
  }//fim do metodo getSpeed

  /* ***************************************************************
  * Metodo: setCounter
  * Funcao: mostra o contador de pacotes gerados.
  * Parametros: packets = pacotes gerados ate agora
  * Retorno: void
  *************************************************************** */
  public void setCounter(long packets) {
    counterLabel.setText("Pacotes gerados: " + packets);
  }//fim do metodo setCounter

  /* ***************************************************************
  * Metodo: setVirtualTime
  * Funcao: mostra o relogio virtual da simulacao.
  * Parametros: millis = tempo virtual em milissegundos
  * Retorno: void
  *************************************************************** */
  public void setVirtualTime(long millis) {
    timeLabel.setText("Tempo virtual: " + millis + " ms");
  }//fim do metodo setVirtualTime

  /* ***************************************************************
  * Metodo: showSnapshot
  * Funcao: destaca o contador no instante da primeira chegada.
  * Parametros: packets = pacotes gerados; millis = tempo virtual
  * Retorno: void
  *************************************************************** */
  public void showSnapshot(long packets, long millis) {
    snapshotLabel.setText("Destino atingido: " + packets
        + " pacotes gerados (t = " + millis + " ms)");
  }//fim do metodo showSnapshot

  /* ***************************************************************
  * Metodo: setStatus
  * Funcao: mostra uma mensagem de estado para o usuario.
  * Parametros: message = texto da mensagem
  * Retorno: void
  *************************************************************** */
  public void setStatus(String message) {
    statusLabel.setText(message);
  }//fim do metodo setStatus

  /* ***************************************************************
  * Metodo: resetDisplay
  * Funcao: zera contador, tempo e snapshot.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  public void resetDisplay() {
    setCounter(0);
    setVirtualTime(0);
    snapshotLabel.setText(NO_SNAPSHOT);
  }//fim do metodo resetDisplay

  /* ***************************************************************
  * Metodo: setRunning
  * Funcao: trava os controles de escolha enquanto a simulacao roda.
  * Parametros: running = true se ha simulacao em andamento
  * Retorno: void
  *************************************************************** */
  public void setRunning(boolean running) {
    versionBox.setDisable(running);
    sourceBox.setDisable(running);
    destinationBox.setDisable(running);
    ttlSpinner.setDisable(running);
    batchCountSpinner.setDisable(running);
    startButton.setDisable(running);
    pauseButton.setDisable(!running);
    batchButton.setDisable(running);
  }//fim do metodo setRunning

  /* ***************************************************************
  * Metodo: setPaused
  * Funcao: alterna o texto do botao entre Pausar e Continuar.
  * Parametros: paused = true se a simulacao esta pausada
  * Retorno: void
  *************************************************************** */
  public void setPaused(boolean paused) {
    pauseButton.setText(paused ? "Continuar" : "Pausar");
  }//fim do metodo setPaused

  /* ***************************************************************
  * Metodo: updateSpeedLabel
  * Funcao: atualiza o texto com o fator de velocidade atual.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void updateSpeedLabel() {
    speedLabel.setText(String.format("Velocidade: %.2fx",
        speedSlider.getValue()));
  }//fim do metodo updateSpeedLabel

  /* ***************************************************************
  * Metodo: titleLabel
  * Funcao: cria um rotulo de titulo em negrito.
  * Parametros: text = texto do rotulo
  * Retorno: Label formatado
  *************************************************************** */
  private Label titleLabel(String text) {
    Label label = new Label(text);
    label.setStyle("-fx-font-weight: bold;");
    return label;
  }//fim do metodo titleLabel

}//fim da classe ControlPanel
