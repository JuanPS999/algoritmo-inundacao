/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: EventLogPane
* Funcao...........: Painel de log de eventos com ListView,
*                    rolagem automatica, botao Limpar e aviso
*                    de linhas descartadas. Estilo telemetria
*                    monoespacada.
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

    private static final String TITLE = "LOG DE EVENTOS";

    private final ListView<String> listView = new ListView<String>();
    private final Button clearButton = new Button("LIMPAR");
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
        Theme.stylePanel(this);

        Label titleLabel = new Label(TITLE);
        Theme.styleSectionTitle(titleLabel);

        listView.setPrefHeight(150.0);
        VBox.setVgrow(listView, Priority.ALWAYS);
        listView.setStyle(
            "-fx-background-color: " + Theme.BG + ";" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 11px;" +
            "-fx-text-fill: " + Theme.TEXT + ";" +
            "-fx-control-inner-background: " + Theme.BG + ";"
        );
        // Forca celulas monoespacadas
        listView.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setFont(Theme.FONT_12);
                    setTextFill(javafx.scene.paint.Color.web(Theme.TEXT));
                    setText(item);
                }
            }
        });

        clearButton.setOnAction(e -> {
            listView.getItems().clear();
            droppedCount = 0;
            updateDroppedLabel();
        });
        Theme.styleFlatButtonOutlined(clearButton);

        HBox buttonBox = new HBox(8.0, clearButton, droppedLabel);
        droppedLabel.setFont(Theme.FONT_12);
        droppedLabel.setTextFill(javafx.scene.paint.Color.web(Theme.TEXT_SECONDARY));

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