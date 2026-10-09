/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: GraphPane
* Funcao...........: Painel JavaFX que desenha o grafo (nos hexagonais,
*                    links estilo cabo), pacotes em transito e permite
*                    habilitar/desabilitar links com o clique.
*                    Fundo e pacotes em Canvas; grade e seta norte.
*                    Pulso visual de atividade nos nos (Canvas).
*                    Legenda discreta no canto.
*************************************************************** */
package view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

    private static final double PACKET_RADIUS = 4.0;
    private static final double MARGIN = 50.0;
    private static final double HIT_WIDTH = 16.0;
    private static final double GRID_SIZE = 40.0;
    private static final double HEX_GAP = 8.0;

    // Limites de raio do hexagono
    private static final double HEX_R_MIN = 18.0;
    private static final double HEX_R_MAX = 32.0;
    private static final double LABEL_MIN_SIZE = 14.0;
    private static final double LABEL_MAX_SIZE = 20.0;

    // Constantes visuais dos pacotes
    private static final Color PACKET_COLOR = Color.web(Theme.PACKET_COLOR, 0.85);
    private static final int MAX_PACKETS_FOR_TTL = 60;
    private static final double TTL_FONT_SIZE = 12.0;
    private static final double TTL_OFFSET_PERP = 14.0; // deslocamento perpendicular ao cabo

    // Placas de peso: distancia minima de outros cabos
    private static final double WEIGHT_LABEL_MIN_DIST = 18.0;
    private static final double[] WEIGHT_LABEL_T_CANDIDATES = {0.5, 0.4, 0.6, 0.3, 0.7};

    // Pulso de atividade: duracao ~225 ms em nanossegundos
    private static final long PULSE_DURATION_NS = 225_000_000L;
    private static final double PULSE_MAX_EXPANSION = 8.0;
    private static final double PULSE_STROKE_WIDTH = 2.5;

    // Legenda: dimensoes e estilo
    private static final double LEGEND_FONT_SIZE = 12.0;
    private static final double LEGEND_ITEM_HEIGHT = 18.0;
    private static final double LEGEND_PADDING = 8.0;
    private static final double LEGEND_GAP = 6.0;
    private static final double LEGEND_SYMBOL_SIZE = 14.0;

    private Graph graph;
    private final Canvas bgCanvas = new Canvas();
    private final Canvas packetsCanvas = new Canvas();
    private final Canvas pulseCanvas = new Canvas();
    private final Group linksLayer = new Group();
    private final Group nodesLayer = new Group();
    private final Group legendLayer = new Group();
    private double[] nodeX = new double[0];
    private double[] nodeY = new double[0];
    private double currentHexR = 24.0;
    private double currentLabelSize = 16.0;
    private int sourceId = 0;
    private int destinationId = 0;
    private boolean destinationReached = false;
    private boolean bgNeedsRedraw = true;

    // Conjunto de links com pacotes em transito (para destacar cabos ativos)
    private final Set<Link> activeLinks = new HashSet<Link>();

    // Posicoes calculadas das placas de peso (t ao longo do cabo, indexado por link id)
    private final Map<Link, Double> weightLabelPositions = new HashMap<Link, Double>();

    // Mapa de pulsos ativos: nodeId -> instante de ativacao (nanoTime)
    private final Map<Integer, Long> nodePulses = new HashMap<Integer, Long>();

    /* ***************************************************************
    * Metodo: setGraph
    * Funcao: substitui o grafo e redesenha.
    * Parametros: graph = novo grafo
    * Retorno: void
    *************************************************************** */
    public void setGraph(Graph graph) {
        this.graph = graph;
        nodePulses.clear();
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
        packetsCanvas.widthProperty().bind(widthProperty());
        packetsCanvas.heightProperty().bind(heightProperty());
        packetsCanvas.setMouseTransparent(true);
        pulseCanvas.widthProperty().bind(widthProperty());
        pulseCanvas.heightProperty().bind(heightProperty());
        pulseCanvas.setMouseTransparent(true);
        legendLayer.setMouseTransparent(true);
        getChildren().addAll(bgCanvas, linksLayer, nodesLayer, pulseCanvas, packetsCanvas, legendLayer);
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
        drawLegend();
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
    * Metodo: pulse
    * Funcao: registra um pulso de atividade no no indicado.
    *         O pulso e desenhado como contorno hexagonal laranja
    *         que se expande e desaparece em ~225 ms no Canvas.
    * Parametros: nodeId = id do no (1-based)
    * Retorno: void
    *************************************************************** */
    public void pulse(int nodeId) {
        if (nodeId >= 1 && nodeId <= graph.getNodeCount()) {
            nodePulses.put(nodeId, System.nanoTime());
        }
    }//fim do metodo pulse

    /* ***************************************************************
    * Metodo: showPackets
    * Funcao: redesenha pacotes em transito no Canvas unico.
    *         Cada pacote: ponto raio 3 em #d3869b alpha 0.85.
    *         TTL: monoespacado 12px se <= 150 pacotes e showTtl=true.
    *         Tambem desenha pulsos de atividade no pulseCanvas.
    * Parametros: flights = pacotes em transito; virtualTime = relogio virtual
    *             showTtl = true para exibir TTL
    * Retorno: void
    *************************************************************** */
    public void showPackets(List<Simulator.InFlight> flights,
            long virtualTime, boolean showTtl) {
        double w = getWidth();
        double h = getHeight();

        // Calcular conjunto de links com pacotes em transito
        Set<Link> newActiveLinks = new HashSet<Link>();
        for (Simulator.InFlight flight : flights) {
            newActiveLinks.add(flight.link);
        }

        // So redesenha cabos se o conjunto de links ativos mudou
        if (!newActiveLinks.equals(activeLinks)) {
            activeLinks.clear();
            activeLinks.addAll(newActiveLinks);
            drawLinks();
        }

        // Pacotes no packetsCanvas
        GraphicsContext gcPkt = packetsCanvas.getGraphicsContext2D();
        gcPkt.clearRect(0, 0, w, h);

        boolean drawTtl = showTtl && flights.size() <= MAX_PACKETS_FOR_TTL;

        if (drawTtl) {
            gcPkt.setFont(Font.font("Monospaced", FontWeight.BOLD, TTL_FONT_SIZE));
            gcPkt.setFill(Theme.TEXT_COLOR);
        }

        // Pacotes: preenchimento #d3869b, contorno 1px #1d2021, raio 4
        Color packetFill = Theme.PACKET_COLOR_OBJ; // #d3869b
        Color packetStroke = Theme.BG_COLOR;          // #1d2021

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

            // Contorno
            gcPkt.setStroke(packetStroke);
            gcPkt.setLineWidth(1.0);
            gcPkt.strokeOval(x - PACKET_RADIUS, y - PACKET_RADIUS,
                    PACKET_RADIUS * 2.0, PACKET_RADIUS * 2.0);
            // Preenchimento
            gcPkt.setFill(packetFill);
            gcPkt.fillOval(x - PACKET_RADIUS, y - PACKET_RADIUS,
                    PACKET_RADIUS * 2.0, PACKET_RADIUS * 2.0);

            if (drawTtl) {
                gcPkt.setFont(Font.font("Monospaced", FontWeight.BOLD, TTL_FONT_SIZE));
                // Calcular vetor perpendicular ao cabo para deslocamento
                double dx = nodeX[toId - 1] - fromX;
                double dy = nodeY[toId - 1] - fromY;
                double len = Math.hypot(dx, dy);
                double px = 0, py = 0;
                if (len > 0) {
                    // Perpendicular (rotacionado 90 graus): (-dy, dx) normalizado
                    px = -dy / len * TTL_OFFSET_PERP;
                    py = dx / len * TTL_OFFSET_PERP;
                }
                String ttlText = Integer.toString(flight.packet.getTtl());
                // Contorno escuro
                gcPkt.setStroke(Theme.BG_COLOR);
                gcPkt.setLineWidth(2.0);
                gcPkt.strokeText(ttlText, x + px, y + py);
                // Preenchimento
                gcPkt.setFill(Theme.TEXT_COLOR);
                gcPkt.fillText(ttlText, x + px, y + py);
            }
        }

        // Pulsos no pulseCanvas (chamado a cada frame pela animacao)
        drawPulses();
    }//fim do metodo showPackets

    /* ***************************************************************
    * Metodo: drawPulses
    * Funcao: desenha pulsos de atividade no pulseCanvas.
    *         Hexagono expandido com fade-out baseado no tempo real.
    *         Remove pulsos expirados.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawPulses() {
        double w = getWidth();
        double h = getHeight();
        GraphicsContext gc = pulseCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        long now = System.nanoTime();
        Color pulseColorBase = Theme.ORANGE_ACCENT_COLOR;

        for (int i = 0; i < nodeX.length; i++) {
            int nodeId = i + 1;
            Long pulseTime = nodePulses.get(nodeId);
            if (pulseTime == null) continue;

            long elapsed = now - pulseTime;
            if (elapsed >= PULSE_DURATION_NS) {
                nodePulses.remove(nodeId);
                continue;
            }

            double progress = (double) elapsed / PULSE_DURATION_NS;
            double cx = nodeX[i];
            double cy = nodeY[i];
            double r = currentHexR + PULSE_MAX_EXPANSION * progress;
            double alpha = 1.0 - progress;
            gc.setStroke(Color.web(Theme.ORANGE_ACCENT, alpha));
            gc.setLineWidth(PULSE_STROKE_WIDTH);

            // Desenha hexagono no Canvas
            gc.beginPath();
            for (int v = 0; v < 6; v++) {
                double angle = Math.toRadians(30 + v * 60);
                double x = cx + r * Math.cos(angle);
                double y = cy + r * Math.sin(angle);
                if (v == 0) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }
            gc.closePath();
            gc.stroke();
        }
    }//fim do metodo drawPulses

    /* ***************************************************************
    * Metodo: clearPackets
    * Funcao: limpa o Canvas de pacotes, Canvas de pulsos e estados.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void clearPackets() {
        GraphicsContext gcPkt = packetsCanvas.getGraphicsContext2D();
        GraphicsContext gcPulse = pulseCanvas.getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            gcPkt.clearRect(0, 0, w, h);
            gcPulse.clearRect(0, 0, w, h);
        }
        nodePulses.clear();
    }//fim do metodo clearPackets

    /* ***************************************************************
    * Metodo: pulseClearNow
    * Funcao: zera imediatamente o mapa de pulsos e limpa o canvas de pulsos.
    *          Isso garante que halos expirem em ate 300 ms reais,
    *          mesmo sem novos quadros da animacao.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void pulseClearNow() {
        nodePulses.clear();
        GraphicsContext gcPulse = pulseCanvas.getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        if (w > 0 && h > 0) {
            gcPulse.clearRect(0, 0, w, h);
        }
    }//fim do metodo pulseClearNow

    /* ***************************************************************
    * Metodo: redraw
    * Funcao: recalcula layout e redesenha fundo, links, nos e legenda.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void redraw() {
        computeLayout();
        drawBackground();
        drawLinks();
        drawNodes();
        drawLegend();
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
        weightLabelPositions.clear();
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;

        double availableRadius = Math.min(getWidth(), getHeight()) / 2.0 - MARGIN;

        double minRadiusForNodes;
        if (count <= 1) {
            minRadiusForNodes = 0;
        } else {
            double angleBetween = Math.PI / count;
            minRadiusForNodes = (HEX_R_MAX + HEX_GAP / 2.0) / Math.sin(angleBetween);
        }

        double layoutRadius = Math.max(availableRadius, minRadiusForNodes);

        if (layoutRadius > 0) {
            double ratio = Math.min(1.0, layoutRadius / 300.0);
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
        gc.setStroke(Theme.GRID_COLOR);
        gc.setLineWidth(0.5);
        for (double x = 0; x <= w; x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y <= h; y += GRID_SIZE) {
            gc.strokeLine(0, y, w, y);
        }
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
    * Funcao: desenha TODOS os cabos primeiro (contorno #504945 + nucleo),
    *         nucleo em repouso bege #a89984, nucleo ativo laranja #fe8019,
    *         desabilitado: tracejado #665c54 com X vermelho no meio.
    *         So redesenha quando o conjunto de links ativos muda.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawLinks() {
        linksLayer.getChildren().clear();
        Color outerColor = Theme.BORDER_COLOR;           // #504945
        Color coreRestColor = Theme.TEXT_SECONDARY_COLOR; // #a89984 (bege) - repouso
        Color coreActiveColor = Theme.ORANGE_ACCENT_COLOR; // #fe8019 (laranja) - ativo
        Color disabledColor = Theme.INACTIVE_COLOR;       // #665c54

        // Calcular posicoes das placas de peso se ainda nao calculadas (carregamento ou resize)
        boolean needCalc = weightLabelPositions.isEmpty();
        if (needCalc) {
            weightLabelPositions.clear();
            for (Link link : graph.getLinks()) {
                double cx = (nodeX[link.getA() - 1] + nodeX[link.getB() - 1]) / 2.0;
                double cy = (nodeY[link.getA() - 1] + nodeY[link.getB() - 1]) / 2.0;
                double t = calcularMelhorPosicaoPeso(link, cx, cy);
                weightLabelPositions.put(link, t);
            }
        }

        for (Link link : graph.getLinks()) {
            double x1 = nodeX[link.getA() - 1];
            double y1 = nodeY[link.getA() - 1];
            double x2 = nodeX[link.getB() - 1];
            double y2 = nodeY[link.getB() - 1];

            Line outer = new Line(x1, y1, x2, y2);
            outer.setStrokeWidth(7.0);
            outer.setStrokeLineCap(StrokeLineCap.ROUND);
            outer.setMouseTransparent(true);
            outer.setStroke(outerColor);

            Line core = new Line(x1, y1, x2, y2);
            core.setStrokeWidth(3.0);
            core.setStrokeLineCap(StrokeLineCap.ROUND);
            core.setMouseTransparent(true);

            Line hitArea = new Line(x1, y1, x2, y2);
            hitArea.setStrokeWidth(HIT_WIDTH);
            hitArea.setStroke(Color.TRANSPARENT);
            hitArea.setCursor(Cursor.HAND);

            if (link.isEnabled()) {
                boolean isActive = activeLinks.contains(link);
                core.setStroke(isActive ? coreActiveColor : coreRestColor);
            } else {
                outer.setStroke(disabledColor);
                outer.getStrokeDashArray().addAll(10.0, 6.0);
                core.setStroke(disabledColor);
                core.getStrokeDashArray().addAll(10.0, 6.0);
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

        // Desenhar placas de peso nas posicoes calculadas
        for (Link link : graph.getLinks()) {
            double x1 = nodeX[link.getA() - 1];
            double y1 = nodeY[link.getA() - 1];
            double x2 = nodeX[link.getB() - 1];
            double y2 = nodeY[link.getB() - 1];
            double t = weightLabelPositions.getOrDefault(link, 0.5);
            double cx = x1 + (x2 - x1) * t;
            double cy = y1 + (y2 - y1) * t;
            addWeightLabel(link.getWeight(), cx, cy, link.isEnabled(), t);
        }
    }//fim do metodo drawLinks

    /* ***************************************************************
    * Metodo: addWeightLabel
    * Funcao: placa de peso retangular com borda 1px, fundo painel,
    *         texto amarelo monoespacado.
    * Parametros: weight = peso; cx, cy = ponto medio; enabled = link ativo
    * Retorno: void
    *************************************************************** */
    private void addWeightLabel(int weight, double cx, double cy, boolean enabled, double t) {
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
    * Metodo: calcularMelhorPosicaoPeso
    * Funcao: encontra a melhor posicao t ao longo do cabo para a placa
    *         de peso, evitando cruzamentos com outros cabos.
    *         Testa t = 0.5, 0.4, 0.6, 0.3, 0.7; usa o primeiro onde
    *         a placa fica a mais de WEIGHT_LABEL_MIN_DIST de qualquer
    *         outro cabo. Se nenhum, usa 0.5.
    * Parametros: link = link atual; cx, cy = ponto medio do link
    * Retorno: double t (0..1) ao longo do cabo
    *************************************************************** */
    private double calcularMelhorPosicaoPeso(Link link, double cx, double cy) {
        double x1 = nodeX[link.getA() - 1];
        double y1 = nodeY[link.getA() - 1];
        double x2 = nodeX[link.getB() - 1];
        double y2 = nodeY[link.getB() - 1];
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len == 0) return 0.5;

        // Criar um texto temporario para medir dimensoes
        Text tempText = new Text(String.valueOf(link.getWeight()));
        tempText.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        double width = tempText.getLayoutBounds().getWidth();
        double height = tempText.getLayoutBounds().getHeight();
        double padX = 8.0;
        double padY = 3.0;
        double rw = width + 2 * padX;
        double rh = height + 2 * padY;

        for (double t : WEIGHT_LABEL_T_CANDIDATES) {
            double tx = x1 + dx * t;
            double ty = y1 + dy * t;

            // Verificar distancia a todos os outros cabos
            boolean ok = true;
            for (Link other : graph.getLinks()) {
                if (other == link) continue;
                double ox1 = nodeX[other.getA() - 1];
                double oy1 = nodeY[other.getA() - 1];
                double ox2 = nodeX[other.getB() - 1];
                double oy2 = nodeY[other.getB() - 1];

                // Distancia do ponto (tx, ty) ao segmento (ox1,oy1)-(ox2,oy2)
                double odx = ox2 - ox1;
                double ody = oy2 - oy1;
                double olen2 = odx * odx + ody * ody;
                double dist;
                if (olen2 == 0) {
                    dist = Math.hypot(tx - ox1, ty - oy1);
                } else {
                    double u = ((tx - ox1) * odx + (ty - oy1) * ody) / olen2;
                    u = Math.max(0, Math.min(1, u));
                    double closestX = ox1 + u * odx;
                    double closestY = oy1 + u * ody;
                    dist = Math.hypot(tx - closestX, ty - closestY);
                }

                if (dist < WEIGHT_LABEL_MIN_DIST + Math.max(rw, rh) / 2.0) {
                    ok = false;
                    break;
                }
            }
            if (ok) return t;
        }
        return 0.5;
    }//fim do metodo calcularMelhorPosicaoPeso

    /* ***************************************************************
    * Metodo: drawNodes
    * Funcao: desenha nos como hexagonos ponta-para-cima (raio proporcional
    *         18-32, preenchimento #3c3836, borda 2px #a89984). Rotulo mono
    *         negrito proporcional (>=14). TX/RX como placas preenchidas.
    *         Destino atingido: borda laranja 4px.
    *         NAO desenha pulsos aqui (feito no pulseCanvas em showPackets).
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

            Polygon hex = new Polygon();
            for (int v = 0; v < 6; v++) {
                double angle = Math.toRadians(30 + v * 60);
                hex.getPoints().addAll(
                    cx + r * Math.cos(angle),
                    cy + r * Math.sin(angle)
                );
            }
            hex.setFill(Theme.HEADER_COLOR);

            boolean isDest = destinationReached && id == destinationId;
            if (id == sourceId) {
                hex.setStroke(Theme.TX_COLOR_OBJ);
            } else if (id == destinationId) {
                hex.setStroke(Theme.RX_COLOR_OBJ);
            } else {
                hex.setStroke(Theme.TEXT_SECONDARY_COLOR);
            }
            hex.setStrokeWidth(isDest ? 4.0 : 2.0);
            if (isDest) {
                hex.setStroke(Theme.PACKET_COLOR_OBJ);
            }
            hex.setMouseTransparent(true);

            Text label = new Text(nodes.get(i).getLabel());
            label.setFont(Font.font("Monospaced", FontWeight.BOLD, labelSize));
            label.setFill(Theme.TEXT_COLOR);
            label.setTextOrigin(VPos.CENTER);
            label.setX(cx - label.getLayoutBounds().getWidth() / 2.0);
            label.setY(cy);
            label.setMouseTransparent(true);

            if (id == sourceId || id == destinationId) {
                String tagText = (id == sourceId) ? "TX" : "RX";
                Color tagFill = (id == sourceId) ? Theme.TX_COLOR_OBJ : Theme.RX_COLOR_OBJ;
                Color tagTextColor = Theme.TEXT_COLOR;

                Text tag = new Text(tagText);
                tag.setFont(Font.font("Monospaced", FontWeight.BOLD, tagSize));
                tag.setFill(tagTextColor);
                double tw = tag.getLayoutBounds().getWidth();
                double th = tag.getLayoutBounds().getHeight();
                double trw = tw + 2 * tagPadX;
                double trh = th + 2 * tagPadY;
                Rectangle tagBg = new Rectangle(
                    cx - trw / 2.0,
                    cy + r + 6.0,
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

    /* ***************************************************************
    * Metodo: drawLegend
    * Funcao: desenha legenda compacta no canto inferior direito.
    *         Itens: TX, RX, cabo em repouso, cabo ativo, cabo cortado,
    *                pacote (ponto c/ contorno), pulso (anel contorno).
    *         Texto 12px monoespacado, baixo contraste, nao interativo.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void drawLegend() {
        legendLayer.getChildren().clear();

        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        Font legendFont = Font.font("Monospaced", FontWeight.NORMAL, LEGEND_FONT_SIZE);
        Color textColor = Theme.TEXT_SECONDARY_COLOR; // baixo contraste

        // Itens da legenda: {cor do simbolo, texto, tipo}
        class LegendItem {
            Color color;
            String text;
            int type; // 0=filled rect (TX/RX), 1=line (cable), 2=dashed line with X, 3=filled circle with stroke (packet), 4=pulse ring
            LegendItem(Color c, String t, int tp) { color = c; text = t; type = tp; }
        }

        LegendItem[] items = new LegendItem[] {
            new LegendItem(Theme.TX_COLOR_OBJ, "TX", 0),
            new LegendItem(Theme.RX_COLOR_OBJ, "RX", 0),
            new LegendItem(Theme.TEXT_SECONDARY_COLOR, "cabo em repouso", 1),   // bege #a89984
            new LegendItem(Theme.ORANGE_ACCENT_COLOR, "cabo ativo", 1),           // laranja #fe8019
            new LegendItem(Theme.INACTIVE_COLOR, "cabo cortado", 2),              // tracejado #665c54 c/ X
            new LegendItem(Theme.PACKET_COLOR_OBJ, "pacote", 3),               // ponto #d3869b c/ contorno #1d2021
            new LegendItem(Theme.ORANGE_ACCENT_COLOR, "pulso", 4)                 // anel contorno
        };

        // Calcula largura total da legenda
        double maxTextWidth = 0;
        for (LegendItem item : items) {
            Text t = new Text(item.text);
            t.setFont(legendFont);
            double tw = t.getLayoutBounds().getWidth();
            if (tw > maxTextWidth) maxTextWidth = tw;
        }
        double legendWidth = LEGEND_PADDING * 2 + LEGEND_SYMBOL_SIZE + LEGEND_GAP + maxTextWidth;
        double legendHeight = LEGEND_PADDING * 2 + items.length * LEGEND_ITEM_HEIGHT;

        // Posicao: canto inferior direito com margem
        double margin = 16.0;
        double startX = w - legendWidth - margin;
        double startY = h - legendHeight - margin;

        // Fundo semi-transparente
        Rectangle bg = new Rectangle(startX, startY, legendWidth, legendHeight);
        bg.setFill(Color.web(Theme.PANEL, 0.85));
        bg.setStroke(Theme.BORDER_COLOR);
        bg.setStrokeWidth(1.0);
        bg.setArcWidth(4.0);
        bg.setArcHeight(4.0);
        bg.setMouseTransparent(true);
        legendLayer.getChildren().add(bg);

        // Itens
        double y = startY + LEGEND_PADDING + LEGEND_ITEM_HEIGHT * 0.75; // baseline aproximado
        for (LegendItem item : items) {
            double symX = startX + LEGEND_PADDING + LEGEND_SYMBOL_SIZE / 2.0;
            double symY = y - LEGEND_ITEM_HEIGHT / 2.5;

            if (item.type == 4) {
                // Pulso: hexagono so contorno laranja
                Polygon p = new Polygon();
                double pr = LEGEND_SYMBOL_SIZE / 2.0;
                for (int v = 0; v < 6; v++) {
                    double angle = Math.toRadians(30 + v * 60);
                    p.getPoints().addAll(
                        symX + pr * Math.cos(angle),
                        symY + pr * Math.sin(angle)
                    );
                }
                p.setFill(null);
                p.setStroke(item.color);
                p.setStrokeWidth(2.0);
                p.setStrokeLineCap(StrokeLineCap.ROUND);
                p.setMouseTransparent(true);
                legendLayer.getChildren().add(p);
            } else if (item.type == 2) {
                // Cabo cortado: linha tracejada cinza #665c54 com X
                Line l = new Line(symX - LEGEND_SYMBOL_SIZE / 2.0, symY, symX + LEGEND_SYMBOL_SIZE / 2.0, symY);
                l.setStroke(item.color);
                l.setStrokeWidth(2.5);
                l.getStrokeDashArray().addAll(6.0, 4.0);
                l.setMouseTransparent(true);
                legendLayer.getChildren().add(l);
                // X no meio
                Line x1 = new Line(symX - 4, symY - 4, symX + 4, symY + 4);
                Line x2 = new Line(symX - 4, symY + 4, symX + 4, symY - 4);
                x1.setStroke(Theme.RED_COLOR);
                x1.setStrokeWidth(1.5);
                x2.setStroke(Theme.RED_COLOR);
                x2.setStrokeWidth(1.5);
                x1.setMouseTransparent(true);
                x2.setMouseTransparent(true);
                legendLayer.getChildren().addAll(x1, x2);
            } else if (item.type == 3) {
                // Pacote: ponto cheio laranja #fe8019 com contorno 1px #1d2021
                Circle c = new Circle(symX, symY, LEGEND_SYMBOL_SIZE / 2.5);
                c.setFill(item.color);
                c.setStroke(Theme.BG_COLOR); // #1d2021
                c.setStrokeWidth(1.0);
                c.setMouseTransparent(true);
                legendLayer.getChildren().add(c);
            } else if (item.type == 1) {
                // Cabo: linha horizontal (bege ou laranja)
                Line l = new Line(symX - LEGEND_SYMBOL_SIZE / 2.0, symY, symX + LEGEND_SYMBOL_SIZE / 2.0, symY);
                l.setStroke(item.color);
                l.setStrokeWidth(3.0);
                l.setStrokeLineCap(StrokeLineCap.ROUND);
                l.setMouseTransparent(true);
                legendLayer.getChildren().add(l);
            } else {
                // TX/RX: retangulo preenchido
                Rectangle r = new Rectangle(symX - LEGEND_SYMBOL_SIZE / 2.0, symY - LEGEND_SYMBOL_SIZE / 3.0, LEGEND_SYMBOL_SIZE, LEGEND_SYMBOL_SIZE * 0.6);
                r.setFill(item.color);
                r.setStroke(Theme.BORDER_COLOR);
                r.setStrokeWidth(1.0);
                r.setArcWidth(2.0);
                r.setArcHeight(2.0);
                r.setMouseTransparent(true);
                legendLayer.getChildren().add(r);
            }

            // Texto
            Text t = new Text(item.text);
            t.setFont(legendFont);
            t.setFill(textColor);
            t.setX(symX + LEGEND_SYMBOL_SIZE / 2.0 + LEGEND_GAP);
            t.setY(symY + 4.0);
            t.setMouseTransparent(true);
            legendLayer.getChildren().add(t);

            y += LEGEND_ITEM_HEIGHT;
        }
    }//fim do metodo drawLegend

}//fim da classe GraphPane