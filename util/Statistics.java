/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: Statistics.java
* Funcao...........: Calcula mean e standard deviation amostral de List<Long>.
*************************************************************** */

package util;

import java.util.List;

/**
 * Utilitarios estatisticos para List<Long>.
 * Calcula mean e standard deviation amostral.
 * Lista vazia ou com menos de 2 valores devolve 0.0.
 */
public final class Statistics {

    private Statistics() {
    }

    /* ***************************************************************
    * Metodo: mean
    * Funcao: Calcula a media aritmetica dos valores.
    * Parametros: valores - Lista de valores Long.
    * Retorno: media dos valores, ou 0.0 se a lista estiver vazia ou ter
    *          apenas um elemento.
    *************************************************************** */
    public static double mean(List<Long> values) {
        if (values == null || values.isEmpty() || values.size() < 2) {
            return 0.0;
        }
        long sum = 0;
        for (Long v : values) {
            sum += v;
        }
        return (double) sum / values.size();
    }

    /* ***************************************************************
    * Metodo: standardDeviation
    * Funcao: Calcula o desvio-padrao amostral dos valores.
    * Parametros: valores - Lista de valores Long.
    * Retorno: desvio-padrao amostral, ou 0.0 se a lista estiver vazia ou ter
    *          menos de 2 elementos.
    *************************************************************** */
    public static double standardDeviation(List<Long> values) {
        if (values == null || values.isEmpty() || values.size() < 2) {
            return 0.0;
        }
        double avg = mean(values);
        double sumSqDiff = 0.0;
        for (Long v : values) {
            double diff = v - avg;
            sumSqDiff += diff * diff;
        }
        return Math.sqrt(sumSqDiff / (values.size() - 1));
    }
}