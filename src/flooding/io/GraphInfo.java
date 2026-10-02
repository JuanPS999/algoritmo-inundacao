package flooding.io;

import flooding.model.Graph;
import flooding.model.Link;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Programa de console (Marco 1): carrega o backbone, imprime o grafo
 * e o diametro em saltos.
 * Uso: java -cp build flooding.io.GraphInfo [caminho-do-backbone.txt]
 */
public final class GraphInfo {

    private GraphInfo() {
    }

    public static void main(String[] args) throws Exception {
        Path file = args.length > 0 ? Paths.get(args[0]) : Paths.get("backbone.txt");
        Graph graph = BackboneLoader.load(file);

        System.out.println("Nos: " + graph.getNodeCount());
        System.out.println("Links (" + graph.getLinks().size() + "):");
        for (Link link : graph.getLinks()) {
            System.out.println("  " + link);
        }
        System.out.println("Diametro (saltos): " + graph.diameter());
    }
}
