/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: EventLogPane
* Funcao...........: Painel de log de eventos com ListView,
*                    rolagem automatica, botao Limpar e aviso
*                    de linhas descartadas.
*************************************************************** */
package view;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class EventLogPane extends VBox {

    private static final String TITLE = "Log de Eventos";

    private final ListView<String> listView = new ListView<String>();
    private final Button clearButton = new Button("Limpar");
    private final Label droppedLabel = new Label("");
    private int droppedCount = 0;

    /* ***************************************************************
    * Metodo: EventLogPane
    * Funcao: construtor; monta o painel de log.
    * Parametros: nenhum
    * Retorno: nao se aplica
    *************************************************************** */
    public EventLogPane() {
        super(4.0);
        setPadding(new Insets(8.0));
        setPrefHeight(200.0);
        VBox.setVgrow(this, Priority.ALWAYS);

        Label titleLabel = new Label(TITLE);
        titleLabel.setStyle("-fx-font-weight: bold;");

        listView.setPrefHeight(150.0);
        VBox.setVgrow(listView, Priority.ALWAYS);

        clearButton.setOnAction(e -> {
            listView.getItems().clear();
            droppedCount = 0;
            updateDroppedLabel();
        });

        HBox buttonBox = new HBox(8.0, clearButton, droppedLabel);
        droppedLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

        getChildren().addAll(titleLabel, listView, buttonBox);
    }//fim do construtor EventLogPane

    /* ***************************************************************
    * Metodo: addLines
    * Funcao: adiciona linhas ao log; rola para o final automaticamente.
    * Parametros: lines = linhas a adicionar
    * Retorno: void
    *************************************************************** */
    public void addLines(List<String> lines) {
        for (String line : lines) {
            listView.getItems().add(line);
        }
        // rola para o final
        listView.scrollTo(listView.getItems().size() - 1);
    }//fim do metodo addLines

    /* ***************************************************************
    * Metodo: setDroppedCount
    * Funcao: define o numero de linhas descartadas e atualiza aviso.
    * Parametros: count = numero de linhas descartadas
    * Retorno: void
    *************************************************************** */
    public void setDroppedCount(int count) {
        this.droppedCount = count;
        updateDroppedLabel();
    }//fim do metodo setDroppedCount

    /* ***************************************************************
    * Metodo: updateDroppedLabel
    * Funcao: atualiza o label de aviso de linhas descartadas.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    private void updateDroppedLabel() {
        if (droppedCount > 0) {
            droppedLabel.setText(droppedCount + " linhas antigas descartadas");
        } else {
            droppedLabel.setText("");
        }
    }//fim do metodo updateDroppedLabel

    /* ***************************************************************
    * Metodo: clear
    * Funcao: limpa o log e zera contador de descartadas.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public void clear() {
        listView.getItems().clear();
        droppedCount = 0;
        updateDroppedLabel();
    }//fim do metodo clear
}//fim da classe EventLogPane