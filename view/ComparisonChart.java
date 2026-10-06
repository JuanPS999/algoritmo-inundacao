/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: ComparisonChart.java
* Funcao...........: Grafico de comparacao simples (Pane com Rectangle,
*                    Line e Label). Uma barra por versao com media
*                    e bigode de +-1 desvio. Sem uso de BarChart.
*************************************************************** */

package view;

import java.util.Collections;
import java.util.List;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import util.Statistics;

/**
 * Grafico de barras simples para comparar as 4 versoes.
 * Mostra a media (barra) e +-1 desvio-padrao (bigode).
 * Redesenha automaticamente quando notificado.
 * Suporta escala linear e logaritmica.
 */
public class ComparisonChart extends Pane {

    private static final int NUM_VERSIONS = 4;
    private static final double MARGIN_TOP = 30.0;
    private static final double MARGIN_BOTTOM = 40.0;
    private static final double MARGIN_LEFT = 60.0;
    private static final double MARGIN_RIGHT = 20.0;
    private static final double BAR_MAX_WIDTH = 120.0;
    private static final double BAR_WIDTH_FACTOR = 0.5;

    private final ResultsTable resultsTable;
    private final Label emptyLabel = new Label("Execute um lote para comparar");

    /* ***************************************************************
    * Metodo: ComparisonChart
    * Funcao: construtor; cria o painel do grafico.
    * Parametros: resultsTable = tabela de resultados (fonte de dados)
    * Retorno: nao se aplica
    *************************************************************** */
    public ComparisonChart(ResultsTable resultsTable) {
        this.resultsTable = resultsTable;
        setPrefSize(400.0, 250.0);
        setMinSize(300.0, 200.0);

        emptyLabel.setTextFill(Theme.MUDO);
        emptyLabel.setFont(Font.font(13));
        getChildren().add(emptyLabel);
        emptyLabel.setManaged(false);

        widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        heightProperty().addListener((obs, oldVal, newVal) -> redraw());
    }//fim do construtor ComparisonChart

