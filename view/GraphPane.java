/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: GraphPane
* Funcao...........: Painel JavaFX que desenha o grafo (nos hexagonais,
*                    links estilo cabo), pacotes em transito e permite
*                    habilitar/desabilitar links com o clique.
*                    Fundo em Canvas com grade e seta norte.
*************************************************************** */
package view;

import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.Graph;
import model.Link;
import model.Node;
import model.Simulator;

public class GraphPane extends Pane {

    private static final double NODE_RADIUS = 24.0;
    private static final double PACKET_RADIUS = 6.0;
    private static final double MARGIN = 50.0;
    private static final double MIN_LAYOUT_RADIUS = 40.0;
    private static final double HIT_WIDTH = 16.0;
    private static final double HEX_R = NODE_RADIUS;
    private static final double HEX_FLAT = HEX_R * Math.sqrt(3) / 2.0;
    private static final double GRID_SIZE = 40.0;

    private Graph graph;
    private final Canvas bgCanvas = new Canvas();
    private final Group linksLayer = new Group();
    private final Group nodesLayer = new Group();
    private final Group packetsLayer = new Group();
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private int sourceId = 0;
    private int destinationId = 0;
    private boolean destinationReached = false;
    private boolean bgNeedsRedraw = true;

    /* ***************************************************************
    * Metodo: setGraph
    * Funcao: substitui o grafo e redesenha.
    * Parametros: graph = novo grafo
    * Retorno: void
    *************************************************************** */
    public void setGraph(Graph graph) {
        this.graph = graph;
        bgNeedsRedraw = true;
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
        bgCanvas.widthProperty().bind(widthProperty());
        bgCanvas.heightProperty().bind(heightProperty());
        bgCanvas.setMouseTransparent(true);
        packetsLayer.setMouseTransparent(true);
        getChildren().addAll(bgCanvas, linksLayer, nodesLayer, packetsLayer);
        widthProperty().addListener((obs, o, n) -> {
            bgNeedsRedraw = true;
            redraw();
        });
        heightProperty().addListener((obs, o, n) -> {
            bgNeedsRedraw = true;
            redraw();
        });
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
    * Funcao: define transmissor e receptor e redesenha nos.
    * Parametros: sourceId = id do transmissor; destinationId = id do receptor
    * Retorno: void
    *************************************************************** */
    public void setEndpoints(int sourceId, int destinationId) {
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        drawNodes();
    }//fim do metodo setEndpoints

    /* ***************************************************************
    * Metodo: setDestinationReached
    * Funcao: destaca receptor quando destino atingido.
    * Parametros: reached = true se destino atingido
    * Retorno: void
    *************************************************************** */
    public void setDestinationReached(boolean reached) {
        this.destinationReached = reached;
        drawNodes();
    }//fim do metodo setDestinationReached

    /* ***************************************************************
    * Metodo: showPackets
    * Funcao: redesenha pacotes em transito interpolados ao longo do link.
    *         Mostra TTL ao lado quando solicitado (ate 150 pacotes).
    * Parametros: flights = pacotes em transito; virtualTime = relogio virtual
    *             showTtl = true para exibir TTL
    * Retorno: void
    *************************************************************** */
    public void showPackets(List<Simulator.InFlight> flights,
            long virtualTime, boolean showTtl) {
        packetsLayer.getChildren().clear();
        if (flights.size() > 150 && showTtl) {
            showTtl = false;
        }
        for (Simulator.InFlight flight : flights) {
            Link link = flight.link;
            int fromId = flight.fromNode;
            int toId = link.other(fromId);
            double progress = flight.progress(virtualTime);
            if (progress < 0.0) progress = 0.0;
            if (progress > 1.0) progress = 1.0;
            double fromX = nodeX[fromId - 1];
            double fromY = nodeY[fromId - 1];
            double x = fromX + (nodeX[toId - 1] - fromX) * progress;
            double y = fromY + (nodeY[toId - 1] - fromY) * progress;
            javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(x, y, PACKET_RADIUS);
            dot.setFill(Color.ORANGE);
            dot.setStroke(Color.BLACK);
            packetsLayer.getChildren().add(dot);
            if (showTtl) {
                Text ttlText = new Text(x + 8.0, y - 8.0, "ttl=" + flight.packet.getTtl());
                ttlText.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
                ttlText.setFill(Theme.TEXT_COLOR);
                packetsLayer.getChildren().add(ttlText);
            }
        }
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
    * Funcao: recalcula layout e redesenha fundo, links e nos.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void redraw() {
        computeLayout();
        drawBackground();
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
        double radius = Math.min(getWidth(), getHeight()) / 2.0 - NODE_RADIUS - MARGIN;
        if (radius < MIN_LAYOUT_RADIUS) radius = MIN_LAYOUT_RADIUS;
        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count - Math.PI / 2.0;
            nodeX[i] = centerX + radius * Math.cos(angle);
            nodeY[i] = centerY + radius * Math.sin(angle);
        }
    }//fim do metodo computeLayout

    /* ***************************************************************
    * Metodo: drawBackground
    * Funcao: desenha grade discreta e seta norte no Canvas de fundo.
    *         So redesenha se tamanho mudou.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawBackground() {
        if (!bgNeedsRedraw) return;
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;
        GraphicsContext gc = bgCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);
        gc.setFill(Theme.BG_COLOR);
        gc.fillRect(0, 0, w, h);
        // Grade fina
        gc.setStroke(Color.web("#2d2c2b"));
        gc.setLineWidth(0.5);
        for (double x = 0; x <= w; x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y <= h; y += GRID_SIZE) {
            gc.strokeLine(0, y, w, y);
        }
        // Seta norte (canto superior esquerdo)
        double cx = 40.0;
        double cy = 40.0;
        double s = 12.0;
        gc.setStroke(Theme.TEXT_SECONDARY_COLOR);
        gc.setFill(Theme.TEXT_SECONDARY_COLOR);
        gc.setLineWidth(1.5);
        gc.strokeLine(cx, cy + s, cx, cy - s);
        gc.strokeLine(cx, cy - s, cx - s / 2.0, cy - s / 2.0);
        gc.strokeLine(cx, cy - s, cx + s / 2.0, cy - s / 2.0);
        Text nLabel = new Text("N");
        nLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        nLabel.setFill(Theme.TEXT_SECONDARY_COLOR);
        // Desenha texto "N" via canvas
        gc.setFont(nLabel.getFont());
        gc.fillText("N", cx - 3, cy - s - 4);
        bgNeedsRedraw = false;
    }//fim do metodo drawBackground

    /* ***************************************************************
    * Metodo: drawLinks
    * Funcao: desenha links como cabos (externo 6px #504945, nucleo 2px #a89984)
    *         com placa de peso retangular borda 1px. Desabilitado: tracejado
    *         cinza #665c54 com marca de corte.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawLinks() {
        linksLayer.getChildren().clear();
        Color outerColor = Theme.BORDER_COLOR;        // #504945
        Color coreColor = Theme.TEXT_SECONDARY_COLOR; // #a89984
        Color disabledColor = Theme.INACTIVE_COLOR;   // #665c54
        for (Link link : graph.getLinks()) {
            double x1 = nodeX[link.getA() - 1];
            double y1 = nodeY[link.getA() - 1];
            double x2 = nodeX[link.getB() - 1];
            double y2 = nodeY[link.getB() - 1];
            // Cabo: linha externa grossa
            Line outer = new Line(x1, y1, x2, y2);
            outer.setStrokeWidth(6.0);
            outer.setStrokeLineCap(StrokeLineCap.ROUND);
            // Cabo: nucleo fino
            Line core = new Line(x1, y1, x2, y2);
            core.setStrokeWidth(2.0);
            core.setStrokeLineCap(StrokeLineCap.ROUND);
            core.setMouseTransparent(true);
            // Area de clique
            Line hitArea = new Line(x1, y1, x2, y2);
            hitArea.setStrokeWidth(HIT_WIDTH);
            hitArea.setStroke(Color.TRANSPARENT);
            hitArea.setCursor(Cursor.HAND);
            if (link.isEnabled()) {
                outer.setStroke(outerColor);
                core.setStroke(coreColor);
            } else {
                outer.setStroke(disabledColor);
                outer.getStrokeDashArray().addAll(10.0, 6.0);
                core.setStroke(disabledColor);
                core.getStrokeDashArray().addAll(10.0, 6.0);
                // Marca de corte (X no meio)
                double mx = (x1 + x2) / 2.0;
                double my = (y1 + y2) / 2.0;
                double cut = 10.0;
                Line cut1 = new Line(mx - cut, my - cut, mx + cut, my + cut);
                Line cut2 = new Line(mx - cut, my + cut, mx + cut, my - cut);
                cut1.setStroke(Theme.RED_COLOR);
                cut1.setStrokeWidth(2.0);
                cut2.setStroke(Theme.RED_COLOR);
                cut2.setStrokeWidth(2.0);
                cut1.setMouseTransparent(true);
                cut2.setMouseTransparent(true);
                linksLayer.getChildren().addAll(cut1, cut2);
            }
            hitArea.setOnMouseClicked(event -> {
                link.setEnabled(!link.isEnabled());
                drawLinks();
            });
            linksLayer.getChildren().addAll(outer, core, hitArea);
            addWeightLabel(link.getWeight(), (x1 + x2) / 2.0, (y1 + y2) / 2.0, link.isEnabled());
        }
    }//fim do metodo drawLinks

    /* ***************************************************************
    * Metodo: addWeightLabel
    * Funcao: placa de peso retangular com borda 1px, fundo painel,
    *         texto amarelo monoespacado.
    * Parametros: weight = peso; cx, cy = ponto medio; enabled = link ativo
    * Retorno: void
    *************************************************************** */
    private void addWeightLabel(int weight, double cx, double cy, boolean enabled) {
        Text text = new Text(String.valueOf(weight));
        text.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        text.setFill(enabled ? Theme.YELLOW_COLOR : Theme.TEXT_SECONDARY_COLOR);
        double width = text.getLayoutBounds().getWidth();
        double height = text.getLayoutBounds().getHeight();
        double padX = 8.0;
        double padY = 3.0;
        double rw = width + 2 * padX;
        double rh = height + 2 * padY;
        Rectangle bg = new Rectangle(cx - rw / 2.0, cy - rh / 2.0, rw, rh);
        bg.setFill(Theme.PANEL_COLOR);
        bg.setStroke(enabled ? Theme.BORDER_COLOR : Theme.INACTIVE_COLOR);
        bg.setStrokeWidth(1.0);
        bg.setMouseTransparent(true);
        text.setMouseTransparent(true);
        text.setX(cx - width / 2.0);
        text.setY(cy + height / 4.0);
        linksLayer.getChildren().addAll(bg, text);
    }//fim do metodo addWeightLabel

    /* ***************************************************************
    * Metodo: drawNodes
    * Funcao: desenha nos como hexagonos ponta-para-cima (raio 24,
    *         preenchimento #3c3836, borda 2px #a89984). Rotulo mono
    *         negrito. TX/RX sob nos. Destino atingido: borda laranja 4px.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawNodes() {
        nodesLayer.getChildren().clear();
        List<model.Node> nodes = graph.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            int id = nodes.get(i).getId();
            double cx = nodeX[i];
            double cy = nodeY[i];
            // Hexagono (ponta para cima)
            Polygon hex = new Polygon();
            // Vertices: 30, 90, 150, 210, 270, 330 graus
            for (int v = 0; v < 6; v++) {
                double angle = Math.toRadians(30 + v * 60);
                hex.getPoints().addAll(
                    cx + HEX_R * Math.cos(angle),
                    cy + HEX_R * Math.sin(angle)
                );
            }
            hex.setFill(Theme.HEADER_COLOR); // #3c3836
            boolean isDest = destinationReached && id == destinationId;
            if (id == sourceId) {
                hex.setStroke(Theme.TX_COLOR_OBJ); // #98971a
            } else if (id == destinationId) {
                hex.setStroke(Theme.RX_COLOR_OBJ); // #458588
            } else {
                hex.setStroke(Theme.TEXT_SECONDARY_COLOR); // #a89984
            }
            hex.setStrokeWidth(isDest ? 4.0 : 2.0);
            if (isDest) {
                hex.setStroke(Theme.ORANGE_ACCENT_COLOR); // #fe8019
            }
            hex.setMouseTransparent(true);
            // Rotulo do no
            Text label = new Text(nodes.get(i).getLabel());
            label.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
            label.setFill(Theme.TEXT_COLOR);
            label.setTextOrigin(VPos.CENTER);
            label.setX(cx - label.getLayoutBounds().getWidth() / 2.0);
            label.setY(cy);
            label.setMouseTransparent(true);
            // Etiqueta TX/RX
            Text tag = null;
            if (id == sourceId) {
                tag = new Text("TX");
                tag.setFill(Theme.TX_COLOR_OBJ);
            } else if (id == destinationId) {
                tag = new Text("RX");
                tag.setFill(Theme.RX_COLOR_OBJ);
            }
            if (tag != null) {
                tag.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
                tag.setX(cx - tag.getLayoutBounds().getWidth() / 2.0);
                tag.setY(cy + NODE_RADIUS + 14.0);
                tag.setMouseTransparent(true);
                nodesLayer.getChildren().add(tag);
            }
            nodesLayer.getChildren().addAll(hex, label);
        }
    }//fim do metodo drawNodes

}//fim da classe GraphPane