/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: GraphPane
* Funcao...........: Painel JavaFX que desenha o grafo (nos em circulo,
*                    links com peso), os pacotes em transito e permite
*                    habilitar/desabilitar links com o clique.
*************************************************************** */
package view;

import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Graph;
import model.Link;
import model.Node;
import model.Simulator;

public class GraphPane extends Pane {

    private static final double NODE_RADIUS = 22.0;
    private static final double PACKET_RADIUS = 6.0;
    private static final double MARGIN = 40.0;
    private static final double MIN_LAYOUT_RADIUS = 40.0;
    private static final double HIT_WIDTH = 14.0;

    private Graph graph;
    private final Group linksLayer = new Group();
    private final Group nodesLayer = new Group();
    private final Group packetsLayer = new Group();
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private int sourceId = 0;
    private int destinationId = 0;
    private boolean destinationReached = false;

    /* ***************************************************************
    * Metodo: setGraph
    * Funcao: substitui o grafo e redesenha.
    * Parametros: graph = novo grafo
    * Retorno: void
    *************************************************************** */
    public void setGraph(Graph graph) {
        this.graph = graph;
        redraw();
    }//fim do metodo setGraph

    /* ***************************************************************
    * Metodo: GraphPane
    * Funcao: construtor; monta as camadas e desenha o grafo.
    * Parametros: graph = grafo carregado do backbone.txt
    * Retorno: nao se aplica
    *************************************************************** */
    public GraphPane(Graph graph) {
        this.graph = graph;
        setPrefSize(800.0, 600.0);
        setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(Theme.BG0, null, null)));
        packetsLayer.setMouseTransparent(true);
        getChildren().addAll(linksLayer, nodesLayer, packetsLayer);
        widthProperty().addListener((observable, oldValue, newValue) -> {
            redraw();
        });//fim do listener de largura
        heightProperty().addListener((observable, oldValue, newValue) -> {
            redraw();
        });//fim do listener de altura
        redraw();
    }//fim do construtor GraphPane

    /* ***************************************************************
    * Metodo: getGraph
    * Funcao: devolve o grafo exibido.
    * Parametros: nenhum
    * Retorno: Graph exibido no painel
    *************************************************************** */
    public Graph getGraph() {
        return graph;
    }//fim do metodo getGraph

    /* ***************************************************************
    * Metodo: setEndpoints
    * Funcao: define transmissor (verde) e receptor (azul) e redesenha.
    * Parametros: sourceId = id do transmissor; destinationId = id do
    *             receptor
    * Retorno: void
    *************************************************************** */
    public void setEndpoints(int sourceId, int destinationId) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        drawNodes();
    }//fim do metodo setEndpoints

    /* ***************************************************************
    * Metodo: setDestinationReached
    * Funcao: destaca (ou nao) o receptor quando o destino foi atingido.
    * Parametros: reached = true se o destino ja foi atingido
    * Retorno: void
    *************************************************************** */
    public void setDestinationReached(boolean reached) {
        this.destinationReached = reached;
        drawNodes();
    }//fim do metodo setDestinationReached

    /* ***************************************************************
    * Metodo: showPackets
    * Funcao: redesenha os pacotes em transito, interpolados ao longo do
    *         link; mostra o TTL ao lado quando solicitado.
    * Parametros: flights = pacotes em transito; virtualTime = relogio
    *             virtual; showTtl = true para exibir o TTL
    * Retorno: void
    *************************************************************** */
    public void showPackets(List<Simulator.InFlight> flights,
            long virtualTime, boolean showTtl) {
        packetsLayer.getChildren().clear();
        for (Simulator.InFlight flight : flights) {
            Link link = flight.link;
            int fromId = flight.fromNode;
            int toId = link.other(fromId);
            double progress = flight.progress(virtualTime);
            if (progress < 0.0) {
                progress = 0.0;
            }//fim do if
            if (progress > 1.0) {
                progress = 1.0;
            }//fim do if
            double fromX = nodeX[fromId - 1];
            double fromY = nodeY[fromId - 1];
            double x = fromX + (nodeX[toId - 1] - fromX) * progress;
            double y = fromY + (nodeY[toId - 1] - fromY) * progress;
            Circle dot = new Circle(x, y, PACKET_RADIUS);
            dot.setFill(Color.ORANGE);
            dot.setStroke(Color.BLACK);
            packetsLayer.getChildren().add(dot);
            if (showTtl) {
                Text ttlText = new Text(x + 8.0, y - 8.0,
                        "ttl=" + flight.packet.getTtl());
                ttlText.setFont(Font.font(11));
                ttlText.setFill(Theme.TEXTO);
                packetsLayer.getChildren().add(ttlText);
            }//fim do if
        }//fim do for
    }//fim do metodo showPackets

    /* ***************************************************************
    * Metodo: clearPackets
    * Funcao: remove todos os pacotes desenhados.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void clearPackets() {
        packetsLayer.getChildren().clear();
    }//fim do metodo clearPackets

    /* ***************************************************************
    * Metodo: redraw
    * Funcao: recalcula as posicoes e redesenha links e nos.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void redraw() {
        computeLayout();
        drawLinks();
        drawNodes();
    }//fim do metodo redraw

    /* ***************************************************************
    * Metodo: computeLayout
    * Funcao: posiciona os nos em circulo, centralizados no painel.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void computeLayout() {
        int count = graph.getNodeCount();
        nodeX = new double[count];
        nodeY = new double[count];
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        double radius = Math.min(getWidth(), getHeight()) / 2.0
                - NODE_RADIUS - MARGIN;
        if (radius < MIN_LAYOUT_RADIUS) {
            radius = MIN_LAYOUT_RADIUS;
        }//fim do if
        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count - Math.PI / 2.0;
            nodeX[i] = centerX + radius * Math.cos(angle);
            nodeY[i] = centerY + radius * Math.sin(angle);
        }//fim do for
    }//fim do metodo computeLayout

    /* ***************************************************************
    * Metodo: drawLinks
    * Funcao: desenha os links; clicar em um link alterna entre
    *         habilitado (Theme.LINK) e desabilitado (Theme.ERRO tracejado).
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawLinks() {
        linksLayer.getChildren().clear();
        for (Link link : graph.getLinks()) {
            double x1 = nodeX[link.getA() - 1];
            double y1 = nodeY[link.getA() - 1];
            double x2 = nodeX[link.getB() - 1];
            double y2 = nodeY[link.getB() - 1];
            Line line = new Line(x1, y1, x2, y2);
            line.setStrokeWidth(2.0);
            if (link.isEnabled()) {
                line.setStroke(Theme.LINK);
            }//fim do if
            else {
                line.setStroke(Theme.ERRO);
                line.getStrokeDashArray().addAll(8.0, 6.0);
            }//fim do else
            Line hitArea = new Line(x1, y1, x2, y2);
            hitArea.setStrokeWidth(HIT_WIDTH);
            hitArea.setStroke(Color.TRANSPARENT);
            hitArea.setOnMouseClicked(event -> {
                link.setEnabled(!link.isEnabled());
                drawLinks();
            });//fim do evento de clique
            linksLayer.getChildren().addAll(line, hitArea);
            addWeightLabel(link.getWeight(), (x1 + x2) / 2.0, (y1 + y2) / 2.0);
        }//fim do for
    }//fim do metodo drawLinks

    /* ***************************************************************
    * Metodo: addWeightLabel
    * Funcao: escreve o peso do link sobre um fundo Theme.BG2.
    *         Texto em Theme.MUDO.
    * Parametros: weight = peso; centerX, centerY = ponto medio do link
    * Retorno: void
    *************************************************************** */
    private void addWeightLabel(int weight, double centerX, double centerY) {
        Text text = new Text(String.valueOf(weight));
        text.setFont(Font.font(12));
        text.setTextOrigin(VPos.CENTER);
        text.setFill(Theme.MUDO);
        double width = text.getLayoutBounds().getWidth();
        double height = text.getLayoutBounds().getHeight();
        text.setX(centerX - width / 2.0);
        text.setY(centerY);
        Rectangle background = new Rectangle(centerX - width / 2.0 - 3.0,
                centerY - height / 2.0, width + 6.0, height);
        background.setFill(Theme.BG2);
        background.setOpacity(0.9);
        background.setMouseTransparent(true);
        text.setMouseTransparent(true);
        linksLayer.getChildren().addAll(background, text);
    }//fim do metodo addWeightLabel

    /* ***************************************************************
    * Metodo: drawNodes
    * Funcao: desenha os nos com rotulo; transmissor Theme.TRANSMISSOR,
    *         receptor Theme.RECEPTOR, demais Theme.BG2 com borda Theme.BORDA.
    *         Destino atingido: contorno Theme.ACENTO grosso.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawNodes() {
        nodesLayer.getChildren().clear();
        List<Node> nodes = graph.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            int id = nodes.get(i).getId();
            Circle circle = new Circle(nodeX[i], nodeY[i], NODE_RADIUS);
            circle.setFill(fillOf(id));
            if (destinationReached && id == destinationId) {
                circle.setStroke(Theme.ACENTO);
                circle.setStrokeWidth(4.0);
            }//fim do if
            else {
                circle.setStroke(Theme.BORDA);
                circle.setStrokeWidth(2.0);
            }//fim do else
            Text label = new Text(nodes.get(i).getLabel());
            label.setFont(Font.font(16));
            label.setTextOrigin(VPos.CENTER);
            label.setFill(Theme.TEXTO);
            label.setX(nodeX[i] - label.getLayoutBounds().getWidth() / 2.0);
            label.setY(nodeY[i]);
            circle.setMouseTransparent(true);
            label.setMouseTransparent(true);
            nodesLayer.getChildren().addAll(circle, label);
        }//fim do for
    }//fim do metodo drawNodes

    /* ***************************************************************
    * Metodo: fillOf
    * Funcao: escolhe a cor de preenchimento de um no.
    * Parametros: id = id do no
    * Retorno: Color transmissor, receptor ou BG2
    *************************************************************** */
    private Color fillOf(int id) {
        if (id == sourceId) {
            return Theme.TRANSMISSOR;
        }//fim do if
        if (id == destinationId) {
            return Theme.RECEPTOR;
        }//fim do if
        return Theme.BG2;
    }//fim do metodo fillOf

}//fim da classe GraphPane