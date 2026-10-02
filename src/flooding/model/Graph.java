package flooding.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Grafo nao direcionado da rede. Nos com ids em [1..N]; cada link aparece
 * uma unica vez e e indexado por id.
 */
public class Graph {

    private final int nodeCount;
    private final List<Node> nodes = new ArrayList<Node>();
    private final List<Link> links = new ArrayList<Link>();
    // adjacencia: para cada no, lista de links incidentes
    private final List<List<Link>> adjacency = new ArrayList<List<Link>>();

    public Graph(int nodeCount) {
        if (nodeCount < 1) {
            throw new IllegalArgumentException("Numero de nos deve ser >= 1: " + nodeCount);
        }
        this.nodeCount = nodeCount;
        for (int i = 1; i <= nodeCount; i++) {
            nodes.add(new Node(i));
            adjacency.add(new ArrayList<Link>());
        }
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Link> getLinks() {
        return Collections.unmodifiableList(links);
    }

    public Node getNode(int id) {
        return nodes.get(id - 1);
    }

    /** Links incidentes ao no (suas "interfaces"). */
    public List<Link> linksOf(int nodeId) {
        return Collections.unmodifiableList(adjacency.get(nodeId - 1));
    }

    public Link addLink(int a, int b, int weight) {
        validateNodeId(a);
        validateNodeId(b);
        if (a == b) {
            throw new IllegalArgumentException("Link de um no para ele mesmo: " + a);
        }
        Link link = new Link(links.size(), a, b, weight);
        links.add(link);
        adjacency.get(a - 1).add(link);
        adjacency.get(b - 1).add(link);
        return link;
    }

    private void validateNodeId(int id) {
        if (id < 1 || id > nodeCount) {
            throw new IllegalArgumentException(
                    "Id de no fora do intervalo [1.." + nodeCount + "]: " + id);
        }
    }

    /** Distancia em saltos (BFS) entre dois nos; -1 se inalcançavel. */
    public int hopDistance(int from, int to) {
        int[] visited = new int[nodeCount + 1]; // 0 = nao visitado; guarda distancia+1
        Deque<Integer> queue = new ArrayDeque<Integer>();
        queue.add(from);
        visited[from] = 1;
        while (!queue.isEmpty()) {
            int current = queue.poll();
            if (current == to) return visited[current] - 1;
            for (Link link : adjacency.get(current - 1)) {
                int next = link.other(current);
                if (visited[next] == 0) {
                    visited[next] = visited[current] + 1;
                    queue.add(next);
                }
            }
        }
        return -1;
    }

    /** Diametro do grafo em saltos (maior distancia minima entre dois nos). */
    public int diameter() {
        int max = 0;
        for (int i = 1; i <= nodeCount; i++) {
            for (int j = i + 1; j <= nodeCount; j++) {
                max = Math.max(max, hopDistance(i, j));
            }
        }
        return max;
    }
}
