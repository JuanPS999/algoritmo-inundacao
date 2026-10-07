/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: SimulationController
* Funcao...........: Liga o painel de controles, o desenho do grafo e o
*                    Simulator. Um AnimationTimer avanca o tempo
*                    virtual proporcionalmente ao tempo real decorrido
*                    e ao fator de velocidade do slider.
*************************************************************** */
package controller;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import model.LatencyProvider;
import model.Link;
import model.Packet;
import model.Simulator;
import model.SimulationListener;
import view.ControlPanel;
import view.GraphPane;
import view.ResultsTable;

public class SimulationController {

    // evita salto de tempo depois de uma pausa longa da interface
    private static final double MAX_FRAME_MS = 100.0;
    // maximo de linhas de log guardadas
    private static final int MAX_LOG_LINES = 500;

    private final ControlPanel controlPanel;
    private final GraphPane graphPane;
    private final ResultsTable resultsTable;
    private final AnimationTimer timer;
    private Simulator simulator;
    private double virtualClock;
    private long lastFrameNanos;
    private boolean paused;
    private boolean snapshotShown;
    private boolean showTtl;
    // semente para o LatencyProvider (vinda da view)
    private long seed = System.nanoTime();
    // log de eventos
    private final Deque<String> logLines = new ArrayDeque<String>();
    private int droppedLogLines = 0;
    // consumer para entregar o log a view (no maximo 1x por frame)
    private Consumer<List<String>> logConsumer;
    // flag para saber se ja entregou log neste frame
    private boolean logDeliveredThisFrame = false;
    // buffer de linhas novas desde ultima entrega
    private final Deque<String> newLogLines = new ArrayDeque<String>();

