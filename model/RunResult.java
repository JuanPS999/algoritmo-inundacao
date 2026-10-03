/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: RunResult.java
* Funcao...........: Resultado de uma execucao do simulador.
*************************************************************** */

package model;

/**
 * Resultado de uma execucao (runUntilFirstArrival).
 */
public class RunResult {

    private final boolean reached;
    private final long snapshotCounter;
    private final long snapshotTime;
    private final long packetsGenerated;
    private final long virtualTime;
    private final boolean abortedByLimit;

    public RunResult(boolean reached, long snapshotCounter, long snapshotTime,
                     long packetsGenerated, long virtualTime, boolean abortedByLimit) {
        this.reached = reached;
        this.snapshotCounter = snapshotCounter;
        this.snapshotTime = snapshotTime;
        this.packetsGenerated = packetsGenerated;
        this.virtualTime = virtualTime;
        this.abortedByLimit = abortedByLimit;
    }

    /* ***************************************************************
* Metodo: isReached
* Funcao: Verifica se o destino foi alcancado.
* Parametros: nenhum
* Retorno: boolean - true se o destino foi alcancado.
*************************************************************** */
public boolean isReached() {
        return reached;
    }

    /* ***************************************************************
* Metodo: getSnapshotCounter
* Funcao: Retorna o contador de pacotes gerados na primeira chegada ao destino.
* Parametros: nenhum
* Retorno: long - contador de pacotes.
*************************************************************** */
public long getSnapshotCounter() {
        return snapshotCounter;
    }

    /* ***************************************************************
* Metodo: getSnapshotTime
* Funcao: Retorna o tempo virtual da primeira chegada ao destino.
* Parametros: nenhum
* Retorno: long - tempo da primeira chegada.
*************************************************************** */
public long getSnapshotTime() {
        return snapshotTime;
    }

    /* ***************************************************************
* Metodo: getPacketsGenerated
* Funcao: Retorna o total de pacotes gerados durante a simulacao.
* Parametros: nenhum
* Retorno: long - total de pacotes.
*************************************************************** */
public long getPacketsGenerated() {
        return packetsGenerated;
    }

    /* ***************************************************************
* Metodo: getVirtualTime
* Funcao: Retorna o tempo virtual final da simulacao.
* Parametros: nenhum
* Retorno: long - tempo virtual final.
*************************************************************** */
public long getVirtualTime() {
        return virtualTime;
    }

    /* ***************************************************************
* Metodo: isAbortedByLimit
* Funcao: Verifica se a simulacao foi encerrada por atingir o limite de transmissoes.
* Parametros: nenhum
* Retorno: boolean - true se foi abortado pelo limite.
*************************************************************** */
public boolean isAbortedByLimit() {
        return abortedByLimit;
    }
}
