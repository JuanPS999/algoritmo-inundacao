/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: WindowSweep.java
* Funcao...........: Utilitario de linha de comando para executar
 * varias simulacoes da V4 com diferentes valores de janela e
 * comparar os resultados.
*************************************************************** */

package util;

import model.Graph;
import model.ForwardingStrategy;
import model.LatencyProvider;
import model.RunResult;
import model.Simulator;
import model.Strategy4Custom;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import util.BackboneLoader;
import util.Statistics;

/**
 * Executa varias simulacoes da V4 com diferentes janelas de deduplicacao
 * e imprime estatisticas agregadas.
 * Uso: java -cp build util.WindowSweep <n> <origem> <destino> <ttl> <sementeBase> [backbone]
 */
public final class WindowSweep {

    private WindowSweep() {
    }

    private static final long[] JANELAS = {0, 250, 500, 1000, 1500, 2000, 5000};

    /* ***************************************************************
    * Metodo: main
    * Funcao: Ponto de entrada do utilitario WindowSweep.
    * Parametros: args - argumentos da linha de comando
    * Retorno: void
    *************************************************************** */
    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Uso: WindowSweep <n> <origem> <destino> <ttl> <sementeBase> [backbone]");
            System.exit(2);
        }
        final int count = Integer.parseInt(args[0]);
        final int source = Integer.parseInt(args[1]);
        final int destination = Integer.parseInt(args[2]);
        final int ttl = Integer.parseInt(args[3]);
        final long baseSeed = Long.parseLong(args[4]);
        Path file = args.length > 5 ? Paths.get(args[5]) : Paths.get("backbone.txt");

        Graph graph = BackboneLoader.load(file);

        System.out.println("WindowSweep V4 - " + count + " execucoes por janela");
        System.out.println("Origem=" + source + " Destino=" + destination + " TTL=" + ttl + " SementeBase=" + baseSeed);
        System.out.println();

        for (long janelaMs : JANELAS) {
            List<Long> samples = new ArrayList<Long>();
            int unreachable = 0;
            int aborted = 0;
            long totalDuplicatas = 0;

            for (int i = 0; i < count; i++) {
                long seed = baseSeed + i;
                ForwardingStrategy strategy = new Strategy4Custom(janelaMs);
                Simulator sim = new Simulator(graph, strategy,
                        new LatencyProvider(seed), source, destination, ttl);
                RunResult result = sim.runUntilFirstArrival();
                if (result.isReached()) {
                    samples.add(Long.valueOf(result.getSnapshotCounter()));
                    totalDuplicatas += ((Strategy4Custom) strategy).getDuplicatasDescartadas();
                } else if (result.isAbortedByLimit()) {
                    aborted++;
                } else {
                    unreachable++;
                }
            }

            double media = Statistics.mean(samples);
            double desvio = Statistics.standardDeviation(samples);

            System.out.printf("Janela=%5dms  Media=%.2f  Desvio=%.2f  Inalcancaveis=%d  Abortadas=%d  DuplicatasTotal=%d%n",
                    janelaMs, media, desvio, unreachable, aborted, totalDuplicatas);
        }
    }
}