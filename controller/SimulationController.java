/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 03/10/2026
* Ultima alteracao.: 03/10/2026
* Nome.............: SimulationController
* Funcao...........: Controla a simulacao, AnimationTimer e conexao com GUI.
*************************************************************** */
package controller;

import model.ForwardingStrategy;
import model.Link;
import model.LatencyProvider;
import model.Packet;
import model.Simulator;
import model.Strategy1AllInterfaces;
import model.Strategy2ExceptIncoming;
import model.Strategy3Ttl;
import model.Strategy4Custom;
import view.ControlPanel;
import view.GraphPane;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Controlador que liga os controles (ControlPanel) ao Simulator.
 * Usa AnimationTimer para avan&ccedil;ar o tempo virtual e desenhar pacotes em voo.
 */
public class SimulationController {
    private final ControlPanel controlPanel;
    private final GraphPane graphPane;
    private Simulator simulator;

    private boolean running = false;

    public SimulationController(ControlPanel controlPanel, GraphPane graphPane, Simulator simulator) {
        this.controlPanel = controlPanel;
        this.graphPane = graphPane;
        this.simulator = simulator;
        simulator.setListener(new Simulator.Listener() {
            @Override
            public void onTransmit(long time, int fromNode, Link link, Packet packet, long counter) {
            }

            @Override
            public void onArrive(long time, int nodeId, Link link, Packet packet, long counter) {
                Platform.runLater(() -> {
                    if (simulator.hasSnapshot()) {
                        System.out.println("Snapshot: contador=" + simulator.getSnapshotCounter() + " t=" + simulator.getSnapshotTime());
                    }
                });
            }
        });
        startAnimationTimer();
    }

    private void startAnimationTimer() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!running) return;
                simulator.advanceTo(simulator.getVirtualTime() + 10);
                drawInFlightPackets();
                System.out.println("Tempo virtual: " + simulator.getVirtualTime() + " ms");
            }
        };
        timer.start();
    }

    private void drawInFlightPackets() {
        for (Simulator.InFlight inflight : simulator.inFlight()) {
            Packet p = inflight.packet;
            Link link = inflight.link;
            double prog = inflight.progress(simulator.getVirtualTime());
            int fromNode = link.getA();
            int toNode = link.getB();
            double x1 = graphPane.getNodeX(fromNode - 1);
            double y1 = graphPane.getNodeY(fromNode - 1);
            double x2 = graphPane.getNodeX(toNode - 1);
            double y2 = graphPane.getNodeY(toNode - 1);
            double ix = x1 + (x2 - x1) * prog;
            double iy = y1 + (y2 - y1) * prog;
            int versao = controlPanel.getCurrentVersion();
            if (p.getTtl() > 0 && (versao == 3 || versao == 4)) {
                Text ttText = new Text("TTL=" + p.getTtl());
                ttText.setFont(javafx.scene.text.Font.font(10));
                ttText.setFill(Color.BLACK);
                ttText.setX(ix - ttText.getLayoutBounds().getWidth() / 2);
                ttText.setY(iy + 15);
                System.out.printf("Pacote TTL=%d em (%.1f, %.1f)%n", p.getTtl(), ix, iy);
            }
        }
    }

    public void start() {
        running = true;
        int version = controlPanel.getCurrentVersion();
        int source = controlPanel.getSourceId();
        int dest = controlPanel.getDestId();
        int ttl = controlPanel.getTtlValue();
        model.ForwardingStrategy strategy;
        switch (version) {
            case 2: strategy = new model.Strategy2ExceptIncoming(); break;
            case 3: strategy = new model.Strategy3Ttl(); break;
            case 4: strategy = new model.Strategy4Custom(); break;
            default: strategy = new model.Strategy1AllInterfaces();
        }
        simulator = new Simulator(graphPane.getGraph(), strategy, new model.LatencyProvider(42), source, dest, ttl);
        simulator.start();
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void reset() {
        running = false;
        System.out.println("Simulacao resetada");
    }
}