    /* ***************************************************************
    * Metodo: SimulationController
    * Funcao: construtor; liga os botoes do painel as acoes.
    * Parametros: controlPanel = painel de controles; graphPane = painel
    *             do grafo; resultsTable = tabela de resultados
    * Retorno: nao se aplica
    *************************************************************** */
    public SimulationController(ControlPanel controlPanel,
            GraphPane graphPane, ResultsTable resultsTable) {
        this.controlPanel = controlPanel;
        this.graphPane = graphPane;
        this.resultsTable = resultsTable;
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                onFrame(now);
            }//fim do metodo handle
        };//fim da classe anonima AnimationTimer
        controlPanel.setOnStart(this::start);
        controlPanel.setOnPause(this::togglePause);
        controlPanel.setOnReset(this::reset);
        controlPanel.setOnEndpointsChange(this::updateEndpoints);
        controlPanel.setOnBatch(this::runBatch);
        controlPanel.setOnStep(this::step);
        updateEndpoints();
    }//fim do construtor SimulationController

    /* ***************************************************************
    * Metodo: setSeed
    * Funcao: define a semente usada pelo LatencyProvider.
    * Parametros: seed = semente (long)
    * Retorno: void
    *************************************************************** */
    public void setSeed(long seed) {
        this.seed = seed;
    }//fim do metodo setSeed

    /* ***************************************************************
    * Metodo: getSeed
    * Funcao: retorna a semente atual.
    * Parametros: nenhum
    * Retorno: long - semente atual
    *************************************************************** */
    public long getSeed() {
        return seed;
    }//fim do metodo getSeed

    /* ***************************************************************
    * Metodo: setLogConsumer
    * Funcao: define o consumer que recebe as linhas de log novas
    *         (chamado no maximo uma vez por quadro).
    * Parametros: consumer = consumer de lista de strings
    * Retorno: void
    *************************************************************** */
    public void setLogConsumer(Consumer<List<String>> consumer) {
        this.logConsumer = consumer;
    }//fim do metodo setLogConsumer

    /* ***************************************************************
    * Metodo: getDroppedLogLines
    * Funcao: retorna o numero de linhas de log descartadas.
    * Parametros: nenhum
    * Retorno: int - linhas descartadas
    *************************************************************** */
    public int getDroppedLogLines() {
        return droppedLogLines;
    }//fim do metodo getDroppedLogLines

    /* ***************************************************************
    * Metodo: onVersionChanged
    * Funcao: chamado quando a versao muda; atualiza flag showTtl.
    * Parametros: version = nova versao (1 a 4)
    * Retorno: void
    *************************************************************** */
    public void onVersionChanged(int version) {
        showTtl = (version >= 3);
        if (simulator != null && !paused) {
            // reinicia simulacao com nova versao se estiver rodando
            start();
        }
    }//fim do metodo onVersionChanged

    /* ***************************************************************
    * Metodo: onSourceChanged
    * Funcao: chamado quando o transmissor muda; atualiza endpoints.
    * Parametros: source = novo id do transmissor
    * Retorno: void
    *************************************************************** */
    public void onSourceChanged(int source) {
        updateEndpoints();
        if (simulator != null && !paused) {
            start();
        }
    }//fim do metodo onSourceChanged

    /* ***************************************************************
    * Metodo: onTargetChanged
    * Funcao: chamado quando o receptor muda; atualiza endpoints.
    * Parametros: target = novo id do receptor
    * Retorno: void
    *************************************************************** */
    public void onTargetChanged(int target) {
        updateEndpoints();
        if (simulator != null && !paused) {
            start();
        }
    }//fim do metodo onTargetChanged

    /* ***************************************************************
    * Metodo: onTtlChanged
    * Funcao: chamado quando o TTL muda.
    * Parametros: ttl = novo valor de TTL
    * Retorno: void
    *************************************************************** */
    public void onTtlChanged(int ttl) {
        if (simulator != null && !paused) {
            start();
        }
    }//fim do metodo onTtlChanged

    /* ***************************************************************
    * Metodo: onWindowChanged
    * Funcao: chamado quando a janela V4 muda.
    * Parametros: windowMs = novo valor da janela em ms
    * Retorno: void
    *************************************************************** */
    public void onWindowChanged(int windowMs) {
        if (simulator != null && !paused) {
            start();
        }
    }//fim do metodo onWindowChanged

    /* ***************************************************************
    * Metodo: onSpeedChanged
    * Funcao: chamado quando a velocidade muda.
    * Parametros: speed = novo fator de velocidade
    * Retorno: void
    *************************************************************** */
    public void onSpeedChanged(double speed) {
        // velocidade e aplicada no onFrame; nada a fazer aqui
    }//fim do metodo onSpeedChanged

    /* ***************************************************************
    * Metodo: onSeedChanged
    * Funcao: chamado quando a semente muda.
    * Parametros: seed = nova semente
    * Retorno: void
    *************************************************************** */
    public void onSeedChanged(long seed) {
        this.seed = seed;
        if (simulator != null && !paused) {
            start();
        }
    }//fim do metodo onSeedChanged

    /* ***************************************************************
    * Metodo: onLogScaleChanged
    * Funcao: chamado quando a escala log muda.
    * Parametros: logScale = true se escala log ativada
    * Retorno: void
    *************************************************************** */
    public void onLogScaleChanged(boolean logScale) {
        resultsTable.setLogScale(logScale);
    }//fim do metodo onLogScaleChanged

    /* ***************************************************************
    * Metodo: updateEndpoints
    * Funcao: pinta transmissor e receptor conforme a escolha atual.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void updateEndpoints() {
        graphPane.setEndpoints(controlPanel.getSourceId(),
                controlPanel.getDestinationId());
    }//fim do metodo updateEndpoints

    /* ***************************************************************
    * Metodo: start
    * Funcao: cria uma nova simulacao com as escolhas do painel e inicia
    *         a animacao. No modo ao vivo nao ha limite de pacotes.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void start() {
        int sourceId = controlPanel.getSourceId();
        int destinationId = controlPanel.getDestinationId();
        if (sourceId == destinationId) {
            controlPanel.setStatus("Escolha transmissor e receptor diferentes.");
            return;
        }//fim do if
        timer.stop();
        int version = controlPanel.getVersion();
        showTtl = (version >= 3);
        long windowMs = controlPanel.getWindowMs();
        simulator = new Simulator(graphPane.getGraph(),
                StrategyFactory.create(version, windowMs),
                new LatencyProvider(seed), sourceId, destinationId,
                controlPanel.getTtl());
        simulator.setListener(new SimulationListener() {
            @Override
            public void onSend(long time, int fromNode, Link link,
                    Packet packet, long counter) {
                addLogLine(formatSend(time, fromNode, link, packet, counter));
            }

            @Override
            public void onArrive(long time, int nodeId, Link link,
                    Packet packet, long counter) {
                addLogLine(formatArrive(time, nodeId, link, packet, counter));
            }

            @Override
            public void onDrop(long time, int nodeId, Link link,
                    Packet packet, long counter, String reason) {
                addLogLine(formatDrop(time, nodeId, link, packet, counter, reason));
            }

            @Override
            public void onFirstArrival(long time, int destinationId,
                    long snapshotCounter, Packet packet) {
                addLogLine(formatFirstArrival(time, destinationId, snapshotCounter, packet));
            }
        });//fim da classe anonima SimulationListener

        virtualClock = 0.0;
        lastFrameNanos = 0L;
        paused = false;
        snapshotShown = false;
        logLines.clear();
        newLogLines.clear();
        droppedLogLines = 0;
        graphPane.setEndpoints(sourceId, destinationId);
        graphPane.setDestinationReached(false);
        graphPane.clearPackets();
        controlPanel.resetDisplay();
        controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.RODANDO);
        controlPanel.setStatus("Simulacao em andamento.");
        simulator.start();
        refreshView();
        timer.start();
    }//fim do metodo start

    /* ***************************************************************
    * Metodo: step
    * Funcao: avanca a simulacao ate o proximo evento (modo passo a passo).
    *         Pausa o timer, cria o simulator se nao existe, e chama
    *         advanceTo(peekNextTime()).
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void step() {
        if (simulator == null) {
            // cria simulator com parametros atuais mas nao inicia timer
            int sourceId = controlPanel.getSourceId();
            int destinationId = controlPanel.getDestinationId();
            if (sourceId == destinationId) {
                controlPanel.setStatus("Escolha transmissor e receptor diferentes.");
                return;
            }
            int version = controlPanel.getVersion();
            showTtl = (version >= 3);
            long windowMs = controlPanel.getWindowMs();
            simulator = new Simulator(graphPane.getGraph(),
                    StrategyFactory.create(version, windowMs),
                    new LatencyProvider(seed), sourceId, destinationId,
                    controlPanel.getTtl());
            simulator.setListener(new SimulationListener() {
                @Override
                public void onSend(long time, int fromNode, Link link,
                        Packet packet, long counter) {
                    addLogLine(formatSend(time, fromNode, link, packet, counter));
                }

                @Override
                public void onArrive(long time, int nodeId, Link link,
                        Packet packet, long counter) {
                    addLogLine(formatArrive(time, nodeId, link, packet, counter));
                }

                @Override
                public void onDrop(long time, int nodeId, Link link,
                        Packet packet, long counter, String reason) {
                    addLogLine(formatDrop(time, nodeId, link, packet, counter, reason));
                }

                @Override
                public void onFirstArrival(long time, int destinationId,
                        long snapshotCounter, Packet packet) {
                    addLogLine(formatFirstArrival(time, destinationId, snapshotCounter, packet));
                }
            });//fim da classe anonima SimulationListener
            simulator.start();
            virtualClock = 0.0;
            lastFrameNanos = 0L;
            paused = true; // comeca pausado no modo passo a passo
            snapshotShown = false;
            logLines.clear();
            newLogLines.clear();
            droppedLogLines = 0;
            graphPane.setEndpoints(sourceId, destinationId);
            graphPane.setDestinationReached(false);
            graphPane.clearPackets();
            controlPanel.resetDisplay();
            controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.PAUSADO);
            controlPanel.setStatus("Modo passo a passo. Clique 'Proximo evento' para avancar.");
        }
        // pausa o timer se estiver rodando
        timer.stop();
        paused = true;
        controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.PAUSADO);
        // avanca ate o proximo evento
        long nextTime = simulator.peekNextTime();
        if (nextTime != Long.MAX_VALUE) {
            simulator.advanceTo(nextTime);
            virtualClock = nextTime;
            refreshView();
            controlPanel.setNextEventTime(simulator.peekNextTime());
            controlPanel.setStatus("Avancado para t = " + virtualClock + " ms");
        } else {
            controlPanel.setNextEventTime(Long.MAX_VALUE);
            controlPanel.setStatus("Fila de eventos vazia.");
        }
    }//fim do metodo step

    /* ***************************************************************
    * Metodo: togglePause
    * Funcao: pausa ou continua a simulacao sem perder o estado.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void togglePause() {
        if (simulator == null) {
            return;
        }//fim do if
        paused = !paused;
        lastFrameNanos = 0L;
        if (paused) {
            timer.stop();
        } else {
            timer.start();
        }
        controlPanel.setEstado(paused ? view.ControlPanel.EstadoSimulacao.PAUSADO
                : view.ControlPanel.EstadoSimulacao.RODANDO);
        controlPanel.setStatus(paused ? "Pausado." : "Simulacao em andamento.");
    }//fim do metodo togglePause

    /* ***************************************************************
    * Metodo: reset
    * Funcao: interrompe a simulacao e limpa a tela.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void reset() {
        timer.stop();
        simulator = null;
        paused = false;
        logLines.clear();
        newLogLines.clear();
        droppedLogLines = 0;
        graphPane.clearPackets();
        graphPane.setDestinationReached(false);
        controlPanel.resetDisplay();
        controlPanel.setStatus("Pronto.");
        controlPanel.setNextEventTime(Long.MAX_VALUE);
    }//fim do metodo reset

    /* ***************************************************************
    * Metodo: onFrame
    * Funcao: a cada quadro, avanca o tempo virtual pelo tempo real
    *         decorrido vezes o fator de velocidade e atualiza a tela.
    * Parametros: now = instante do quadro em nanossegundos
    * Retorno: void
    *************************************************************** */
    private void onFrame(long now) {
        if (simulator == null) {
            return;
        }//fim do if
        if (paused || lastFrameNanos == 0L) {
            lastFrameNanos = now;
            return;
        }//fim do if
        double elapsedMillis = (now - lastFrameNanos) / 1000000.0;
        lastFrameNanos = now;
        if (elapsedMillis > MAX_FRAME_MS) {
            elapsedMillis = MAX_FRAME_MS;
        }//fim do if
        virtualClock += elapsedMillis * controlPanel.getSpeed();
        simulator.advanceTo((long) virtualClock);
        refreshView();
        // entrega log a view no maximo 1x por frame
        deliverLogIfNeeded();
    }//fim do metodo onFrame

    /* ***************************************************************
    * Metodo: deliverLogIfNeeded
    * Funcao: entrega as linhas de log acumuladas ao consumer da view.
    *         So entrega se ha consumer e ha linhas novas.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void deliverLogIfNeeded() {
        if (logConsumer != null && !newLogLines.isEmpty()) {
            logConsumer.accept(new ArrayList<String>(newLogLines));
            newLogLines.clear();
        }
    }//fim do metodo deliverLogIfNeeded

    /* ***************************************************************
    * Metodo: addLogLine
    * Funcao: adiciona uma linha ao log, mantendo no maximo MAX_LOG_LINES.
    *         Conta linhas descartadas.
    * Parametros: line = linha formatada
    * Retorno: void
    *************************************************************** */
    private void addLogLine(String line) {
        logLines.addLast(line);
        newLogLines.addLast(line);
        if (logLines.size() > MAX_LOG_LINES) {
            logLines.removeFirst();
            droppedLogLines++;
        }
    }//fim do metodo addLogLine

    /* ***************************************************************
    * Metodo: formatSend
    * Funcao: formata linha de envio: "t=123 | A->B | pkt 5 | ttl 2 | enviado"
    * Parametros: time, fromNode, link, packet, counter
    * Retorno: String formatada
    *************************************************************** */
    private String formatSend(long time, int fromNode, Link link, Packet packet, long counter) {
        int toNode = link.other(fromNode);
        return String.format("t=%d | %s->%s | pkt %d | %s | enviado",
                time, nodeLabel(fromNode), nodeLabel(toNode), packet.getId(),
                showTtl ? "ttl " + packet.getTtl() : "");
    }//fim do metodo formatSend

    /* ***************************************************************
    * Metodo: formatArrive
    * Funcao: formata linha de chegada: "t=123 | A->B | pkt 5 | ttl 2 | chegou"
    * Parametros: time, nodeId, link, packet, counter
    * Retorno: String formatada
    *************************************************************** */
    private String formatArrive(long time, int nodeId, Link link, Packet packet, long counter) {
        int fromNode = link.other(nodeId);
        return String.format("t=%d | %s->%s | pkt %d | %s | chegou",
                time, nodeLabel(fromNode), nodeLabel(nodeId), packet.getId(),
                showTtl ? "ttl " + packet.getTtl() : "");
    }//fim do metodo formatArrive

    /* ***************************************************************
    * Metodo: formatDrop
    * Funcao: formata linha de descarte: "t=123 | A->B | pkt 5 | ttl 2 | descartado (TTL)"
    * Parametros: time, nodeId, link, packet, counter, reason
    * Retorno: String formatada
    *************************************************************** */
    private String formatDrop(long time, int nodeId, Link link, Packet packet, long counter, String reason) {
        int fromNode = (link != null) ? link.other(nodeId) : 0;
        String linkInfo = (link != null) ? nodeLabel(fromNode) + "->" + nodeLabel(nodeId) : "origem";
        return String.format("t=%d | %s | pkt %d | %s | descartado (%s)",
                time, linkInfo, packet.getId(),
                showTtl ? "ttl " + packet.getTtl() : "", reason);
    }//fim do metodo formatDrop

    /* ***************************************************************
    * Metodo: formatFirstArrival
    * Funcao: formata linha de primeira chegada: "t=123 | A->B | pkt 5 | ttl 2 | primeira chegada (snapshot 8)"
    * Parametros: time, destinationId, snapshotCounter, packet
    * Retorno: String formatada
    *************************************************************** */
    private String formatFirstArrival(long time, int destinationId, long snapshotCounter, Packet packet) {
        return String.format("t=%d | primeira chegada no %s | snapshot=%d",
                time, nodeLabel(destinationId), snapshotCounter);
    }//fim do metodo formatFirstArrival

    /* ***************************************************************
    * Metodo: nodeLabel
    * Funcao: converte id numerico para rotulo de letra (1->A, 2->B, ...).
    * Parametros: id = id do no
    * Retorno: String - rotulo
    *************************************************************** */
    private String nodeLabel(int id) {
        if (id >= 1 && id <= 26) {
            return String.valueOf((char) ('A' + id - 1));
        }
        return String.valueOf(id);
    }//fim do metodo nodeLabel

    /* ***************************************************************
    * Metodo: refreshView
    * Funcao: atualiza contador, pacotes em transito e snapshot; encerra
    *         a animacao quando nao ha mais pacotes na rede.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void refreshView() {
        long clock = (long) virtualClock;
        List<Simulator.InFlight> flights = simulator.inFlight();
        controlPanel.setCounter(simulator.getPacketsGenerated());
        controlPanel.setVirtualTime(clock);
        graphPane.showPackets(flights, clock, showTtl);
        controlPanel.setNextEventTime(simulator.peekNextTime());
        if (simulator.hasSnapshot() && !snapshotShown) {
            snapshotShown = true;
            controlPanel.showSnapshot(simulator.getSnapshotCounter(),
                    simulator.getSnapshotTime());
            resultsTable.addResult(controlPanel.getVersion(), simulator.getSnapshotCounter());
            graphPane.setDestinationReached(true);
        }//fim do if
        if (flights.isEmpty()) {
            timer.stop();
            controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.ENCERRADO);
            if (snapshotShown) {
                controlPanel.setStatus("Encerrada: nao ha mais pacotes na rede.");
            } else {
                controlPanel.setStatus("Encerrada sem atingir o destino (rede esvaziou).");
                resultsTable.addFailure(controlPanel.getVersion(), "INALCANCAVEL");
            }
        }//fim do if
    }//fim do metodo refreshView

    /* ***************************************************************
    * Metodo: runBatch
    * Funcao: executa o lote de simulacoes em thread de fundo.
    *         O lote NAO instala listener.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void runBatch() {
        final int version = controlPanel.getVersion();
        final int sourceId = controlPanel.getSourceId();
        final int destinationId = controlPanel.getDestinationId();
        final int ttl = controlPanel.getTtl();
        final int count = controlPanel.getBatchCount();
        final long baseSeed = controlPanel.getSeed();
        final long windowMs = controlPanel.getWindowMs();

        if (sourceId == destinationId) {
            controlPanel.setStatus("Escolha transmissor e receptor diferentes.");
            return;
        }

        controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.LOTE);
        controlPanel.setStatus("Executando lote...");
        controlPanel.setBatchProgress(0, count);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                for (int i = 0; i < count; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    final long seed = baseSeed + i;
                    final int current = i + 1;

                    controller.BatchResult result = controller.BatchRunner.run(
                            graphPane.getGraph(), version, sourceId, destinationId, ttl, 1, seed, windowMs);

                    if (result.getSamples().size() > 0) {
                        final long snapshot = result.getSamples().get(0);
                        Platform.runLater(() -> {
                            resultsTable.addResult(version, snapshot);
                            controlPanel.setBatchProgress(current, count);
                        });
                    } else if (result.getAborted() > 0) {
                        Platform.runLater(() -> {
                            resultsTable.addFailure(version, "ABORTADA");
                            controlPanel.setBatchProgress(current, count);
                        });
                    } else if (result.getUnreachable() > 0) {
                        Platform.runLater(() -> {
                            resultsTable.addFailure(version, "INALCANCAVEL");
                            controlPanel.setBatchProgress(current, count);
                        });
                    } else {
                        Platform.runLater(() -> {
                            controlPanel.setBatchProgress(current, count);
                        });
                    }
                }
                return null;
            }

            @Override
            protected void succeeded() {
                controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.OCIOSO);
                controlPanel.setStatus("Lote concluido.");
            }

            @Override
            protected void failed() {
                controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.OCIOSO);
                controlPanel.setStatus("Erro no lote: " + getException().getMessage());
            }

            @Override
            protected void cancelled() {
                controlPanel.setEstado(view.ControlPanel.EstadoSimulacao.OCIOSO);
                controlPanel.setStatus("Lote cancelado.");
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }//fim do metodo runBatch
}//fim da classe SimulationController