    /* ***************************************************************
    * Metodo: redraw
    * Funcao: redesenha o grafico com os dados atuais.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void redraw() {
        getChildren().clear();

        double[] means = new double[NUM_VERSIONS];
        double[] stdDevs = new double[NUM_VERSIONS];
        boolean[] hasSamples = new boolean[NUM_VERSIONS];

        for (int v = 0; v < NUM_VERSIONS; v++) {
            List<Long> samples = getSamples(v);
            if (!samples.isEmpty()) {
                hasSamples[v] = true;
                means[v] = Statistics.mean(samples);
                stdDevs[v] = Statistics.standardDeviation(samples);
            }
        }

        double chartWidth = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        double chartHeight = getHeight() - MARGIN_TOP - MARGIN_BOTTOM;
        double barAreaWidth = chartWidth / NUM_VERSIONS;
        double barWidth = Math.min(BAR_MAX_WIDTH, barAreaWidth * BAR_WIDTH_FACTOR);

        // Determina escala
        boolean logScale = resultsTable.isLogScale();
        double maxValue = 0.0;
        for (int v = 0; v < NUM_VERSIONS; v++) {
            if (hasSamples[v]) {
                maxValue = Math.max(maxValue, means[v] + stdDevs[v]);
            }
        }
        if (maxValue <= 0) maxValue = 1.0;

        // Desenha eixo Y (linha vertical)
        Line yAxis = new Line(MARGIN_LEFT, MARGIN_TOP, MARGIN_LEFT, MARGIN_TOP + chartHeight);
        yAxis.setStroke(Color.web("#2b3544"));
        yAxis.setStrokeWidth(1.5);
        getChildren().add(yAxis);

        // Desenha grades horizontais e labels Y
        if (logScale) {
            drawLogScale(chartHeight, maxValue);
        } else {
            drawLinearScale(chartHeight, maxValue);
        }

        // Desenha barras para cada versao (sempre 4 posicoes)
        for (int v = 0; v < NUM_VERSIONS; v++) {
            double xCenter = MARGIN_LEFT + (v + 0.5) * barAreaWidth;
            double yBase = MARGIN_TOP + chartHeight;

            if (!hasSamples[v]) {
                // Versao sem dados: mostra "sem dados" em MUDO
                Text noDataLabel = new Text("sem dados");
                noDataLabel.setFont(Font.font(12));
                noDataLabel.setFill(Theme.MUDO);
                noDataLabel.setTextOrigin(VPos.CENTER);
                noDataLabel.setX(xCenter - noDataLabel.getLayoutBounds().getWidth() / 2.0);
                noDataLabel.setY(yBase - chartHeight / 2.0);
                getChildren().add(noDataLabel);
            } else {
                // Calcula altura da barra
                double barHeight;
                if (logScale) {
                    barHeight = chartHeight * (Math.log10(1.0 + means[v]) / Math.log10(1.0 + maxValue));
                } else {
                    barHeight = (means[v] / maxValue) * chartHeight;
                }
                double yTop = yBase - barHeight;

                // Barra (retangulo)
                Rectangle bar = new Rectangle(
                        xCenter - barWidth / 2.0,
                        yTop,
                        barWidth,
                        barHeight);
                bar.setFill(Theme.VERSAO[v]);
                bar.setStroke(Color.web("#0d1117"));
                bar.setStrokeWidth(1.0);
                getChildren().add(bar);

                // Bigode superior (desvio positivo) com altura minima visivel
                double whiskerTopValue = means[v] + stdDevs[v];
                double whiskerTop;
                if (logScale) {
                    whiskerTop = yBase - chartHeight * (Math.log10(1.0 + whiskerTopValue) / Math.log10(1.0 + maxValue));
                } else {
                    whiskerTop = yBase - (whiskerTopValue / maxValue) * chartHeight;
                }
                // Garante altura minima do bigode (2px)
                if (yTop - whiskerTop < 2.0) {
                    whiskerTop = yTop - 2.0;
                }
                Line upperWhisker = new Line(xCenter, yTop, xCenter, whiskerTop);
                upperWhisker.setStroke(Theme.VERSAO[v]);
                upperWhisker.setStrokeWidth(2.0);
                getChildren().add(upperWhisker);

                // Traco horizontal no topo do bigode superior
                double whiskerWidth = barWidth * 0.6;
                Line upperCap = new Line(
                        xCenter - whiskerWidth / 2.0, whiskerTop,
                        xCenter + whiskerWidth / 2.0, whiskerTop);
                upperCap.setStroke(Theme.VERSAO[v]);
                upperCap.setStrokeWidth(2.0);
                getChildren().add(upperCap);

                // Bigode inferior (desvio negativo) - so se media > desvio
                if (means[v] > stdDevs[v]) {
                    double whiskerBottomValue = means[v] - stdDevs[v];
                    double whiskerBottom;
                    if (logScale) {
                        whiskerBottom = yBase - chartHeight * (Math.log10(1.0 + whiskerBottomValue) / Math.log10(1.0 + maxValue));
                    } else {
                        whiskerBottom = yBase - (whiskerBottomValue / maxValue) * chartHeight;
                    }
                    // Garante altura minima do bigode (2px)
                    if (whiskerBottom - yBase < 2.0) {
                        whiskerBottom = yBase + 2.0;
                    }
                    Line lowerWhisker = new Line(xCenter, yBase, xCenter, whiskerBottom);
                    lowerWhisker.setStroke(Theme.VERSAO[v]);
                    lowerWhisker.setStrokeWidth(2.0);
                    getChildren().add(lowerWhisker);

                    Line lowerCap = new Line(
                            xCenter - whiskerWidth / 2.0, whiskerBottom,
                            xCenter + whiskerWidth / 2.0, whiskerBottom);
                    lowerCap.setStroke(Theme.VERSAO[v]);
                    lowerCap.setStrokeWidth(2.0);
                    getChildren().add(lowerCap);
                }

                // Valor acima da barra
                Text valueText = new Text(String.format("%.1f", means[v]));
                valueText.setFont(Font.font(12));
                valueText.setFill(Theme.TEXTO);
                valueText.setTextOrigin(VPos.BOTTOM);
                valueText.setX(xCenter - valueText.getLayoutBounds().getWidth() / 2.0);
                valueText.setY(yTop - 4.0);
                getChildren().add(valueText);
            }

            // Label da versao (V1, V2, V3, V4) no eixo X - sempre mostrado
            Text versionLabel = new Text("V" + (v + 1));
            versionLabel.setFont(Font.font(12));
            versionLabel.setFill(Theme.TEXTO);
            versionLabel.setTextOrigin(VPos.TOP);
            versionLabel.setX(xCenter - versionLabel.getLayoutBounds().getWidth() / 2.0);
            versionLabel.setY(MARGIN_TOP + chartHeight + 8.0);
            getChildren().add(versionLabel);
        }
    }//fim do metodo redraw

    /* ***************************************************************
    * Metodo: drawLogScale
    * Funcao: desenha grades e labels para escala logaritmica
    *         (marcas em 1, 10, 100, 1000...).
    * Parametros: chartHeight = altura da area do grafico; maxValue = valor maximo
    * Retorno: void
    ************************************************************* */
    private void drawLogScale(double chartHeight, double maxValue) {
        double logMax = Math.log10(1.0 + maxValue);
        // Marcas em 1, 10, 100, 1000...
        for (int exp = 0; exp <= 6; exp++) {
            double value = Math.pow(10, exp);
            if (value > maxValue * 1.5) break;
            double logVal = Math.log10(1.0 + value);
            double ratio = logVal / logMax;
            double y = MARGIN_TOP + chartHeight * (1.0 - ratio);

            Line grid = new Line(MARGIN_LEFT, y, getWidth() - MARGIN_RIGHT, y);
            grid.setStroke(Color.web("#1c2430"));
            grid.setStrokeWidth(0.5);
            grid.getStrokeDashArray().addAll(4.0, 4.0);
            getChildren().add(grid);

            Text label = new Text(String.format("%.0f", value));
            label.setFont(Font.font(10));
            label.setFill(Theme.MUDO);
            label.setTextOrigin(VPos.CENTER);
            label.setX(MARGIN_LEFT - 5.0 - label.getLayoutBounds().getWidth());
            label.setY(y);
            getChildren().add(label);
        }
    }//fim do metodo drawLogScale

