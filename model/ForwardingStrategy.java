/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: ForwardingStrategy.java
* Funcao...........: Interface de estrategia de reenvio de pacotes.
*************************************************************** */

package model;

import model.Graph;
import model.Link;

import java.util.List;

/**
 * Estrategia de reenvio: decide por quais interfaces do no o pacote sera
 * retransmitido. Pode alterar o pacote (ex.: decremento de TTL na V3).
 * O receptor TAMBEM reencaminha (decisao tomada no Simulator).
 */
public interface ForwardingStrategy {

    /**
     * @param graph   topologia da rede
     * @param nodeId  no que recebeu o pacote
     * @param packet  pacote recebido (a implementacao pode altera-lo)
     * @return links por onde retransmitir; lista vazia = descartar
     */
    List<Link> forward(Graph graph, int nodeId, Packet packet);
}
