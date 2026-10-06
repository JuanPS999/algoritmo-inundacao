/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: Packet
* Funcao...........: Pacote que trafega pelos links (id, messageId, TTL, interface).
*************************************************************** */
package model;

import model.Link;

/**
 * Pacote em transito. Carrega id, messageId, TTL e a interface (link) pela
 * qual chegou ao no atual; null quando gerado pelo transmissor.
 */
public class Packet {

    private final long id;
    private final long messageId;
    private int ttl;
    private final Link arrivalLink;
    private long arrivalTime;

    /* ***************************************************************
* Metodo: Packet
* Funcao: Construtor do pacote com id, messageId, TTL e link de chegada.
* Parametros: id - identificador da copia; messageId - identificador logico da mensagem;
 *           ttl - valor TTL; arrivalLink - link pelo qual chegou.
 * Retorno: void
*************************************************************** */
public Packet(long id, long messageId, int ttl, Link arrivalLink) {
        this.id = id;
        this.messageId = messageId;
        this.ttl = ttl;
        this.arrivalLink = arrivalLink;
    }

    /* ***************************************************************
* Metodo: Packet
* Funcao: Construtor do pacote com id, TTL e link de chegada (messageId defaults a 0).
* Parametros: id - identificador da copia; ttl - valor TTL; arrivalLink - link pelo qual chegou.
 *           messageId e definido como 0 para compatibilidade com V1/V2/V3.
 * Retorno: void
*************************************************************** */
public Packet(long id, int ttl, Link arrivalLink) {
        this.id = id;
        this.messageId = 0;
        this.ttl = ttl;
        this.arrivalLink = arrivalLink;
    }

    /* ***************************************************************
* Metodo: getId
* Funcao: Retorna o identificador do pacote.
* Parametros: nenhum
* Retorno: long - id do pacote.
*************************************************************** */
public long getId() {
        return id;
    }

    /* ***************************************************************
* Metodo: getMessageId
* Funcao: Retorna o identificador logico da mensagem a que este pacote pertence.
* Parametros: nenhum
* Retorno: long - messageId.
*************************************************************** */
public long getMessageId() {
        return messageId;
    }

    /* ***************************************************************
* Metodo: getTtl
* Funcao: Retorna o valor do TTL do pacote.
* Parametros: nenhum
* Retorno: int - valor do TTL.
*************************************************************** */
public int getTtl() {
        return ttl;
    }

    /* ***************************************************************
* Metodo: setTtl
* Funcao: Define o valor do TTL do pacote.
* Parametros: ttl - novo valor do TTL.
* Retorno: void
*************************************************************** */
public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    /** Link pelo qual o pacote chegou ao no atual; null na origem. */
    /* ***************************************************************
* Metodo: getArrivalLink
* Funcao: Retorna o link pelo qual o pacote chegou ao no atual.
* Parametros: nenhum
* Retorno: Link - link de chegada ou null na origem.
*************************************************************** */
public Link getArrivalLink() {
        return arrivalLink;
    }

    /* ***************************************************************
* Metodo: getArrivalTime
* Funcao: Retorna o instante em que este pacote chegou ao no (em ms virtual).
* Parametros: nenhum
* Retorno: long - instante de chegada em ms.
*************************************************************** */
public long getArrivalTime() {
        return arrivalTime;
    }

    /* ***************************************************************
* Metodo: setArrivalTime
* Funcao: Define o instante de chegada do pacote (chamado pelo Simulator).
* Parametros: t - instante virtual de chegada.
* Retorno: void
*************************************************************** */
public void setArrivalTime(long t) {
        this.arrivalTime = t;
    }
}