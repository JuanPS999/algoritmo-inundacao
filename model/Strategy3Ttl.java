/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: Strategy3Ttl
* Funcao...........: Versao 3: exceto interface de chegada, com TTL.
*************************************************************** */
package model;

import model.Graph;
import model.Link;

import java.util.ArrayList;
import java.util.List;

/**
 * Versao 3: como a V2, e antes de reenviar faz ttl = ttl - 1;
 * se ttl <= 0, descarta. A checagem de entrega vem antes do descarte
 * (tratada no Simulator, no momento da chegada).
 */
public class Strategy3Ttl implements ForwardingStrategy {

    private String lastDropReason = null;

    @Override
    public List<Link> forward(Graph graph, int nodeId, Packet packet) {
        packet.setTtl(packet.getTtl() - 1);
        if (packet.getTtl() <= 0) {
            lastDropReason = SimulationListener.DROP_TTL;
            return new ArrayList<Link>(); // descarta
        }
        lastDropReason = null;
        List<Link> out = new ArrayList<Link>();
        for (Link link : graph.linksOf(nodeId)) {
            if (link.isEnabled() && link != packet.getArrivalLink()) {
                out.add(link);
            }
        }
        return out;
    }

    @Override
    public String getLastDropReason() {
        return lastDropReason;
    }
}
