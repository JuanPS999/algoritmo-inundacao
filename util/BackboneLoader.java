/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: BackboneLoader.java
* Funcao...........: Carrega o arquivo backbone.txt e constroi o grafo da rede.
*************************************************************** */

package util;

import model.Graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Carrega o arquivo backbone.txt:
 *   1a linha: numero de nos (pode ter ';' final, ex.: "8;").
 *   Demais:   NO1;NO2;PESO  - aresta nao direcionada, listada uma unica vez.
 * Linhas em branco e espacos sao ignorados. Ids validados em [1..N].
 */
public final class BackboneLoader {

    private BackboneLoader() {
    }

    public static Graph load(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String firstLine = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    firstLine = line;
                    break;
                }
            }
            if (firstLine == null) {
                throw new IOException("Arquivo backbone vazio: " + file);
            }
            if (firstLine.endsWith(";")) {
                firstLine = firstLine.substring(0, firstLine.length() - 1).trim();
            }
            int nodeCount;
            try {
                nodeCount = Integer.parseInt(firstLine);
            } catch (NumberFormatException e) {
                throw new IOException("Primeira linha deve conter o numero de nos: '"
                        + firstLine + "'", e);
            }
            Graph graph = new Graph(nodeCount);

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length != 3) {
                    throw new IOException("Linha " + lineNumber + ": esperado NO1;NO2;PESO, "
                            + "encontrado '" + line + "'");
                }
                try {
                    int a = Integer.parseInt(parts[0].trim());
                    int b = Integer.parseInt(parts[1].trim());
                    int weight = Integer.parseInt(parts[2].trim());
                    graph.addLink(a, b, weight);
                } catch (NumberFormatException e) {
                    throw new IOException("Linha " + lineNumber + ": valor nao numerico em '"
                            + line + "'", e);
                } catch (IllegalArgumentException e) {
                    throw new IOException("Linha " + lineNumber + ": " + e.getMessage(), e);
                }
            }
            return graph;
        }
    }
}
