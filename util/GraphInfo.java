/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: GraphInfo.java
* Funcao...........: Programa de console que carrega o backbone e imprime o grafo e o diametro.
*************************************************************** */

package util;

import model.Graph;
import model.Link;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Programa de console (Marco 1): carrega o backbone, imprime o grafo
 * e o diametro em saltos.
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
