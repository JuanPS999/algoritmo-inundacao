/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: ControlPanel
* Funcao...........: Painel de controle da simulacao com controles GUI.
*************************************************************** */
package view;

import model.Graph;
import model.Simulator;
import model.ForwardingStrategy;
import model.LatencyProvider;
import model.Strategy1AllInterfaces;
import model.Strategy2ExceptIncoming;
import model.Strategy3Ttl;
import model.Strategy4Custom;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;

/**
 * Painel de controle da simulacao: combos de versao, transmissor, receptor,
 * spinner de TTL e botoes Iniciar/Resetar.
 */
public class ControlPanel extends VBox {

    private final Graph graph;
    private Simulator simulator;

    private final ComboBox<Integer> versionBox;
    private final ComboBox<Integer> sourceBox;
    private final ComboBox<Integer> destBox;
    private final Spinner<Integer> ttlSpinner;
    private final Button btnStart;
    private final Button btnReset;

    private final IntegerProperty currentVersion = new SimpleIntegerProperty(1);
    private final IntegerProperty sourceId = new SimpleIntegerProperty(1);
    private final IntegerProperty destId = new SimpleIntegerProperty(2);
    private final IntegerProperty ttlValue = new SimpleIntegerProperty(0);

    /* ***************************************************************
* Metodo: ControlPanel
* Funcao: Construtor que inicializa o painel com o grafo e controles.
* Parametros: graph - grafo da rede; simulator - nucleo da simulacao.
* Retorno: void
*************************************************************** */
public ControlPanel(Graph graph, Simulator simulator) {
        this.graph = graph;
        this.simulator = simulator;

        setPadding(new Insets(10));
        setSpacing(8);
        setStyle("-fx-background-color: #f0f0f0;");

        // Diâmetro para o valor padrao do TTL
        int diameter = graph.diameter();

        // --- ComboBox Versao ---
        versionBox = new ComboBox<>();
        versionBox.getItems().addAll(1, 2, 3, 4);
        versionBox.setValue(1);
        versionBox.valueProperty().addListener((obs, oldV, newV) -> {
            currentVersion.set(newV.intValue());
        });

        // --- ComboBox Transmissor ---
        sourceBox = new ComboBox<>();
        for (int i = 1; i <= graph.getNodeCount(); i++) {
            sourceBox.getItems().add(i);
        }
        sourceBox.setValue(1);
        sourceBox.valueProperty().addListener((obs, oldS, newS) -> {
            sourceId.set(newS.intValue());
        });

        // --- ComboBox Receptor ---
        destBox = new ComboBox<>();
        for (int i = 1; i <= graph.getNodeCount(); i++) {
            destBox.getItems().add(i);
        }
        destBox.setValue(2);
        destBox.valueProperty().addListener((obs, oldD, newD) -> {
            destId.set(newD.intValue());
        });

        // --- Spinner TTL ---
        ttlSpinner = new Spinner<>(1, diameter > 0 ? diameter : 10, diameter > 0 ? diameter : 10);
        ttlSpinner.valueProperty().addListener((obs, oldT, newT) -> {
            ttlValue.set(newT.intValue());
        });

        // --- Botoes ---
        btnStart = new Button("Iniciar");
        btnReset = new Button("Resetar");

        // Layout
        getChildren().addAll(
                new Label("Versao:"), versionBox,
                new Label("Transmissor:"), sourceBox,
                new Label("Receptor:"), destBox,
                new Label("TTL (inicial):"), ttlSpinner,
                btnStart,
                btnReset
        );

        // Ajustar largura do spinner
        ttlSpinner.setPrefWidth(60);

        // Inicializar TTL com o diametro
        ttlSpinner.getValueFactory().setValue(diameter > 0 ? diameter : 5);

        // Eventos dos botoes
        btnStart.setOnAction(e -> startSimulation());
        btnReset.setOnAction(e -> resetSimulation());
    }

    /* ***************************************************************
* Metodo: getCurrentVersion
* Funcao: Retorna a versao atual selecionada (1..4).
* Parametros: nenhum
* Retorno: int - numero da versao.
*************************************************************** */
public int getCurrentVersion() {
        return currentVersion.get();
    }

    /* ***************************************************************
* Metodo: getSourceId
* Funcao: Retorna o id do transmissor selecionado.
* Parametros: nenhum
* Retorno: int - id do no transmissor.
*************************************************************** */
public int getSourceId() {
        return sourceId.get();
    }

    /* ***************************************************************
* Metodo: getDestId
* Funcao: Retorna o id do receptor selecionado.
* Parametros: nenhum
* Retorno: int - id do no receptor.
*************************************************************** */
public int getDestId() {
        return destId.get();
    }

    /* ***************************************************************
* Metodo: getTtlValue
* Funcao: Retorna o valor do TTL selecionado.
* Parametros: nenhum
* Retorno: int - valor do TTL.
*************************************************************** */
public int getTtlValue() {
        return ttlValue.get();
    }

    /* ***************************************************************
* Metodo: startSimulation
* Funcao: Inicia a simulacao com os parametros atuais.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
private void startSimulation() {
        int version = getCurrentVersion();
        int source = getSourceId();
        int dest = getDestId();
        int ttl = getTtlValue();

        // Validacao basica
        if (source == dest) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setContentText("Transmissor e receptor diferentes, por favor.");
            alert.showAndWait();
            return;
        }

        // Criar estrategia
        ForwardingStrategy strategy;
        switch (version) {
            case 2: strategy = new Strategy2ExceptIncoming(); break;
            case 3: strategy = new Strategy3Ttl(); break;
            case 4: strategy = new Strategy4Custom(); break;
            default: strategy = new Strategy1AllInterfaces();
        }

        // Criar simulador (nao final para permitir reatribuicao)
        simulator = new Simulator(graph, strategy, new LatencyProvider(42), source, dest, ttl);

        // AnimationTimer sera conectado pelo SimulationController
        System.out.println("Simulacao iniciada: versao=" + version + " source=" + source + " dest=" + dest + " ttl=" + ttl);
    }

    /* ***************************************************************
* Metodo: resetSimulation
* Funcao: Reseta o estado da simulacao.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
private void resetSimulation() {
        simulator = new Simulator(graph,
                new Strategy1AllInterfaces(),
                new LatencyProvider(42),
                getSourceId(), getDestId(), (Integer) ttlSpinner.getValue());
        System.out.println("Simulacao resetada");
    }
}