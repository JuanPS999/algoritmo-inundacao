/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: ResultsTable.java
* Funcao...........: Tabela JavaFX com 4 linhas fixas (V1 a V4) para
*                    exibir resultados por versao: ultima execucao,
*                    n, media, desvio-padrao, min, max,
*                    inalcancaveis, abortadas.
*************************************************************** */

package view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import util.Statistics;

/**
 * Tabela de resultados com 4 linhas fixas (V1 a V4).
 * Cada versao acumula uma lista de snapshots validos e contadores
 * de falhas (inalcancaveis e abortadas).
 */
public final class ResultsTable {

    private static class RowData {
        private final int version;
        private final List<Long> samples = new ArrayList<Long>();
        private long lastSnapshot = 0;
        private int unreachable = 0;
        private int aborted = 0;

        RowData(int version) {
            this.version = version;
        }
    }

    // Callback para notificar mudancas (ex.: redesenhar grafico)
    private Runnable onChangeCallback;
    private boolean logScale = true;

    private final RowData[] rows = new RowData[4];
    private final ObservableList<RowData> items = FXCollections.observableArrayList();
    private final TableView<RowData> table = new TableView<RowData>();
    private final Button clearButton = new Button("LIMPAR");

    /* ***************************************************************
    * Metodo: ResultsTable
    * Funcao: construtor; cria a tabela com colunas e 4 linhas fixas.
    * Parametros: nenhum
    * Retorno: nao se aplica
    *************************************************************** */
    public ResultsTable() {
        for (int i = 0; i < 4; i++) {
            rows[i] = new RowData(i + 1);
            items.add(rows[i]);
        }

        table.setItems(items);
        // Politica de redimensionamento restrita: colunas nao encolhem alem do minimo,
        // sem barra de rolagem horizontal.
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Coluna V (versao) - largura 38
        TableColumn<RowData, String> colVersion = new TableColumn<RowData, String>("");
        colVersion.setCellValueFactory(cell -> new SimpleStringProperty("V" + cell.getValue().version));
        colVersion.setCellFactory(col -> new TableCellMonospace());
        colVersion.setPrefWidth(38);
        colVersion.setMinWidth(38);
        colVersion.setMaxWidth(38);
        colVersion.setResizable(false);
        Label hdrVersion = new Label("V");
        hdrVersion.setTooltip(new Tooltip("VERSAO"));
        colVersion.setGraphic(hdrVersion);

        // Coluna ULT (ultima execucao) - largura 56
        TableColumn<RowData, Long> colLast = new TableColumn<RowData, Long>("");
        colLast.setCellValueFactory(cell -> new SimpleLongProperty(cell.getValue().lastSnapshot).asObject());
        colLast.setCellFactory(col -> new TableCellMonospace());
        colLast.setPrefWidth(56);
        colLast.setMinWidth(56);
        colLast.setMaxWidth(56);
        colLast.setResizable(false);
        Label hdrLast = new Label("ULT");
        hdrLast.setTooltip(new Tooltip("ULTIMA EXECUCAO"));
        colLast.setGraphic(hdrLast);

        // Coluna N (quantidade de execucoes validas) - largura 40
        TableColumn<RowData, Integer> colN = new TableColumn<RowData, Integer>("");
        colN.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().samples.size()).asObject());
        colN.setCellFactory(col -> new TableCellMonospace());
        colN.setPrefWidth(40);
        colN.setMinWidth(40);
        colN.setMaxWidth(40);
        colN.setResizable(false);
        Label hdrN = new Label("N");
        hdrN.setTooltip(new Tooltip("QUANTIDADE DE EXECUCOES VALIDAS"));
        colN.setGraphic(hdrN);

        // Coluna MEDIA - largura 64
        TableColumn<RowData, Double> colMean = new TableColumn<RowData, Double>("");
        colMean.setCellValueFactory(cell -> {
            double m = Statistics.mean(cell.getValue().samples);
            return new ReadOnlyObjectWrapper<Double>(m);
        });
        colMean.setCellFactory(col -> new TableCellMonospace());
        colMean.setPrefWidth(64);
        colMean.setMinWidth(64);
        colMean.setMaxWidth(64);
        colMean.setResizable(false);
        Label hdrMean = new Label("MEDIA");
        hdrMean.setTooltip(new Tooltip("MEDIA DOS SNAPSHOTS"));
        colMean.setGraphic(hdrMean);

        // Coluna DESV (desvio padrao) - largura 60
        TableColumn<RowData, Double> colStdDev = new TableColumn<RowData, Double>("");
        colStdDev.setCellValueFactory(cell -> {
            double s = Statistics.standardDeviation(cell.getValue().samples);
            return new ReadOnlyObjectWrapper<Double>(s);
        });
        colStdDev.setCellFactory(col -> new TableCellMonospace());
        colStdDev.setPrefWidth(60);
        colStdDev.setMinWidth(60);
        colStdDev.setMaxWidth(60);
        colStdDev.setResizable(false);
        Label hdrStdDev = new Label("DESV");
        hdrStdDev.setTooltip(new Tooltip("DESVIO PADRAO"));
        colStdDev.setGraphic(hdrStdDev);

        // Coluna MIN - largura 50
        TableColumn<RowData, Long> colMin = new TableColumn<RowData, Long>("");
        colMin.setCellValueFactory(cell -> {
            List<Long> s = cell.getValue().samples;
            long v = (s.isEmpty()) ? 0 : Collections.min(s);
            return new SimpleLongProperty(v).asObject();
        });
        colMin.setCellFactory(col -> new TableCellMonospace());
        colMin.setPrefWidth(50);
        colMin.setMinWidth(50);
        colMin.setMaxWidth(50);
        colMin.setResizable(false);
        Label hdrMin = new Label("MIN");
        hdrMin.setTooltip(new Tooltip("VALOR MINIMO"));
        colMin.setGraphic(hdrMin);

        // Coluna MAX - largura 50
        TableColumn<RowData, Long> colMax = new TableColumn<RowData, Long>("");
        colMax.setCellValueFactory(cell -> {
            List<Long> s = cell.getValue().samples;
            long v = (s.isEmpty()) ? 0 : Collections.max(s);
            return new SimpleLongProperty(v).asObject();
        });
        colMax.setCellFactory(col -> new TableCellMonospace());
        colMax.setPrefWidth(50);
        colMax.setMinWidth(50);
        colMax.setMaxWidth(50);
        colMax.setResizable(false);
        Label hdrMax = new Label("MAX");
        hdrMax.setTooltip(new Tooltip("VALOR MAXIMO"));
        colMax.setGraphic(hdrMax);

        // Coluna INAL (inalcancaveis) - largura 56
        TableColumn<RowData, Integer> colUnreachable = new TableColumn<RowData, Integer>("");
        colUnreachable.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().unreachable).asObject());
        colUnreachable.setCellFactory(col -> new TableCellMonospace());
        colUnreachable.setPrefWidth(56);
        colUnreachable.setMinWidth(56);
        colUnreachable.setMaxWidth(56);
        colUnreachable.setResizable(false);
        Label hdrUnreachable = new Label("INAL");
        hdrUnreachable.setTooltip(new Tooltip("INALCANCAVEIS"));
        colUnreachable.setGraphic(hdrUnreachable);

        // Coluna ABOR (abortadas) - largura 56
        TableColumn<RowData, Integer> colAborted = new TableColumn<RowData, Integer>("");
        colAborted.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().aborted).asObject());
        colAborted.setCellFactory(col -> new TableCellMonospace());
        colAborted.setPrefWidth(56);
        colAborted.setMinWidth(56);
        colAborted.setMaxWidth(56);
        colAborted.setResizable(false);
        Label hdrAborted = new Label("ABOR");
        hdrAborted.setTooltip(new Tooltip("ABORTADAS"));
        colAborted.setGraphic(hdrAborted);

        table.getColumns().addAll(colVersion, colLast, colN, colMean, colStdDev, colMin, colMax, colUnreachable, colAborted);

        // Altura fixa de linha: 28px
        table.setFixedCellSize(28.0);
        // Altura total = cabecalho (28) + 4 linhas * 28 = 140px
        // Sem linhas vazias, sem barra vertical.
        double tableHeight = 28.0 + 4 * 28.0;
        table.setPrefHeight(tableHeight);
        table.setMinHeight(tableHeight);
        table.setMaxHeight(tableHeight);

        // Zebra striping via row factory
        table.setRowFactory(tv -> new TableRow<RowData>() {
            @Override
            protected void updateItem(RowData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    int idx = getIndex();
                    String baseColor = (idx % 2 == 0) ? Theme.PANEL : Theme.HEADER;
                    if (isSelected()) {
                        setStyle("-fx-background-color: " + Theme.ORANGE_FILL + "; -fx-text-fill: " + Theme.BG + ";");
                    } else {
                        setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: " + Theme.TEXT + ";");
                    }
                }
            }
        });

        // Header style
        table.getStylesheets().clear();
        Theme.styleTableView(table);

        // Aplicar estilo aos cabecalhos das colunas
        for (TableColumn<RowData, ?> col : table.getColumns()) {
            Theme.styleTableColumnHeader(col);
        }

        // Estilizar scrollbars quando a cena estiver disponivel
        table.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Theme.styleScrollBars(table);
            }
        });

        Theme.styleFlatButtonOutlined(clearButton);
        clearButton.setOnAction(e -> clear());
    }//fim do construtor ResultsTable

    /**
     * Celula monoespacada com texto amarelo para valores numericos.
     * Respeita selecao de linha (fundo laranja, texto escuro).
     * Fonte 13px.
     */
    private static class TableCellMonospace<S, T> extends javafx.scene.control.TableCell<S, T> {
        private static final javafx.scene.text.Font FONT_13 = Theme.monoFont(13, false);

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("");
            } else {
                setFont(FONT_13);
                setText(item.toString());
                // Cor do texto: amarelo para normal, fundo escuro para selecao
                TableRow<S> row = getTableRow();
                if (row != null && row.isSelected()) {
                    setTextFill(javafx.scene.paint.Color.web(Theme.BG));
                } else {
                    setTextFill(javafx.scene.paint.Color.web(Theme.YELLOW));
                }
            }
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            // Atualiza cor do texto quando selecao muda
            if (!isEmpty() && getItem() != null) {
                if (selected) {
                    setTextFill(javafx.scene.paint.Color.web(Theme.BG));
                } else {
                    setTextFill(javafx.scene.paint.Color.web(Theme.YELLOW));
                }
            }
        }
    }

    /* ***************************************************************
    * Metodo: addResult
    * Funcao: adiciona um snapshot a versao indicada e recalcula.
    * Parametros: versao = 1 a 4; snapshot = contador de pacotes gerados
    * Retorno: void
    *************************************************************** */
    public void addResult(int version, long snapshot) {
        if (version < 1 || version > 4) {
            return;
        }
        RowData row = rows[version - 1];
        row.samples.add(Long.valueOf(snapshot));
        row.lastSnapshot = snapshot;
        table.refresh();
        if (onChangeCallback != null) onChangeCallback.run();
    }//fim do metodo addResult

    /* ***************************************************************
    * Metodo: addFailure
    * Funcao: incrementa contador de falha (inalcancavel ou abortada).
    *         Falhas nao entram na media/desvio/min/max.
    * Parametros: versao = 1 a 4; tipo = "INALCANCAVEL" ou "ABORTADA"
    * Retorno: void
    *************************************************************** */
    public void addFailure(int version, String tipo) {
        if (version < 1 || version > 4) {
            return;
        }
        RowData row = rows[version - 1];
        if ("INALCANCAVEL".equals(tipo)) {
            row.unreachable++;
        } else if ("ABORTADA".equals(tipo)) {
            row.aborted++;
        }
        table.refresh();
        if (onChangeCallback != null) onChangeCallback.run();
    }//fim do metodo addFailure

    /* ***************************************************************
    * Metodo: clear
    * Funcao: limpa todas as amostras e contadores.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void clear() {
        for (RowData row : rows) {
            row.samples.clear();
            row.lastSnapshot = 0;
            row.unreachable = 0;
            row.aborted = 0;
        }
        table.refresh();
        if (onChangeCallback != null) onChangeCallback.run();
    }//fim do metodo clear

    /* ***************************************************************
    * Metodo: setOnChange
    * Funcao: define callback chamado quando os dados mudam.
    * Parametros: callback = Runnable a executar
    * Retorno: void
    *************************************************************** */
    public void setOnChange(Runnable callback) {
        this.onChangeCallback = callback;
    }//fim do metodo setOnChange

    /* ***************************************************************
    * Metodo: getNode
    * Funcao: devolve o TableView para insercao no layout.
    * Parametros: nenhum
    * Retorno: Node da tabela
    *************************************************************** */
    public Node getNode() {
        return table;
    }//fim do metodo getNode

    /* ***************************************************************
    * Metodo: getClearButton
    * Funcao: devolve o botao Limpar para uso externo.
    * Parametros: nenhum
    * Retorno: Button
    *************************************************************** */
    public Button getClearButton() {
        return clearButton;
    }//fim do metodo getClearButton

    /* ***************************************************************
    * Metodo: setLogScale
    * Funcao: define se a escala logaritmica esta ativa (para grafico).
    * Parametros: logScale - true para escala log
    * Retorno: void
    *************************************************************** */
    public void setLogScale(boolean logScale) {
        this.logScale = logScale;
    }//fim do metodo setLogScale

    /* ***************************************************************
    * Metodo: isLogScale
    * Funcao: retorna se a escala logaritmica esta ativa.
    * Parametros: nenhum
    * Retorno: boolean
    *************************************************************** */
    public boolean isLogScale() {
        return logScale;
    }//fim do metodo isLogScale

    /* ***************************************************************
    * Metodo: getSamples
    * Funcao: devolve as amostras de uma versao (para o grafico).
    * Parametros: version = 1 a 4
    * Retorno: List<Long> imutavel
    *************************************************************** */
    public List<Long> getSamples(int version) {
        if (version < 1 || version > 4) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(rows[version - 1].samples);
    }//fim do metodo getSamples

}//fim da classe ResultsTable