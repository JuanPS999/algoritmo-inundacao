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

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import model.ForwardingStrategy;
import model.LatencyProvider;
import model.Link;
import model.Packet;
import model.Simulator;
import view.ControlPanel;
import view.GraphPane;

public class SimulationController {

  // evita salto de tempo depois de uma pausa longa da interface
  private static final double MAX_FRAME_MS = 100.0;

  private final ControlPanel controlPanel;
  private final GraphPane graphPane;
  private final Map<Packet, Integer> origins =
      new IdentityHashMap<Packet, Integer>();
  private final AnimationTimer timer;
  private Simulator simulator;
  private double virtualClock;
  private long lastFrameNanos;
  private boolean paused;
  private boolean snapshotShown;
  private boolean showTtl;

  /* ***************************************************************
  * Metodo: SimulationController
  * Funcao: construtor; liga os botoes do painel as acoes.
  * Parametros: controlPanel = painel de controles; graphPane = painel
  *             do grafo
  * Retorno: nao se aplica
  *************************************************************** */
  public SimulationController(ControlPanel controlPanel,
      GraphPane graphPane) {
    this.controlPanel = controlPanel;
    this.graphPane = graphPane;
    this.timer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        onFrame(now);
      }//fim do metodo handle
    };//fim da classe anonima AnimationTimer
    controlPanel.setOnStart(() -> start());
    controlPanel.setOnPause(() -> togglePause());
    controlPanel.setOnReset(() -> reset());
    controlPanel.setOnEndpointsChange(() -> updateEndpoints());
    updateEndpoints();
  }//fim do construtor SimulationController

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
  private void start() {
    int sourceId = controlPanel.getSourceId();
    int destinationId = controlPanel.getDestinationId();
    if (sourceId == destinationId) {
      controlPanel.setStatus("Escolha transmissor e receptor diferentes.");
      return;
    }//fim do if
    timer.stop();
    int version = controlPanel.getVersion();
    showTtl = (version >= 3);
    origins.clear();
    simulator = new Simulator(graphPane.getGraph(), StrategyFactory.create(version),
        new LatencyProvider(System.nanoTime()), sourceId, destinationId,
        controlPanel.getTtl());
    simulator.setListener(new Simulator.Listener() {
      @Override
      public void onTransmit(long time, int fromNode, Link link,
          Packet packet, long counter) {
        origins.put(packet, Integer.valueOf(fromNode));
      }//fim do metodo onTransmit

      @Override
      public void onArrive(long time, int nodeId, Link link,
          Packet packet, long counter) {
        origins.remove(packet);
      }//fim do metodo onArrive
    });//fim da classe anonima Listener
    virtualClock = 0.0;
    lastFrameNanos = 0L;
    paused = false;
    snapshotShown = false;
    graphPane.setEndpoints(sourceId, destinationId);
    graphPane.setDestinationReached(false);
    graphPane.clearPackets();
    controlPanel.resetDisplay();
    controlPanel.setRunning(true);
    controlPanel.setPaused(false);
    controlPanel.setStatus("Simulacao em andamento.");
    simulator.start();
    refreshView();
    timer.start();
  }//fim do metodo start

  /* ***************************************************************
  * Metodo: togglePause
  * Funcao: pausa ou continua a simulacao sem perder o estado.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void togglePause() {
    if (simulator == null) {
      return;
    }//fim do if
    paused = !paused;
    lastFrameNanos = 0L;
    controlPanel.setPaused(paused);
    controlPanel.setStatus(paused ? "Pausado." : "Simulacao em andamento.");
  }//fim do metodo togglePause

  /* ***************************************************************
  * Metodo: reset
  * Funcao: interrompe a simulacao e limpa a tela.
  * Parametros: nenhum
  * Retorno: void
  *************************************************************** */
  private void reset() {
    timer.stop();
    simulator = null;
    origins.clear();
    paused = false;
    graphPane.clearPackets();
    graphPane.setDestinationReached(false);
    controlPanel.resetDisplay();
    controlPanel.setRunning(false);
    controlPanel.setPaused(false);
    controlPanel.setStatus("Pronto.");
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
  }//fim do metodo onFrame

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
    graphPane.showPackets(flights, clock, origins, showTtl);
    if (simulator.hasSnapshot() && !snapshotShown) {
      snapshotShown = true;
      controlPanel.showSnapshot(simulator.getSnapshotCounter(),
          simulator.getSnapshotTime());
      graphPane.setDestinationReached(true);
    }//fim do if
    if (flights.isEmpty()) {
      timer.stop();
      controlPanel.setRunning(false);
      controlPanel.setStatus(snapshotShown
          ? "Encerrada: nao ha mais pacotes na rede."
          : "Encerrada sem atingir o destino (rede esvaziou).");
    }//fim do if
  }//fim do metodo refreshView

}//fim da classe SimulationController
