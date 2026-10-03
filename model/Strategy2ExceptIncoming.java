/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: Strategy2ExceptIncoming.java
* Funcao...........: Versao 2 de estrategia de flooding; reenvia exceto pela interface de chegada.
*************************************************************** */

package model;

import model.Graph;
import model.Link;

import java.util.ArrayList;
import java.util.List;

/**
 * Versao 2: reenvia por todas as interfaces EXCETO a de chegada.
 * Ignora TTL. So considera links habilitados.
 */
public class Strategy2ExceptIncoming implements ForwardingStrategy {

    @Override
    public List<Link> forward(Graph graph, int nodeId, Packet packet) {
        List<Link> out = new ArrayList<Link>();
        for (Link link : graph.linksOf(nodeId)) {
            if (link.isEnabled() && link != packet.getArrivalLink()) {
                out.add(link);
            }
        }
        return out;
    }
}
