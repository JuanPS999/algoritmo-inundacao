package flooding.sim;

import flooding.model.Graph;
import flooding.model.Link;

import java.util.ArrayList;
import java.util.List;

/**
 * Versao 1: reenvia por TODAS as interfaces, incluindo a de chegada.
 * Ignora TTL. So considera links habilitados.
 */
public class Strategy1AllInterfaces implements ForwardingStrategy {

    @Override
    public List<Link> forward(Graph graph, int nodeId, Packet packet) {
        List<Link> out = new ArrayList<Link>();
        for (Link link : graph.linksOf(nodeId)) {
            if (link.isEnabled()) {
                out.add(link);
            }
        }
        return out;
    }
}
