package flooding.sim;

import flooding.model.Link;

/**
 * Pacote em transito. Carrega id, TTL e a interface (link) pela qual chegou
 * ao no atual; null quando gerado pelo transmissor.
 */
public class Packet {

    private final long id;
    private int ttl;
    private final Link arrivalLink;

    public Packet(long id, int ttl, Link arrivalLink) {
        this.id = id;
        this.ttl = ttl;
        this.arrivalLink = arrivalLink;
    }

    public long getId() {
        return id;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    /** Link pelo qual o pacote chegou ao no atual; null na origem. */
    public Link getArrivalLink() {
        return arrivalLink;
    }
}
