/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: BatchConsole.java
* Funcao...........: Executa multiplas simulacoes em lote via linha
*                    de comando e imprime estatisticas por versao.
*************************************************************** */

package util;

import controller.BatchResult;
import controller.BatchRunner;
import controller.StrategyFactory;
import java.nio.file.Paths;
import model.Graph;

/**
 * Modo em lote para teste e validacao: executa N vezes cada versao
 * (1 a 4) com sementes base + i e imprime resultados estatisticos.
 */
public final class BatchConsole {

    private BatchConsole() {
    }

    /* ***************************************************************
    * Metodo: main
    * Funcao: ponto de entrada; le argumentos e executa o lote.
    * Parametros: args = <n> <origem> <destino> <ttl> <sementeBase> [backbone]
    * Retorno: void
    *************************************************************** */
    public static void main(String[] args) {
        if (args.length < 5 || args.length > 6) {
            System.err.println("Uso: java util.BatchConsole <n> <origem> <destino> <ttl> <sementeBase> [backbone]");
            System.exit(1);
        }
        int count = Integer.parseInt(args[0]);
        int sourceId = Integer.parseInt(args[1]);
        int destinationId = Integer.parseInt(args[2]);
        int ttl = Integer.parseInt(args[3]);
        long baseSeed = Long.parseLong(args[4]);
        String backbonePath = (args.length == 6) ? args[5] : "backbone.txt";

        Graph graph;
        try {
            graph = util.BackboneLoader.load(Paths.get(backbonePath));
        } catch (Exception e) {
            System.err.println("Erro ao carregar backbone: " + e.getMessage());
            System.exit(1);
            return;
        }

        if (sourceId < 1 || sourceId > graph.getNodeCount()
                || destinationId < 1 || destinationId > graph.getNodeCount()) {
            System.err.println("Origem ou destino invalido (1 a " + graph.getNodeCount() + ")");
            System.exit(1);
        }

        System.out.println("Versao,Validas,Abortadas,Inalcancaveis,Media,DesvioPadrao");

        for (int version = 1; version <= 4; version++) {
            long windowMs = (version == 4) ? 1000 : 0;
            BatchResult result = BatchRunner.run(graph, version, sourceId,
                    destinationId, ttl, count, baseSeed, windowMs);

            int valid = result.getSamples().size();
            int aborted = result.getAborted();
            int unreachable = result.getUnreachable();
            double mean = util.Statistics.mean(result.getSamples());
            double stdDev = util.Statistics.standardDeviation(result.getSamples());

            System.out.println(version + "," + valid + "," + aborted + ","
                    + unreachable + "," + mean + "," + stdDev);
        }
    }//fim do metodo main

}//fim da classe BatchConsole