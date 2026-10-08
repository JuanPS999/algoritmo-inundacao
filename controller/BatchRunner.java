/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: BatchRunner
* Funcao...........: Executa multiplas simulacoes em lote (sem JavaFX)
*                    e agrega resultados estatisticos.
*************************************************************** */
package controller;

import java.util.ArrayList;
import java.util.List;

import model.Graph;
import model.LatencyProvider;
import model.RunResult;
import model.Simulator;

public class BatchRunner {

    /* ***************************************************************
    * Metodo: run
    * Funcao: executa 'count' simulacoes com sementes baseSeed + i,
    *         classifica resultados e devolve um BatchResult imutavel.
    * Parametros: graph = grafo de rede; version = versao da estrategia
    *             (1 a 4); sourceId = no transmissor; destinationId =
    *             no receptor; ttl = TTL inicial; count = numero de
    *             execucoes; baseSeed = semente base do LatencyProvider;
    *             windowMs = janela de deduplicacao para V4 (ignorado
    *             nas outras versoes)
    * Retorno: BatchResult com amostras, abortadas e inalcancaveis
    *************************************************************** */
    public static BatchResult run(Graph graph, int version, int sourceId,
            int destinationId, int ttl, int count, long baseSeed, long windowMs) {
        List<Long> samples = new ArrayList<Long>();
        int aborted = 0;
        int unreachable = 0;

        for (int i = 0; i < count; i++) {
            long seed = baseSeed + i;
            Simulator simulator = new Simulator(graph,
                    StrategyFactory.create(version, windowMs),
                    new LatencyProvider(seed),
                    sourceId, destinationId, ttl);
            RunResult result = simulator.runUntilFirstArrival();
            if (result.isReached()) {
                samples.add(Long.valueOf(result.getSnapshotCounter()));
            } else if (result.isAbortedByLimit()) {
                aborted++;
            } else {
                unreachable++;
            }
        }

        return new BatchResult(samples, aborted, unreachable);
    }//fim do metodo run

}//fim da classe BatchRunner