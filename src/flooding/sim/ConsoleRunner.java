package flooding.sim;

import flooding.io.BackboneLoader;
import flooding.model.Graph;
import flooding.model.Link;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitario de console para depuracao e testes futuros.
 * Uso: java -cp build flooding.sim.ConsoleRunner <versao> <origem> <destino> <ttl> <semente> [backbone]
 * Imprime um evento por linha e o snapshot da primeira chegada ao destino.
 */
public final class ConsoleRunner {

    private ConsoleRunner() {
    }

    private static ForwardingStrategy strategyFor(int version) {
        switch (version) {
            case 1: return new Strategy1AllInterfaces();
            case 2: return new Strategy2ExceptIncoming();
            case 3: return new Strategy3Ttl();
            case 4: return new Strategy4Custom();
            default:
                throw new IllegalArgumentException("Versao invalida (1..4): " + version);
        }
    }

    private static String route(Link link, int fromNode) {
        return link.getA() + "-" + link.getB() + " (" + fromNode + "->" + link.other(fromNode) + ")";
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Uso: ConsoleRunner <versao> <origem> <destino> <ttl> <semente> [backbone]");
            System.exit(2);
        }
        final int version = Integer.parseInt(args[0]);
        final int source = Integer.parseInt(args[1]);
        final int destination = Integer.parseInt(args[2]);
        final int ttl = Integer.parseInt(args[3]);
        final long seed = Long.parseLong(args[4]);
        Path file = args.length > 5 ? Paths.get(args[5]) : Paths.get("backbone.txt");

        Graph graph = BackboneLoader.load(file);
        final boolean showTtl = version >= 3;

        Simulator sim = new Simulator(graph, strategyFor(version),
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
    }
}
