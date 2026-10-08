/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: BatchResult
* Funcao...........: Resultado imutavel de uma execucao em lote
*                    (batch) contendo amostras, contagem de
*                    abortadas e inalcancaveis.
*************************************************************** */
package controller;

import java.util.Collections;
import java.util.List;

public final class BatchResult {

  private final List<Long> samples;
  private final int aborted;
  private final int unreachable;

  /* ***************************************************************
  * Metodo: BatchResult
  * Funcao: construtor; cria resultado imutavel com as amostras e
  *         contadores. A lista de amostras eh defensivamente copiada
  *         para garantir imutabilidade.
  * Parametros: samples = lista de contadores de pacotes (sucessos);
  *             aborted = numero de execucoes abortadas por limite;
  *             unreachable = numero de execucoes sem caminho ao destino
  * Retorno: nao se aplica
  *************************************************************** */
  public BatchResult(List<Long> samples, int aborted, int unreachable) {
    this.samples = Collections.unmodifiableList(samples);
    this.aborted = aborted;
    this.unreachable = unreachable;
  }//fim do construtor BatchResult

  /* ***************************************************************
  * Metodo: getSamples
  * Funcao: retorna lista imutavel das amostras (sucessos).
  * Parametros: nenhum
  * Retorno: List<Long> imutavel
  *************************************************************** */
  public List<Long> getSamples() {
    return samples;
  }//fim do metodo getSamples

  /* ***************************************************************
  * Metodo: getAborted
  * Funcao: retorna o numero de execucoes abortadas por limite.
  * Parametros: nenhum
  * Retorno: int
  *************************************************************** */
  public int getAborted() {
    return aborted;
  }//fim do metodo getAborted

  /* ***************************************************************
  * Metodo: getUnreachable
  * Funcao: retorna o numero de execucoes inalcancaveis.
  * Parametros: nenhum
  * Retorno: int
  *************************************************************** */
  public int getUnreachable() {
    return unreachable;
  }//fim do metodo getUnreachable

}//fim da classe BatchResult