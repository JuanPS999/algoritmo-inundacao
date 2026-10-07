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

    private static final double PACKET_RADIUS = 6.0;
    private static final double MARGIN = 50.0;
    private static final double HIT_WIDTH = 16.0;
    private static final double GRID_SIZE = 40.0;
    private static final double HEX_GAP = 8.0; // espaco minimo entre hexagonos

    // Limites de raio do hexagono
    private static final double HEX_R_MIN = 18.0;
    private static final double HEX_R_MAX = 32.0;
    private static final double LABEL_MIN_SIZE = 14.0;
    private static final double LABEL_MAX_SIZE = 20.0;

    private Graph graph;
    private final Canvas bgCanvas = new Canvas();
    private final Group linksLayer = new Group();
    private final Group nodesLayer = new Group();
    private final Group packetsLayer = new Group();
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private double currentHexR = 24.0;
    private double currentLabelSize = 16.0;
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
    *         Calcula raio do hexagono e fonte proporcionais ao tamanho.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void computeLayout() {
        int count = graph.getNodeCount();
        nodeX = new double[count];
        nodeY = new double[count];
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;

        // Raio disponivel para o circulo dos nos
        double availableRadius = Math.min(getWidth(), getHeight()) / 2.0 - MARGIN;

        // Raio minimo para que hexagonos nao se sobreponham
        // Distancia entre centros adjacentes = 2 * R * sin(pi/n)
        // Deve ser >= 2 * HEX_R + HEX_GAP
        double minRadiusForNodes;
        if (count <= 1) {
            minRadiusForNodes = 0;
        } else {
            double angleBetween = Math.PI / count;
            // Usamos HEX_R_MAX como estimativa inicial para calcular o minimo
            minRadiusForNodes = (HEX_R_MAX + HEX_GAP / 2.0) / Math.sin(angleBetween);
        }

        double layoutRadius = Math.max(availableRadius, minRadiusForNodes);

        // Calcula raio do hexagono proporcional ao raio do layout
        // Quanto maior o layout, maior o hexagono (ate HEX_R_MAX)
        if (layoutRadius > 0) {
            double ratio = Math.min(1.0, layoutRadius / 300.0); // 300 = referencia
            currentHexR = HEX_R_MIN + (HEX_R_MAX - HEX_R_MIN) * ratio;
            currentLabelSize = LABEL_MIN_SIZE + (LABEL_MAX_SIZE - LABEL_MIN_SIZE) * ratio;
        } else {
            currentHexR = HEX_R_MIN;
            currentLabelSize = LABEL_MIN_SIZE;
        }

        for (int i = 0; i < count; i++) {
            double angle = 2.0 * Math.PI * i / count - Math.PI / 2.0;
            nodeX[i] = centerX + layoutRadius * Math.cos(angle);
            nodeY[i] = centerY + layoutRadius * Math.sin(angle);
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
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.fillText("N", cx - 3, cy - s - 4);
        bgNeedsRedraw = false;
    }//fim do metodo drawBackground

    /* ***************************************************************
    * Metodo: drawLinks
    * Funcao: desenha TODOS os cabos primeiro (contorno escuro + nucleo claro),
    *         depois TODAS as placas de peso por cima. Desabilitado: tracejado
    *         cinza #665c54 com marca de corte.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawLinks() {
        linksLayer.getChildren().clear();
        Color outerColor = Theme.BORDER_COLOR;        // #504945 - contorno escuro
        Color coreColor = Theme.YELLOW_COLOR;         // #d79921 - nucleo claro (amarelo sujo)
        Color disabledColor = Theme.INACTIVE_COLOR;   // #665c54

        // PASSO 1: Desenha TODOS os cabos (linhas)
        for (Link link : graph.getLinks()) {
            double x1 = nodeX[link.getA() - 1];
            double y1 = nodeY[link.getA() - 1];
            double x2 = nodeX[link.getB() - 1];
            double y2 = nodeY[link.getB() - 1];

            // Cabo: contorno externo escuro (mais grosso)
            Line outer = new Line(x1, y1, x2, y2);
            outer.setStrokeWidth(7.0);
            outer.setStrokeLineCap(StrokeLineCap.ROUND);
            outer.setMouseTransparent(true);

            // Cabo: nucleo claro (mais fino, por cima)
            Line core = new Line(x1, y1, x2, y2);
            core.setStrokeWidth(3.0);
            core.setStrokeLineCap(StrokeLineCap.ROUND);
            core.setMouseTransparent(true);

            // Area de clique (invisivel, por cima de tudo)
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
                // Marca de corte (X no meio) - vermelho
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
        }

        // PASSO 2: Desenha TODAS as placas de peso por cima dos cabos
        for (Link link : graph.getLinks()) {
            double cx = (nodeX[link.getA() - 1] + nodeX[link.getB() - 1]) / 2.0;
            double cy = (nodeY[link.getA() - 1] + nodeY[link.getB() - 1]) / 2.0;
            addWeightLabel(link.getWeight(), cx, cy, link.isEnabled());
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
    * Funcao: desenha nos como hexagonos ponta-para-cima (raio proporcional
    *         18-32, preenchimento #3c3836, borda 2px #a89984). Rotulo mono
    *         negrito proporcional (>=14). TX/RX como placas preenchidas.
    *         Destino atingido: borda laranja 4px.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawNodes() {
        nodesLayer.getChildren().clear();
        List<model.Node> nodes = graph.getNodes();
        double r = currentHexR;
        double labelSize = currentLabelSize;
        double tagSize = 12.0;
        double tagPadX = 8.0;
        double tagPadY = 3.0;

        for (int i = 0; i < nodes.size(); i++) {
            int id = nodes.get(i).getId();
            double cx = nodeX[i];
            double cy = nodeY[i];

            // Hexagono (ponta para cima)
            Polygon hex = new Polygon();
            for (int v = 0; v < 6; v++) {
                double angle = Math.toRadians(30 + v * 60);
                hex.getPoints().addAll(
                    cx + r * Math.cos(angle),
                    cy + r * Math.sin(angle)
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
            label.setFont(Font.font("Monospaced", FontWeight.BOLD, labelSize));
            label.setFill(Theme.TEXT_COLOR);
            label.setTextOrigin(VPos.CENTER);
            label.setX(cx - label.getLayoutBounds().getWidth() / 2.0);
            label.setY(cy);
            label.setMouseTransparent(true);

            // Etiqueta TX/RX como placa preenchida
            if (id == sourceId || id == destinationId) {
                String tagText = (id == sourceId) ? "TX" : "RX";
                Color tagFill = (id == sourceId) ? Theme.TX_COLOR_OBJ : Theme.RX_COLOR_OBJ;
                Color tagTextColor = Theme.TEXT_COLOR; // #ebdbb2

                Text tag = new Text(tagText);
                tag.setFont(Font.font("Monospaced", FontWeight.BOLD, tagSize));
                tag.setFill(tagTextColor);
                double tw = tag.getLayoutBounds().getWidth();
                double th = tag.getLayoutBounds().getHeight();
                double trw = tw + 2 * tagPadX;
                double trh = th + 2 * tagPadY;
                Rectangle tagBg = new Rectangle(
                    cx - trw / 2.0,
                    cy + r + 6.0, // 6px abaixo do hexagono
                    trw, trh
                );
                tagBg.setFill(tagFill);
                tagBg.setStroke(Theme.BORDER_COLOR);
                tagBg.setStrokeWidth(1.0);
                tagBg.setArcWidth(4.0);
                tagBg.setArcHeight(4.0);
                tagBg.setMouseTransparent(true);
                tag.setMouseTransparent(true);
                tag.setX(cx - tw / 2.0);
                tag.setY(cy + r + 6.0 + trh / 2.0 + th / 4.0);
                nodesLayer.getChildren().addAll(tagBg, tag);
            }

            nodesLayer.getChildren().addAll(hex, label);
        }
    }//fim do metodo drawNodes

}//fim da classe GraphPane