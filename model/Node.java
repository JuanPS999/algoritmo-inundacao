/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: Node.java
* Funcao...........: No (roteador) da rede, identificado por id numerico.
*************************************************************** */

package model;

/**
 * No (roteador) da rede. Identificado por id numerico em [1..N].
 * Rotulo na GUI: 1->A, 2->B, ...
 */
public class Node {

    private final int id;

    /* ***************************************************************
* Metodo: Node
* Funcao: Construtor do no com o identificador especificado.
* Parametros: id - identificador do no.
* Retorno: void
*************************************************************** */
public Node(int id) {
        this.id = id;
    }

    /* ***************************************************************
* Metodo: getId
* Funcao: Retorna o identificador do no.
* Parametros: nenhum
* Retorno: int - id do no.
*************************************************************** */
public int getId() {
        return id;
    }

    /* ***************************************************************
* Metodo: getLabel
* Funcao: Retorna o rotulo do no (1->A, 2->B, etc.).
* Parametros: nenhum
* Retorno: String - rotulo do no.
*************************************************************** */
public String getLabel() {
        return String.valueOf((char) ('A' + id - 1));
    }

    @Override
    public String toString() {
        return getLabel() + "(" + id + ")";
    }
}
