package flooding.sim;

import flooding.model.Graph;
import flooding.model.Link;

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