    /* ***************************************************************
    * Metodo: drawLinearScale
    * Funcao: desenha grades e labels para escala linear.
    * Parametros: chartHeight = altura da area do grafico; maxValue = valor maximo
    * Retorno: void
    ************************************************************* */
    private void drawLinearScale(double chartHeight, double maxValue) {
        int numGridLines = 5;
        for (int i = 0; i <= numGridLines; i++) {
            double ratio = (double) i / numGridLines;
            double y = MARGIN_TOP + chartHeight * (1.0 - ratio);
            double value = maxValue * ratio;

            Line grid = new Line(MARGIN_LEFT, y, getWidth() - MARGIN_RIGHT, y);
            grid.setStroke(Color.web("#1c2430"));
            grid.setStrokeWidth(0.5);
            grid.getStrokeDashArray().addAll(4.0, 4.0);
            getChildren().add(grid);

            Text label = new Text(String.format("%.0f", value));
            label.setFont(Font.font(10));
            label.setFill(Theme.MUDO);
            label.setTextOrigin(VPos.CENTER);
            label.setX(MARGIN_LEFT - 5.0 - label.getLayoutBounds().getWidth());
            label.setY(y);
            getChildren().add(label);
        }
    }//fim do metodo drawLinearScale

    /* ***************************************************************
    * Metodo: getSamples
    * Funcao: obtem as amostras de uma versao da tabela de resultados.
    * Parametros: versionIndex = 0 a 3
    * Retorno: List<Long> com as amostras
    *************************************************************** */
    private List<Long> getSamples(int versionIndex) {
        return resultsTable.getSamples(versionIndex + 1);
    }//fim do metodo getSamples
}//fim da classe ComparisonChart