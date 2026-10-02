package flooding.sim;

import flooding.model.Graph;
import flooding.model.Link;

import java.util.List;

/**
 * Versao 4: STUB. Otimizacao do aluno ainda nao definida.
 * TODO: implementar aqui a otimizacao escolhida (e documenta-la no Sobre).
 * Por enquanto, delega integralmente a Versao 3.
 */
public class Strategy4Custom implements ForwardingStrategy {

    private final Strategy3Ttl delegate = new Strategy3Ttl();

    @Override
    public List<Link> forward(Graph graph, int nodeId, Packet packet) {
        return delegate.forward(graph, nodeId, packet);
    }
}
