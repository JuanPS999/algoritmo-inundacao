/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
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
    private final Button clearButton = new Button("Limpar");

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
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RowData, String> colVersion = new TableColumn<RowData, String>("Versao");
        colVersion.setCellValueFactory(cell -> new SimpleStringProperty("V" + cell.getValue().version));

        TableColumn<RowData, Long> colLast = new TableColumn<RowData, Long>("Ultima");
        colLast.setCellValueFactory(cell -> new SimpleLongProperty(cell.getValue().lastSnapshot).asObject());

        TableColumn<RowData, Integer> colN = new TableColumn<RowData, Integer>("N");
        colN.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().samples.size()).asObject());

        TableColumn<RowData, Double> colMean = new TableColumn<RowData, Double>("Media");
        colMean.setCellValueFactory(cell -> {
            double m = Statistics.mean(cell.getValue().samples);
            return new ReadOnlyObjectWrapper<Double>(m);
        });

        TableColumn<RowData, Double> colStdDev = new TableColumn<RowData, Double>("Desvio");
        colStdDev.setCellValueFactory(cell -> {
            double s = Statistics.standardDeviation(cell.getValue().samples);
            return new ReadOnlyObjectWrapper<Double>(s);
        });

        TableColumn<RowData, Long> colMin = new TableColumn<RowData, Long>("Min");
        colMin.setCellValueFactory(cell -> {
            List<Long> s = cell.getValue().samples;
            long v = (s.isEmpty()) ? 0 : Collections.min(s);
            return new SimpleLongProperty(v).asObject();
        });

        TableColumn<RowData, Long> colMax = new TableColumn<RowData, Long>("Max");
        colMax.setCellValueFactory(cell -> {
            List<Long> s = cell.getValue().samples;
            long v = (s.isEmpty()) ? 0 : Collections.max(s);
            return new SimpleLongProperty(v).asObject();
        });

        TableColumn<RowData, Integer> colUnreachable = new TableColumn<RowData, Integer>("Inalcancaveis");
        colUnreachable.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().unreachable).asObject());

        TableColumn<RowData, Integer> colAborted = new TableColumn<RowData, Integer>("Abortadas");
        colAborted.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().aborted).asObject());

        table.getColumns().addAll(colVersion, colLast, colN, colMean, colStdDev, colMin, colMax, colUnreachable, colAborted);
        table.setFixedCellSize(25.0);
        table.setPrefHeight(4 * 25.0 + 30.0);

        clearButton.setOnAction(e -> clear());
    }//fim do construtor ResultsTable

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