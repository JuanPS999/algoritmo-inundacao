/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: SimulationListener
* Funcao...........: Interface de listener para eventos da simulacao.
*************************************************************** */

package model;

import model.Link;
import model.Packet;

/**
 * Listener opcional para acompanhar a execucao da simulacao.
 * Todos os callbacks recebem o tempo virtual do evento.
 * Implementacoes devem ser leves; null listener = nenhum custo.
 */
public interface SimulationListener {

    /** Motivo de descarte: link desabilitado enquanto pacote estava em transito. */
    String DROP_LINK = "LINK";

    /** Motivo de descarte: TTL expirou (versoes 3 e 4). */
    String DROP_TTL = "TTL";

    /** Motivo de descarte: duplicata dentro da janela (versao 4). */
    String DROP_DUPLICATE = "DUPLICATA";

    /**
     * Uma copia foi enviada em um link. O contador ja foi incrementado.
     * @param time       tempo virtual do envio
     * @param fromNode   no que enviou a copia
     * @param link       link por onde a copia foi transmitida
     * @param packet     pacote transmitido (contem id, messageId, ttl)
     * @param counter    contador de pacotes gerados apos este envio
     */
    void onSend(long time, int fromNode, Link link, Packet packet, long counter);

    /**
     * Um pacote chegou a um no (antes de qualquer processamento).
     * @param time       tempo virtual da chegada
     * @param nodeId     no que recebeu o pacote
     * @param link       link por onde o pacote chegou
     * @param packet     pacote recebido
     * @param counter    contador de pacotes gerados ate agora
     */
    void onArrive(long time, int nodeId, Link link, Packet packet, long counter);

    /**
     * Um pacote foi descartado por algum motivo.
     * @param time       tempo virtual do descarte
     * @param nodeId     no onde ocorreria o processamento
     * @param link       link por onde o pacote chegou (ou null se na origem)
     * @param packet     pacote descartado
     * @param counter    contador de pacotes gerados ate agora
     * @param reason     motivo: DROP_LINK, DROP_TTL ou DROP_DUPLICATE
     */
    void onDrop(long time, int nodeId, Link link, Packet packet,
                long counter, String reason);

    /**
     * Primeira chegada ao destino (snapshot).
     * @param time            tempo virtual da primeira chegada
     * @param destinationId   id do no destino
     * @param snapshotCounter contador de pacotes gerados no snapshot
     * @param packet          pacote que atingiu o destino
     */
    void onFirstArrival(long time, int destinationId,
                        long snapshotCounter, Packet packet);
}