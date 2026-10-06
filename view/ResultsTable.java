/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: ResultsTable.java
* Funcao...........: Tabela JavaFX com 4 linhas fixas (V1 a V4) para
*                    exibir resultados por versao: ultima execucao,
*                    n, media e desvio-padrao.
*************************************************************** */

package view;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import util.Statistics;

/**
 * Tabela de resultados com 4 linhas fixas (V1 a V4).
 * Cada versao acumula uma lista de snapshots validos.
 */
public final class ResultsTable {

    private static class RowData {
        private final int version;
        private final List<Long> samples = new ArrayList<Long>();
        private long lastSnapshot = 0;

        RowData(int version) {
            this.version = version;
        }
    }

    private final RowData[] rows = new RowData[4];
    private final ObservableList<RowData> items = FXCollections.observableArrayList();
    private final TableView<RowData> table = new TableView<RowData>();

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

        table.getColumns().addAll(colVersion, colLast, colN, colMean, colStdDev);
        table.setFixedCellSize(25.0);
        table.setPrefHeight(4 * 25.0 + 30.0);
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
    }//fim do metodo addResult

    /* ***************************************************************
    * Metodo: clear
    * Funcao: limpa todas as amostras e o valor da ultima execucao.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void clear() {
        for (RowData row : rows) {
            row.samples.clear();
            row.lastSnapshot = 0;
        }
        table.refresh();
    }//fim do metodo clear

    /* ***************************************************************
    * Metodo: getNode
    * Funcao: devolve o TableView para insercao no layout.
    * Parametros: nenhum
    * Retorno: Node da tabela
    *************************************************************** */
    public Node getNode() {
        return table;
    }//fim do metodo getNode

}//fim da classe ResultsTable