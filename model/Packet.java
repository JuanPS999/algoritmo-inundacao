/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: Packet
* Funcao...........: Pacote que trafega pelos links (id, TTL, interface).
*************************************************************** */
package model;

import model.Link;

/**
 * Pacote em transito. Carrega id, TTL e a interface (link) pela qual chegou
 * ao no atual; null quando gerado pelo transmissor.
 */
public class Packet {

    private final long id;
    private int ttl;
    private final Link arrivalLink;

    /* ***************************************************************
* Metodo: Packet
* Funcao: Construtor do pacote com id, TTL e link de chegada.
* Parametros: id - identificador do pacote; ttl - valor TTL; arrivalLink - link pelo qual chegou.
* Retorno: void
*************************************************************** */
public Packet(long id, int ttl, Link arrivalLink) {
        this.id = id;
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
}
