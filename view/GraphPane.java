/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: GraphPane
* Funcao...........: Painel JavaFX que desenha o grafo a partir do backbone,
*                      com nos em circulo, rotulos A..H e pesos nos links.
* Requisito: grafo inteiro deve ficar visivel dentro do viewport, sem cortes,
*              raio calculado dinamicamente a partir do tamanho disponivel.
*************************************************************** */
package view;

import model.Graph;
import model.Link;
import model.Node;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Painel que desenha o grafo em circulo: nos com rotulos A..H,
* linhas para os links e o peso escrito em cada link.
* O grafo eh centralizado e dimensionado para caber dentro do viewport,
* calculando o raio a partir do tamanho disponivel, deixando margem para
* nos e rotulos. O posicionamento e responsivo a redimensionamento.
* Clique em um link alterna estado habilitado/desabilitado
* (cor vermelha quando desabilitado).
*/
public class GraphPane extends Pane {

    private static final double NODE_RADIUS = 25;
    private static final double LABEL_OFFSET = 30; // offset do rotulo ao centro

    private Graph graph;
    private double[] nodeX;
    private double[] nodeY;

    /* ***************************************************************
* Metodo: GraphPane
* Funcao: Construtor que carrega o backbone e inicializa o desenho.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
public GraphPane() {
        loadAndDrawGraph();
        // Define tamanho preferido baseado em dimensao razoavel
        setPrefSize(800, 600);
    }

    /* ***************************************************************
* Metodo: loadAndDrawGraph
* Funcao: Carrega backbone.txt e redesenha o grafo.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
private void loadAndDrawGraph() {
        try {
            java.nio.file.Path backbone = java.nio.file.Paths.get("backbone.txt");
            graph = util.BackboneLoader.load(backbone);
            computeLayout();
            drawLinks();
            drawNodes();
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /* ***************************************************************
* Metodo: computeLayout
* Funcao: Posiciona os nos em circulo uniforme, calculando raio baseado
*         no tamanho disponivel do GraphPane.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
private void computeLayout() {
        int n = graph.getNodeCount();
        if (n <= 0) {
            nodeX = new double[0];
            nodeY = new double[0];
            return;
        }

        // Calcula raio baseado no tamanho disponivel do pane com margem segura
        double availableWidth = getWidth() > 0 ? getWidth() : 800;
        double availableHeight = getHeight() > 0 ? getHeight() : 600;

        // Margem minima: considerar diametro dos nos + espaco para rotulos
        double minMargin = (NODE_RADIUS * 2 + LABEL_OFFSET) * 1.2;
        double radius = Math.min(availableWidth, availableHeight) / 2.0 - minMargin;

        // Garante raio minimo positivo
        if (radius < 50) radius = 50;

        double angleStep = 2 * Math.PI / n;
        nodeX = new double[n];
        nodeY = new double[n];

        for (int i = 0; i < n; i++) {
            double angle = i * angleStep - Math.PI / 2; // -PI/2 para comecar no topo (12h)
            double x = getWidth() / 2.0 + radius * Math.cos(angle);
            double y = getHeight() / 2.0 + radius * Math.sin(angle);
            nodeX[i] = x;
            nodeY[i] = y;
        }
    }

    /* ***************************************************************
* Metodo: layoutBoundsChanged
* Funcao: Called when the pane's layout bounds change (e.g., resize).
*          Recalculates layout and redraws.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
@Override
protected void layoutChildren() {
        super.layoutChildren();
        // Recalcular posicoes se o grafo ja estiver carregado
        if (graph != null) {
            computeLayout();
        }
        drawLinks();
        drawNodes();
    }

    /* ***************************************************************
* Metodo: drawLinks
* Funcao: Desenha todas as arestas (links) do grafo com pesos.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
private void drawLinks() {
        getChildren().clear();
        if (graph == null) return;
        List<Link> links = graph.getLinks();
        for (int i = 0; i < links.size(); i++) {
            Link link = links.get(i);
            int a = link.getA();
            int b = link.getB();
            double x1 = nodeX[a - 1];
            double y1 = nodeY[a - 1];
            double x2 = nodeX[b - 1];
            double y2 = nodeY[b - 1];
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(Color.BLACK);
            line.setStrokeWidth(2);
            if (!link.isEnabled()) {
                line.setStroke(Color.RED);
                // link desabilitado: cor vermelha
            }
            line.setOnMouseClicked(e -> {
                link.setEnabled(!link.isEnabled());
                drawLinks();
                drawNodes();
            });
            getChildren().add(line);

            // desenhar peso no meio do link
            double midX = (x1 + x2) / 2;
            double midY = (y1 + y2) / 2;
            Text weightText = new Text(String.valueOf(link.getWeight()));
            weightText.setFont(Font.font(12));
            weightText.setFill(Color.BLACK);
            getChildren().add(weightText);
            double dx = x2 - x1;
            double dy = y2 - y1;
            double len = Math.sqrt(dx * dx + dy * dy);
            double angle = Math.atan2(dy, dx);
            weightText.setTranslateX(midX + (len / 2) * Math.cos(angle + Math.PI / 2));
            weightText.setTranslateY(midY + (len / 2) * Math.sin(angle + Math.PI / 2));
        }
    }

    /* ***************************************************************
* Metodo: drawNodes
* Funcao: Desenha nos como circulos com rotulos A..H.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
private void drawNodes() {
        getChildren().clear();
        if (graph == null) return;
        List<Node> nodes = graph.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            double x = nodeX[i];
            double y = nodeY[i];
            Circle circle = new Circle(x, y, NODE_RADIUS);
            circle.setFill(Color.WHITE);
            circle.setStroke(Color.BLACK);
            circle.setStrokeWidth(2);
            getChildren().add(circle);
            Text label = new Text(nodes.get(i).getLabel());
            label.setFont(Font.font(14));
            label.setFill(Color.BLACK);
            // Posicionar rotulo levemente acima e centralizado do no
            label.setX(x - label.getLayoutBounds().getWidth() / 2);
            label.setY(y - NODE_RADIUS - 5); // acima do circulo
            getChildren().add(label);
        }
    }

    /* ***************************************************************
* Metodo: getGraph
* Funcao: Retorna o grafico carregado.
* Parametros: nenhum
* Retorno: Graph
*************************************************************** */
public Graph getGraph() {
        return graph;
    }

    /* ***************************************************************
* Metodo: getNodeX
* Funcao: Retorna a posicao X do no.
* Parametros: indice - indice do no nao array (0-based).
* Retorno: double - posicao X.
*************************************************************** */
public double getNodeX(int indice) {
        if (indice >= 0 && indice < nodeX.length) return nodeX[indice];
        return 0;
    }

    /* ***************************************************************
* Metodo: getNodeY
* Funcao: Retorna a posicao Y do no.
* Parametros: indice - indice do no nao array (0-based).
* Retorno: double - posicao Y.
*************************************************************** */
public double getNodeY(int indice) {
        if (indice >= 0 && indice < nodeY.length) return nodeY[indice];
        return 0;
    }
}