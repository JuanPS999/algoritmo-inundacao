/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: Simulator
* Funcao...........: Nucleo da simulacao em tempo virtual.
*************************************************************** */
package model;

import model.Graph;
import model.Link;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Nucleo da simulacao em TEMPO VIRTUAL: fila de eventos (PriorityQueue por
 * instante de chegada, desempate FIFO). Sem Thread.sleep e sem threads.
 *
 * Semantica da chegada de um pacote no no n no instante t:
 *   1) se o link foi desabilitado enquanto o pacote estava nele: descarte;
 *   2) se n e o receptor e ainda nao ha snapshot: registra (contador, t);
 *   3) pergunta a estrategia por quais interfaces reenviar
 *      (o receptor TAMBEM reencaminha);
 *   4) para cada copia enviada: contador++ e chegada agendada em
 *      t + LatencyProvider.nextLatencyMs().
 *
 * Inicio: o transmissor gera uma copia por interface; cada copia conta
 * como 1 pacote gerado.
 *
 * Uso pull-based pela GUI: advanceTo(tempoVirtual) e inFlight().
 * Modo em lote: runUntilFirstArrival().
 */
public class Simulator {

    /** Trava de seguranca do modo em lote: limite de transmissoes. */
    public static final long ABORT_LIMIT = 2000000L;

    /** Evento de chegada agendado de um pacote em um no. */
    private static final class Arrival implements Comparable<Arrival> {
        final long time;
        final long departureTime;
        final long seq; // desempate FIFO
        final int nodeId;
        final Packet packet;
        final Link link; // link em que o pacote esta em transito

        Arrival(long time, long departureTime, long seq, int nodeId, Packet packet, Link link) {
            this.time = time;
            this.departureTime = departureTime;
            this.seq = seq;
            this.nodeId = nodeId;
            this.packet = packet;
            this.link = link;
        }

        @Override
        public int compareTo(Arrival other) {
            if (time != other.time) return Long.compare(time, other.time);
            return Long.compare(seq, other.seq);
        }
    }

    /** Pacote em transito em um link, para desenho na GUI. */
    public static final class InFlight {
        public final Packet packet;
        public final Link link;
        public final long departureTime;
        public final long arrivalTime;

        InFlight(Packet p, Link link, long dep, long arr) {
            this.packet = p;
            this.link = link;
            this.departureTime = dep;
            this.arrivalTime = arr;
        }

        /** Fracao [0,1] do percurso no instante t. */
        public double progress(long t) {
            if (t <= departureTime) return 0.0;
            if (t >= arrivalTime) return 1.0;
            return (double) (t - departureTime) / (double) (arrivalTime - departureTime);
        }
    }

    /** Listener opcional de eventos (usado pelo ConsoleRunner). */
    public interface Listener {
        /** Uma copia enviada em um link: contador ja incrementado. */
        void onTransmit(long time, int fromNode, Link link, Packet packet, long counter);

        /** Pacote chegou ao no destino do link (antes de qualquer processamento). */
        void onArrive(long time, int nodeId, Link link, Packet packet, long counter);
    }

    private final Graph graph;
    private final ForwardingStrategy strategy;
    private final LatencyProvider latency;
    private final int sourceId;
    private final int destinationId;
    private final int initialTtl;

    private final PriorityQueue<Arrival> queue = new PriorityQueue<Arrival>();
    private long virtualTime = 0;
    private long packetsGenerated = 0;
    private long nextSeq = 0;
    private long nextPacketId = 0;
    private boolean started = false;

    // snapshot da primeira chegada ao destino
    private boolean snapshotTaken = false;
    private long snapshotCounter = -1;
    private long snapshotTime = -1;

    private Listener listener;

    public Simulator(Graph graph, ForwardingStrategy strategy, LatencyProvider latency,
                     int sourceId, int destinationId, int initialTtl) {
        this.graph = graph;
        this.strategy = strategy;
        this.latency = latency;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.initialTtl = initialTtl;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public long getVirtualTime() {
        return virtualTime;
    }

    public long getPacketsGenerated() {
        return packetsGenerated;
    }

    public boolean hasSnapshot() {
        return snapshotTaken;
    }

    public long getSnapshotCounter() {
        return snapshotCounter;
    }

    public long getSnapshotTime() {
        return snapshotTime;
    }

    /** Injetor inicial: uma copia por interface do transmissor. */
    public void start() {
        if (started) return;
        started = true;
        for (Link link : graph.linksOf(sourceId)) {
            if (!link.isEnabled()) continue;
            Packet p = new Packet(nextPacketId++, initialTtl, null);
            transmit(virtualTime, sourceId, link, p);
        }
    }

    private void transmit(long time, int fromNode, Link link, Packet packet) {
        packetsGenerated++;
        int to = link.other(fromNode);
        long arrival = time + latency.nextLatencyMs();
        // a copia que viaja carrega a interface de chegada = este link
        Packet travelling = new Packet(nextPacketId++, packet.getTtl(), link);
        queue.add(new Arrival(arrival, time, nextSeq++, to, travelling, link));
        if (listener != null) {
            listener.onTransmit(time, fromNode, link, packet, packetsGenerated);
        }
    }

    /** Avanca o tempo virtual ate t (inclusive), processando eventos. */
    public void advanceTo(long t) {
        start();
        while (!queue.isEmpty() && queue.peek().time <= t) {
            process(queue.poll());
        }
        virtualTime = Math.max(virtualTime, t);
    }

    /** Processa eventos ate a primeira chegada ao destino ou a fila esvaziar. */
    public RunResult runUntilFirstArrival() {
        start();
        boolean abortedByLimit = false;
        while (!hasSnapshot() && !queue.isEmpty()) {
            if (packetsGenerated > ABORT_LIMIT) {
                abortedByLimit = true;
                break;
            }
            advanceTo(queue.peek().time);
        }
        return new RunResult(hasSnapshot(), snapshotCounter, snapshotTime,
                packetsGenerated, virtualTime, abortedByLimit);
    }

    private void process(Arrival ev) {
        virtualTime = Math.max(virtualTime, ev.time);
        // 1) link desabilitado enquanto o pacote estava nele: descarte
        if (!ev.link.isEnabled()) {
            return;
        }
        if (listener != null) {
            listener.onArrive(ev.time, ev.nodeId, ev.link, ev.packet, packetsGenerated);
        }
        // 2) snapshot na primeira chegada ao receptor
        if (ev.nodeId == destinationId && !snapshotTaken) {
            snapshotTaken = true;
            snapshotCounter = packetsGenerated;
            snapshotTime = ev.time;
        }
        // 3) e 4) a estrategia decide; o receptor TAMBEM reencaminha
        List<Link> out = strategy.forward(graph, ev.nodeId, ev.packet);
        for (Link link : out) {
            transmit(ev.time, ev.nodeId, link, ev.packet);
        }
    }

    /** Pacotes atualmente em transito (para desenho na GUI). */
    public List<InFlight> inFlight() {
        List<InFlight> list = new ArrayList<InFlight>();
        for (Arrival ev : queue) {
            list.add(new InFlight(ev.packet, ev.link, ev.departureTime, ev.time));
        }
        return list;
    }
}
