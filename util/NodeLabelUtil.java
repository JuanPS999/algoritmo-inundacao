/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: NodeLabelUtil
* Funcao...........: Utilitario para conversao de id de no para rotulo
*                    alfabetico (1->A, 2->B, ..., 26->Z, 27->N27).
*************************************************************** */

package util;

/**
 * Utilitario para rotulos de nos.
 * Conversao: 1->A, 2->B, ..., 26->Z, 27->N27, 28->N28, etc.
 */
public final class NodeLabelUtil {

    private NodeLabelUtil() {
    }

    /* ***************************************************************
    * Metodo: toLabel
    * Funcao: Converte id numerico para rotulo alfabetico.
    * Parametros: id - identificador do no (1-based)
    * Retorno: String - rotulo (A-Z para 1-26, N27+ para maiores)
    *************************************************************** */
    public static String toLabel(int id) {
        if (id >= 1 && id <= 26) {
            return String.valueOf((char) ('A' + id - 1));
        }
        return "N" + id;
    }

    /* ***************************************************************
    * Metodo: fromLabel
    * Funcao: Converte rotulo alfabetico para id numerico (inverso de toLabel).
    *         So suporta formato A-Z e N27+.
    * Parametros: label - rotulo do no
    * Retorno: int - id numerico ou -1 se invalido
    *************************************************************** */
    public static int fromLabel(String label) {
        if (label == null || label.isEmpty()) {
            return -1;
        }
        if (label.length() == 1) {
            char c = label.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return c - 'A' + 1;
            }
        }
        if (label.startsWith("N")) {
            try {
                return Integer.parseInt(label.substring(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}