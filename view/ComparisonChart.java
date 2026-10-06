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
 */
public class ComparisonChart extends Pane {

    private static final int NUM_VERSIONS = 4;
    private static final double MARGIN_TOP = 30.0;
    private static final double MARGIN_BOTTOM = 30.0;
    private static final double MARGIN_LEFT = 60.0;
    private static final double MARGIN_RIGHT = 20.0;
    private static final double BAR_MAX_WIDTH = 60.0;
    private static final double GAP_FACTOR = 0.3;

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

        emptyLabel.setTextFill(Color.web("#8b98a9"));
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

        // Verifica se ha dados validos
        boolean hasData = false;
        double[] means = new double[NUM_VERSIONS];
        double[] stdDevs = new double[NUM_VERSIONS];
        boolean[] hasSamples = new boolean[NUM_VERSIONS];

        for (int v = 0; v < NUM_VERSIONS; v++) {
            List<Long> samples = getSamples(v);
            if (!samples.isEmpty()) {
                hasData = true;
                hasSamples[v] = true;
                means[v] = Statistics.mean(samples);
                stdDevs[v] = Statistics.standardDeviation(samples);
            }
        }

        if (!hasData) {
            // Mostra mensagem de vazio
            emptyLabel.setLayoutX((getWidth() - emptyLabel.prefWidth(-1)) / 2.0);
            emptyLabel.setLayoutY((getHeight() - emptyLabel.prefHeight(-1)) / 2.0);
            getChildren().add(emptyLabel);
            return;
        }

        // Calcula escala: maior (media + desvio)
        double maxValue = 0.0;
        for (int v = 0; v < NUM_VERSIONS; v++) {
            if (hasSamples[v]) {
                maxValue = Math.max(maxValue, means[v] + stdDevs[v]);
            }
        }
        if (maxValue <= 0) maxValue = 1.0;

        double chartWidth = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        double chartHeight = getHeight() - MARGIN_TOP - MARGIN_BOTTOM;
        double barAreaWidth = chartWidth / NUM_VERSIONS;
        double barWidth = Math.min(BAR_MAX_WIDTH, barAreaWidth * (1.0 - GAP_FACTOR));
        double gap = barAreaWidth - barWidth;

        // Desenha eixo Y (linha vertical)
        Line yAxis = new Line(MARGIN_LEFT, MARGIN_TOP, MARGIN_LEFT, MARGIN_TOP + chartHeight);
        yAxis.setStroke(Color.web("#2b3544"));
        yAxis.setStrokeWidth(1.5);
        getChildren().add(yAxis);

        // Desenha grades horizontais e labels Y
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
            label.setFill(Color.web("#8b98a9"));
            label.setTextOrigin(VPos.CENTER);
            label.setX(MARGIN_LEFT - 5.0 - label.getLayoutBounds().getWidth());
            label.setY(y);
            getChildren().add(label);
        }

        // Desenha barras para cada versao
        for (int v = 0; v < NUM_VERSIONS; v++) {
            if (!hasSamples[v]) continue;

            double xCenter = MARGIN_LEFT + gap / 2.0 + v * barAreaWidth + barWidth / 2.0;
            double barHeight = (means[v] / maxValue) * chartHeight;
            double yBase = MARGIN_TOP + chartHeight;
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

            // Bigode superior (desvio positivo)
            double whiskerTop = yBase - ((means[v] + stdDevs[v]) / maxValue) * chartHeight;
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
                double whiskerBottom = yBase - ((means[v] - stdDevs[v]) / maxValue) * chartHeight;
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
            valueText.setFont(Font.font(11));
            valueText.setFill(Color.web("#e6edf3"));
            valueText.setTextOrigin(VPos.BOTTOM);
            valueText.setX(xCenter - valueText.getLayoutBounds().getWidth() / 2.0);
            valueText.setY(yTop - 4.0);
            getChildren().add(valueText);

            // Label da versao (V1, V2, V3, V4) no eixo X
            Text versionLabel = new Text("V" + (v + 1));
            versionLabel.setFont(Font.font(12));
            versionLabel.setFill(Color.web("#e6edf3"));
            versionLabel.setTextOrigin(VPos.TOP);
            versionLabel.setX(xCenter - versionLabel.getLayoutBounds().getWidth() / 2.0);
            versionLabel.setY(MARGIN_TOP + chartHeight + 8.0);
            getChildren().add(versionLabel);
        }
    }//fim do metodo redraw

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