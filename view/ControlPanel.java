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
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
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
    private final Spinner<Integer> windowSpinner;
    private final Slider speedSlider = new Slider(0.25, 10.0, 2.0);
    private final Label speedLabel = new Label();
    private final Button startButton = new Button("Iniciar");
    private final Button pauseButton = new Button("Pausar");
    private final Button resetButton = new Button("Resetar");
    private final Button stepButton = new Button("Proximo evento");
    private final Button batchButton = new Button("Executar N vezes");
    private final Button randomSeedButton = new Button("Sortear");
    private final Label counterLabel = new Label();
    private final Label timeLabel = new Label();
    private final Label snapshotLabel = new Label(NO_SNAPSHOT);
    private final Label statusLabel = new Label("Pronto.");
    private final Label batchProgressLabel = new Label("0/0");
    private final Label nextEventLabel = new Label("Fila vazia");
    private final TextField seedField = new TextField();
    private final TextField replayField = new TextField();

    private long currentSeed = System.nanoTime();

    /* ***************************************************************
    * Metodo: ControlPanel
    * Funcao: construtor; monta todos os controles do painel.
    * Parametros: graph = grafo (rotulos dos nos e diametro para o TTL)
    * Retorno: nao se aplica
    *************************************************************** */
    public ControlPanel(Graph graph) {
        super(8.0);
        setPadding(new Insets(12.0));
        setPrefWidth(320.0);

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

        windowSpinner = new Spinner<Integer>(0, 10000, 1000);
        windowSpinner.setEditable(true);
        windowSpinner.setMaxWidth(Double.MAX_VALUE);
        windowSpinner.setDisable(true); // habilitado apenas na V4

        versionBox.setMaxWidth(Double.MAX_VALUE);
        sourceBox.setMaxWidth(Double.MAX_VALUE);
        destinationBox.setMaxWidth(Double.MAX_VALUE);
        ttlSpinner.setMaxWidth(Double.MAX_VALUE);
        speedSlider.setMaxWidth(Double.MAX_VALUE);
        batchCountSpinner.setMaxWidth(Double.MAX_VALUE);
        windowSpinner.setMaxWidth(Double.MAX_VALUE);

        seedField.setMaxWidth(Double.MAX_VALUE);
        seedField.setText(String.valueOf(currentSeed));
        seedField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateSeedField(newVal);
        });

        replayField.setMaxWidth(Double.MAX_VALUE);
        replayField.setEditable(false);

        speedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateSpeedLabel();
                });//fim do listener de velocidade
        updateSpeedLabel();

        // listener para mudar estado do windowSpinner conforme versao
        versionBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateWindowSpinnerState();
            updateReplayCommand();
        });

        pauseButton.setDisable(true);
        batchButton.setDisable(false);
        stepButton.setDisable(true);
        HBox buttons = new HBox(6.0, startButton, pauseButton, resetButton, stepButton);
        HBox batchBox = new HBox(6.0, batchButton, batchProgressLabel);
        HBox seedBox = new HBox(6.0, new Label("Semente:"), seedField, randomSeedButton);
        HBox windowBox = new HBox(6.0, new Label("Janela V4 (ms):"), windowSpinner);
        HBox nextEventBox = new HBox(6.0, new Label("Proximo evento:"), nextEventLabel);
        HBox replayBox = new HBox(6.0, new Label("Reproducao:"), replayField);

        snapshotLabel.setWrapText(true);
        snapshotLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        statusLabel.setWrapText(true);
        resetDisplay();

        getChildren().addAll(
                titleLabel("Versao do algoritmo"), versionBox,
                titleLabel("Transmissor"), sourceBox,
                titleLabel("Receptor"), destinationBox,
                titleLabel("TTL inicial (versoes 3 e 4)"), ttlSpinner,
                titleLabel("Semente"), seedBox,
                windowBox,
                speedLabel, speedSlider,
                buttons,
                titleLabel("Execucao em lote"), batchCountSpinner, batchBox,
                nextEventBox,
                titleLabel("Reproducao"), replayBox,
                titleLabel("Medicao"), counterLabel, timeLabel, snapshotLabel,
                statusLabel);
    }//fim do construtor ControlPanel

    /* ***************************************************************
    * Metodo: validateSeedField
    * Funcao: valida o campo de semente; se invalido, borda vermelha
    *         e mantem valor anterior.
    * Parametros: text = texto digitado
    * Retorno: void
    *************************************************************** */
    private void validateSeedField(String text) {
        try {
            long seed = Long.parseLong(text.trim());
            currentSeed = seed;
            seedField.setStyle("");
        } catch (NumberFormatException e) {
            seedField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            seedField.setText(String.valueOf(currentSeed));
        }
    }//fim do metodo validateSeedField

    /* ***************************************************************
    * Metodo: updateWindowSpinnerState
    * Funcao: habilita/desabilita o spinner de janela conforme versao.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void updateWindowSpinnerState() {
        int version = getVersion();
        windowSpinner.setDisable(version != 4);
    }//fim do metodo updateWindowSpinnerState

    /* ***************************************************************
    * Metodo: updateReplayCommand
    * Funcao: atualiza o campo de comando de reproducao.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void updateReplayCommand() {
        int version = getVersion();
        int source = getSourceId();
        int destination = getDestinationId();
        int ttl = getTtl();
        long seed = getSeed();
        StringBuilder cmd = new StringBuilder();
        cmd.append("java -cp build util.ConsoleRunner ");
        cmd.append(version).append(" ");
        cmd.append(source).append(" ");
        cmd.append(destination).append(" ");
        cmd.append(ttl).append(" ");
        cmd.append(seed);
        if (version == 4) {
            cmd.append(" janela=").append(getWindowMs());
        }
        replayField.setText(cmd.toString());
    }//fim do metodo updateReplayCommand

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
    * Metodo: setOnStep
    * Funcao: registra a acao do botao Proximo evento.
    * Parametros: action = acao a executar
    * Retorno: void
    *************************************************************** */
    public void setOnStep(Runnable action) {
        stepButton.setOnAction(event -> action.run());
    }//fim do metodo setOnStep

    /* ***************************************************************
    * Metodo: setOnEndpointsChange
    * Funcao: avisa quando transmissor ou receptor mudam.
    * Parametros: action = acao a executar
    * Retorno: void
    *************************************************************** */
    public void setOnEndpointsChange(Runnable action) {
        sourceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            action.run();
            updateReplayCommand();
        });//fim do listener do transmissor
        destinationBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    action.run();
                    updateReplayCommand();
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
    * Metodo: getWindowMs
    * Funcao: informa a janela de deduplicacao (ms) para V4.
    * Parametros: nenhum
    * Retorno: long com a janela em ms
    *************************************************************** */
    public long getWindowMs() {
        try {
            int typed = Integer.parseInt(windowSpinner.getEditor().getText().trim());
            return Math.max(0, typed);
        }//fim do try
        catch (NumberFormatException exception) {
            return windowSpinner.getValue().longValue();
        }//fim do catch
    }//fim do metodo getWindowMs

    /* ***************************************************************
    * Metodo: getSeed
    * Funcao: informa a semente atual.
    * Parametros: nenhum
    * Retorno: long - semente
    *************************************************************** */
    public long getSeed() {
        return currentSeed;
    }//fim do metodo getSeed

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
    * Metodo: setOnRandomSeed
    * Funcao: registra a acao do botao Sortear semente.
    * Parametros: action = acao a executar
    * Retorno: void
    *************************************************************** */
    public void setOnRandomSeed(Runnable action) {
        randomSeedButton.setOnAction(event -> action.run());
    }//fim do metodo setOnRandomSeed

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
        nextEventLabel.setText("Fila vazia");
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
        windowSpinner.setDisable(running || getVersion() != 4);
        startButton.setDisable(running);
        pauseButton.setDisable(!running);
        stepButton.setDisable(!running);
        batchButton.setDisable(running);
        seedField.setDisable(running);
        randomSeedButton.setDisable(running);
    }//fim do metodo setRunning

    private boolean paused = false;

    /* ***************************************************************
    * Metodo: setPaused
    * Funcao: alterna o texto do botao entre Pausar e Continuar.
    * Parametros: paused = true se a simulacao esta pausada
    * Retorno: void
    *************************************************************** */
    public void setPaused(boolean paused) {
        this.paused = paused;
        pauseButton.setText(paused ? "Continuar" : "Pausar");
        stepButton.setDisable(!running || !paused);
    }//fim do metodo setPaused

    private boolean running = false;

    /* ***************************************************************
    * Metodo: setNextEventTime
    * Funcao: atualiza o label do proximo evento.
    * Parametros: nextTime = tempo do proximo evento ou Long.MAX_VALUE
    * Retorno: void
    *************************************************************** */
    public void setNextEventTime(long nextTime) {
        if (nextTime == Long.MAX_VALUE) {
            nextEventLabel.setText("Fila vazia");
        } else {
            nextEventLabel.setText("t = " + nextTime + " ms");
        }
    }//fim do metodo setNextEventTime

    /* ***************************************************************
    * Metodo: getSeedField
    * Funcao: retorna o campo de texto da semente (para uso interno).
    * Parametros: nenhum
    * Retorno: TextField
    *************************************************************** */
    public TextField getSeedField() {
        return seedField;
    }//fim do metodo getSeedField

    /* ***************************************************************
    * Metodo: updateForNewGraph
    * Funcao: atualiza o painel para um novo grafo recarregado.
    * Parametros: graph = novo grafo
    * Retorno: void
    *************************************************************** */
    public void updateForNewGraph(Graph graph) {
        sourceBox.getItems().clear();
        destinationBox.getItems().clear();
        for (int id = 1; id <= graph.getNodeCount(); id++) {
            String label = graph.getNode(id).getLabel();
            sourceBox.getItems().add(label);
            destinationBox.getItems().add(label);
        }
        sourceBox.getSelectionModel().select(0);
        destinationBox.getSelectionModel().select(
                Math.min(3, graph.getNodeCount() - 1));
        ttlSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, 99, Math.max(1, graph.diameter())));
        updateReplayCommand();
    }//fim do metodo updateForNewGraph

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