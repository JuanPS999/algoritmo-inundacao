/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: ControlPanel
* Funcao...........: Painel lateral com os controles da simulacao
*                    organizados em secoes: Cenario, Execucao, Lote.
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
import javafx.scene.control.Tooltip;
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
    private final Spinner<Integer> ttlSpinner = new Spinner<Integer>();
    private final Spinner<Integer> batchCountSpinner = new Spinner<Integer>();
    private final Spinner<Integer> windowSpinner = new Spinner<Integer>();
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
    private boolean running = false;
    private boolean paused = false;

    // Labels de overlay no grafo (atualizados pelo controller)
    private Label overlayPackets = null;
    private Label overlayTime = null;
    private Label overlaySnapshot = null;

    /* ***************************************************************
    * Metodo: ControlPanel
    * Funcao: construtor; monta todos os controis em secoes.
    * Parametros: graph = grafo (rotulos dos nos e diametro para o TTL)
    * Retorno: nao se aplica
    *************************************************************** */
    public ControlPanel(Graph graph) {
        super(8.0);
        setPadding(new Insets(12.0));
        setPrefWidth(320.0);
        getStyleClass().add("secao");

        // Inicializa controles
        initControls(graph);
        buildLayout();
        setupListeners();
        resetDisplay();
    }//fim do construtor ControlPanel

    /* ***************************************************************
    * Metodo: initControls
    * Funcao: inicializa todos os controles com valores padrao.
    * Parametros: graph = grafo
    * Retorno: void
    *************************************************************** */
    private void initControls(Graph graph) {
        for (int i = 0; i < VERSION_NAMES.length; i++) {
            versionBox.getItems().add(VERSION_NAMES[i]);
        }
        versionBox.getSelectionModel().select(2);
        versionBox.setMaxWidth(Double.MAX_VALUE);
        versionBox.setTooltip(new Tooltip("Versao do algoritmo de flooding"));

        for (int id = 1; id <= graph.getNodeCount(); id++) {
            String label = graph.getNode(id).getLabel();
            sourceBox.getItems().add(label);
            destinationBox.getItems().add(label);
        }
        sourceBox.getSelectionModel().select(0);
        destinationBox.getSelectionModel().select(
                Math.min(3, graph.getNodeCount() - 1));
        sourceBox.setMaxWidth(Double.MAX_VALUE);
        sourceBox.setTooltip(new Tooltip("No transmissor"));
        destinationBox.setMaxWidth(Double.MAX_VALUE);
        destinationBox.setTooltip(new Tooltip("No receptor"));

        ttlSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, 99, Math.max(1, graph.diameter())));
        ttlSpinner.setEditable(true);
        ttlSpinner.setMaxWidth(Double.MAX_VALUE);
        ttlSpinner.setTooltip(new Tooltip("TTL inicial (versoes 3 e 4)"));

        batchCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, 1000, 30));
        batchCountSpinner.setEditable(true);
        batchCountSpinner.setMaxWidth(Double.MAX_VALUE);
        batchCountSpinner.setTooltip(new Tooltip("Numero de execucoes do lote"));

        windowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                0, 10000, 1000));
        windowSpinner.setEditable(true);
        windowSpinner.setMaxWidth(Double.MAX_VALUE);
        windowSpinner.setDisable(true);
        windowSpinner.setTooltip(new Tooltip("Janela de deduplicacao V4 (ms)"));

        speedSlider.setMaxWidth(Double.MAX_VALUE);
        speedSlider.setTooltip(new Tooltip("Velocidade da animacao (0.25x a 10x)"));

        seedField.setMaxWidth(Double.MAX_VALUE);
        seedField.setText(String.valueOf(currentSeed));
        seedField.setTooltip(new Tooltip("Semente para geracao de latencias"));

        replayField.setMaxWidth(Double.MAX_VALUE);
        replayField.setEditable(false);
        replayField.setTooltip(new Tooltip("Comando para reproduzir no console"));

        startButton.getStyleClass().add("primario");
        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setTooltip(new Tooltip("Inicia nova simulacao"));

        pauseButton.setDisable(true);
        pauseButton.setMaxWidth(Double.MAX_VALUE);
        pauseButton.setTooltip(new Tooltip("Pausa ou continua a simulacao"));

        resetButton.setMaxWidth(Double.MAX_VALUE);
        resetButton.setTooltip(new Tooltip("Reseta a simulacao"));

        stepButton.setDisable(true);
        stepButton.setMaxWidth(Double.MAX_VALUE);
        stepButton.setTooltip(new Tooltip("Avanca um evento (passo a passo)"));

        batchButton.setMaxWidth(Double.MAX_VALUE);
        batchButton.setTooltip(new Tooltip("Executa lote de simulacoes"));

        randomSeedButton.setMaxWidth(Double.MAX_VALUE);
        randomSeedButton.setTooltip(new Tooltip("Gera nova semente aleatoria"));

        speedLabel.setStyle("-fx-font-weight: bold;");
        updateSpeedLabel();

        snapshotLabel.setWrapText(true);
        snapshotLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #8b98a9; -fx-font-size: 12px;");

        nextEventLabel.setStyle("-fx-text-fill: #8b98a9; -fx-font-size: 12px;");
    }//fim do metodo initControls

    /* ***************************************************************
    * Metodo: buildLayout
    * Funcao: monta o layout em secoes com espacamento consistente.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void buildLayout() {
        // Sessao Cenario
        VBox secaoCenario = new VBox(6.0);
        secaoCenario.getChildren().addAll(
                sectionTitle("Cenario"),
                labeledControl("Versao:", versionBox),
                labeledControl("Origem:", sourceBox),
                labeledControl("Destino:", destinationBox),
                labeledControl("TTL:", ttlSpinner),
                labeledControl("Janela V4:", windowSpinner)
        );

        // Sessao Execucao
        HBox buttons = new HBox(6.0, startButton, pauseButton, resetButton, stepButton);
        HBox seedBox = new HBox(6.0, new Label("Semente:"), seedField, randomSeedButton);
        HBox windowBox = new HBox(6.0, new Label("Janela V4 (ms):"), windowSpinner);
        HBox nextEventBox = new HBox(6.0, new Label("Proximo evento:"), nextEventLabel);
        HBox replayBox = new HBox(6.0, new Label("Reproducao:"), replayField);

        VBox secaoExecucao = new VBox(6.0);
        secaoExecucao.getChildren().addAll(
                sectionTitle("Execucao"),
                speedLabel, speedSlider,
                buttons,
                seedBox,
                windowBox,
                nextEventBox,
                replayBox
        );

        // Sessao Lote
        HBox batchBox = new HBox(6.0, batchButton, batchProgressLabel);
        VBox secaoLote = new VBox(6.0);
        secaoLote.getChildren().addAll(
                sectionTitle("Lote"),
                labeledControl("N:", batchCountSpinner),
                batchBox
        );

        // Sessao Medicao (contadores)
        VBox secaoMedicao = new VBox(6.0);
        secaoMedicao.getChildren().addAll(
                sectionTitle("Medicao"),
                counterLabel, timeLabel, snapshotLabel, statusLabel
        );

        getChildren().addAll(secaoCenario, secaoExecucao, secaoLote, secaoMedicao);
    }//fim do metodo buildLayout

    /* ***************************************************************
    * Metodo: labeledControl
    * Funcao: cria HBox com label e controle alinhados.
    * Parametros: labelText = texto do rotulo; control = controle
    * Retorno: HBox
    *************************************************************** */
    private HBox labeledControl(String labelText, javafx.scene.control.Control control) {
        Label label = new Label(labelText);
        label.setMinWidth(80.0);
        label.setStyle("-fx-text-fill: #e6edf3; -fx-font-weight: bold;");
        HBox box = new HBox(8.0, label, control);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(control, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }//fim do metodo labeledControl

    /* ***************************************************************
    * Metodo: sectionTitle
    * Funcao: cria titulo de secao estilizado.
    * Parametros: text = texto do titulo
    * Retorno: Label
    *************************************************************** */
    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #4c9aff;");
        return label;
    }//fim do metodo sectionTitle

    /* ***************************************************************
    * Metodo: setupListeners
    * Funcao: configura listeners dos controles.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void setupListeners() {
        speedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> updateSpeedLabel());

        versionBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateWindowSpinnerState();
            updateReplayCommand();
        });

        seedField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateSeedField(newVal);
        });
    }//fim do metodo setupListeners

    /* ***************************************************************
    * Metodo: setOverlayLabels
    * Funcao: define as labels de overlay no grafo para atualizacao.
    * Parametros: packets = label de pacotes; time = label de tempo;
    *             snapshot = label de snapshot
    * Retorno: void
    *************************************************************** */
    public void setOverlayLabels(Label packets, Label time, Label snapshot) {
        this.overlayPackets = packets;
        this.overlayTime = time;
        this.overlaySnapshot = snapshot;
    }//fim do metodo setOverlayLabels

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
            seedField.setStyle("-fx-border-color: #f85149; -fx-border-width: 2px;");
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
        });
        destinationBox.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    action.run();
                    updateReplayCommand();
                });
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
        } catch (NumberFormatException exception) {
            return ttlSpinner.getValue().intValue();
        }
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
        } catch (NumberFormatException exception) {
            return batchCountSpinner.getValue().intValue();
        }
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
        } catch (NumberFormatException exception) {
            return windowSpinner.getValue().longValue();
        }
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
        if (overlayPackets != null) {
            overlayPackets.setText("Pacotes: " + packets);
        }
    }//fim do metodo setCounter

    /* ***************************************************************
    * Metodo: setVirtualTime
    * Funcao: mostra o relogio virtual da simulacao.
    * Parametros: millis = tempo virtual em milissegundos
    * Retorno: void
    *************************************************************** */
    public void setVirtualTime(long millis) {
        timeLabel.setText("Tempo virtual: " + millis + " ms");
        if (overlayTime != null) {
            overlayTime.setText("Tempo: " + millis + " ms");
        }
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
        if (overlaySnapshot != null) {
            overlaySnapshot.setText("Snapshot: " + packets + " @ " + millis + "ms");
        }
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
        if (overlaySnapshot != null) {
            overlaySnapshot.setText("Snapshot: --");
        }
    }//fim do metodo resetDisplay

    /* ***************************************************************
    * Metodo: setRunning
    * Funcao: trava os controles de escolha enquanto a simulacao roda.
    * Parametros: running = true se ha simulacao em andamento
    * Retorno: void
    *************************************************************** */
    public void setRunning(boolean running) {
        this.running = running;
        versionBox.setDisable(running);
        sourceBox.setDisable(running);
        destinationBox.setDisable(running);
        ttlSpinner.setDisable(running);
        batchCountSpinner.setDisable(running);
        windowSpinner.setDisable(running || getVersion() != 4);
        startButton.setDisable(running);
        pauseButton.setDisable(!running);
        stepButton.setDisable(!running || !paused);
        batchButton.setDisable(running);
        seedField.setDisable(running);
        randomSeedButton.setDisable(running);
    }//fim do metodo setRunning

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

}//fim da classe ControlPanel