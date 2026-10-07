/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: ConsoleRunner.java
* Funcao...........: Utilitario de console para simulacao de flooding. Executa uma simulacao e imprime eventos e snapshot da primeira chegada ao destino.
*************************************************************** */

package util;

import model.Graph;
import model.Link;
import model.Packet;
import model.RunResult;
import model.ForwardingStrategy;
import model.LatencyProvider;
import model.Strategy1AllInterfaces;
import model.Strategy2ExceptIncoming;
import model.Strategy3Ttl;
import model.Strategy4Custom;
import model.Simulator;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitario de console para simulacao de flooding.
 * Uso: java -cp build util.ConsoleRunner <versao> <origem> <destino> <ttl> <semente> [backbone] [janela=<ms>]
 * Imprime um evento por linha e o snapshot da primeira chegada ao destino.
 * O argumento janela=<ms> e opcional, pode aparecer em qualquer posicao apos o 5o argumento.
 * So e usado na versao 4; padrao = 1000 ms.
 */
public final class ConsoleRunner {

    private ConsoleRunner() {
    }

    private static ForwardingStrategy strategyFor(int version, long janelaMs) {
        switch (version) {
            case 1: return new Strategy1AllInterfaces();
            case 2: return new Strategy2ExceptIncoming();
            case 3: return new Strategy3Ttl();
            case 4: return new Strategy4Custom(janelaMs);
            default:
                throw new IllegalArgumentException("Versao invalida (1..4): " + version);
        }
    }

    private static String route(Link link, int fromNode) {
        return link.getA() + "-" + link.getB() + " (" + fromNode + "->" + link.other(fromNode) + ")";
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Uso: ConsoleRunner <versao> <origem> <destino> <ttl> <semente> [backbone] [janela=<ms>]");
            System.exit(2);
        }
        final int version = Integer.parseInt(args[0]);
        final int source = Integer.parseInt(args[1]);
        final int destination = Integer.parseInt(args[2]);
        final int ttl = Integer.parseInt(args[3]);
        final long seed = Long.parseLong(args[4]);

        // Parse optional arguments: backbone file and janela=<ms>
        Path file = Paths.get("backbone.txt");
        long janelaMs = 1000; // default for V4

        for (int i = 5; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("janela=")) {
                try {
                    janelaMs = Long.parseLong(arg.substring(7));
                } catch (NumberFormatException e) {
                    System.err.println("Valor invalido para janela: " + arg);
                    System.exit(2);
                }
            } else {
                file = Paths.get(arg);
            }
        }

        Graph graph = BackboneLoader.load(file);
        final boolean showTtl = version >= 3;

        ForwardingStrategy strategy = strategyFor(version, janelaMs);
        Simulator sim = new Simulator(graph, strategy,
                new LatencyProvider(seed), source, destination, ttl);

        sim.setListener(new Simulator.Listener() {
            @Override
            public void onTransmit(long time, int fromNode, Link link, Packet packet, long counter) {
                System.out.printf("t=%6d  ENVIO    link %-12s %s contador=%d%n",
                        time, route(link, fromNode), ttlInfo(packet), counter);
            }

            @Override
            public void onArrive(long time, int nodeId, Link link, Packet packet, long counter) {
                System.out.printf("t=%6d  CHEGADA  link %-12s no=%d %s contador=%d%n",
                        time, route(link, link.other(nodeId)), nodeId, ttlInfo(packet), counter);
            }

            private String ttlInfo(Packet p) {
                return showTtl ? "ttl=" + p.getTtl() + " " : "";
            }
        });

        RunResult result = sim.runUntilFirstArrival();

        System.out.println();
        if (result.isReached()) {
            System.out.println("SNAPSHOT: primeira chegada ao destino " + destination
                    + " em t=" + result.getSnapshotTime()
                    + " ms com contador=" + result.getSnapshotCounter() + " pacotes gerados.");
        } else if (result.isAbortedByLimit()) {
            System.out.println("ABORTADO: limite de " + Simulator.ABORT_LIMIT
                    + " transmissoes excedido sem chegada ao destino.");
        } else {
            System.out.println("DESTINO INALCANCAVEL: fila de eventos esvaziou sem chegada.");
        }
        System.out.println("Total de pacotes gerados: " + result.getPacketsGenerated()
                + "  |  tempo virtual final: " + result.getVirtualTime() + " ms");

        // V4: imprimir duplicatas descartadas
        if (version == 4) {
            ForwardingStrategy strat = sim.getStrategy();
            if (strat instanceof model.Strategy4Custom) {
                System.out.println("Duplicatas descartadas: " + ((model.Strategy4Custom) strat).getDuplicatasDescartadas());
            }
        }
    }
}