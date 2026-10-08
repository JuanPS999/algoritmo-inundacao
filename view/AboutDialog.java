/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: AboutDialog.java
* Funcao...........: Dialogo "Sobre" com informacoes do projeto,
*                    autor, uso, versoes e detalhes da V4.
*                    Estilo via Theme (Gruvbox escuro).
*************************************************************** */

package view;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Dialogo "Sobre" exibido no menu Ajuda.
 */
public final class AboutDialog {

    private AboutDialog() {
    }

    /* ***************************************************************
    * Metodo: show
    * Funcao: cria e exibe o Alert INFORMATION com area de texto rolavel.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public static void show() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("SOBRE");
        alert.setHeaderText(null);
        alert.setGraphic(null);

        // Conteudo em TextArea monoespacada, somente leitura, rolavel
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(Font.font("Monospaced", FontWeight.NORMAL, 12));
        textArea.setText(buildContent());

        // ScrollPane para rolagem
        ScrollPane scroll = new ScrollPane(textArea);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPrefViewportWidth(860);
        scroll.setPrefViewportHeight(600);

        VBox content = new VBox(scroll);
        content.setPadding(new Insets(16));
        content.setPrefWidth(860);
        content.setPrefHeight(660);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(900);
        alert.getDialogPane().setPrefHeight(720);
        alert.setResizable(true);

        // Aplicar estilo do Theme ao DialogPane
        javafx.scene.control.DialogPane pane = alert.getDialogPane();
        pane.setStyle(
            "-fx-background-color: " + Theme.PANEL + ";" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;"
        );

        // Header (titulo) - texto claro #ebdbb2
        Node headerPanel = pane.lookup(".header-panel");
        if (headerPanel != null) {
            headerPanel.setStyle("-fx-background-color: transparent;");
        }
        Node headerLabel = pane.lookup(".content.label");
        if (headerLabel != null) {
            headerLabel.setStyle(
                "-fx-text-fill: #ebdbb2;" +
                "-fx-font-family: Monospaced;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;"
            );
        }

        // TextArea e ScrollPane estilo escuro
        textArea.setStyle(
            "-fx-background-color: " + Theme.BG + ";" +
            "-fx-control-inner-background: " + Theme.BG + ";" +
            "-fx-text-fill: " + Theme.TEXT + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;" +
            "-fx-border-color: " + Theme.BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-highlight-fill: " + Theme.ORANGE_FILL + ";" +
            "-fx-highlight-text-fill: " + Theme.BG + ";"
        );
        scroll.setStyle(
            "-fx-background-color: " + Theme.BG + ";" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent;"
        );
        Node scrollViewport = scroll.lookup(".viewport");
        if (scrollViewport != null) {
            scrollViewport.setStyle("-fx-background-color: " + Theme.BG + ";");
        }

        // Botoes
        for (ButtonType bt : pane.getButtonTypes()) {
            Node btn = pane.lookupButton(bt);
            if (btn instanceof javafx.scene.control.Button) {
                javafx.scene.control.Button b = (javafx.scene.control.Button) btn;
                if (bt.getButtonData().isDefaultButton()) {
                    Theme.styleFlatButtonFilled(b);
                } else {
                    Theme.styleFlatButtonOutlined(b);
                }
            }
        }

        alert.showAndWait();
    }//fim do metodo show

    /* ***************************************************************
    * Metodo: buildContent
    * Funcao: monta o texto do dialogo com todas as secoes.
    * Parametros: nenhum
    * Retorno: String conteudo formatado
    *************************************************************** */
    private static String buildContent() {
        StringBuilder sb = new StringBuilder();

        sb.append("PROJETO\n");
        sb.append("  Roteamento por Inundacao - Redes de Computadores II (UESB)\n\n");

        sb.append("AUTOR\n");
        sb.append("  Juan Prado Da Silva\n");
        sb.append("  Matricula: 202310291\n\n");

        sb.append("OPCAO 4: V4 - DEDUPLICACAO TEMPORAL\n");
        sb.append("  Problema: Em flooding, o mesmo messageId chega repetidas vezes\n");
        sb.append("  ao mesmo no por caminhos diferentes, gerando retransmissoes\n");
        sb.append("  desnecessarias e tempestade de pacotes.\n");
        sb.append("  Como funciona: Cada no mantem um mapa messageId -> instanteVisto.\n");
        sb.append("  Ao receber uma copia em t, se (t - instanteVisto) < windowMs,\n");
        sb.append("  a copia e descartada como duplicata e NAO renova o registro.\n");
        sb.append("  Caso contrario, atualiza instanteVisto e aplica logica V3.\n");
        sb.append("  Parametro da janela: windowMs (padrao 1000 ms). windowMs = 0\n");
        sb.append("  equivale a V3 (sem deduplicacao).\n");
        sb.append("  Custo: Memoria O(N * M) onde N = nos, M = messageIds ativos;\n");
        sb.append("  limpeza preguicosa so durante consultas (sem timers).\n");
        sb.append("  Limitacao: Nao evita a primeira onda de duplicatas; so\n");
        sb.append("  suprime re-chegadas dentro da janela. Janela muito grande\n");
        sb.append("  pode suprimir retransmissoes legitimas; muito pequena\n");
        sb.append("  nao filtra ruido. windowMs = 0 desativa (igual a V3).\n\n");

        sb.append("COMO USAR\n");
        sb.append("  1. Escolha versao (V1 a V4), transmissor, receptor e TTL na aba CENARIO.\n");
        sb.append("  2. Clique INICIAR na aba EXECUCAO para comecar a simulacao.\n");
        sb.append("  3. Clique em um cabo no grafo para corta-lo ou restaura-lo.\n");
        sb.append("  4. Acompanhe a MEDICAO e a TABELA de resultados.\n");
        sb.append("  5. Semente: deixe ALEATORIA ou informe valor; ULTIMA guarda a semente usada.\n\n");

        sb.append("O QUE E MEDIDO\n");
        sb.append("  Pacotes gerados ate a primeira chegada ao destino (snapshot).\n\n");

        sb.append("VERSOES\n");
        sb.append("  V1: Envia por todas as interfaces; ignora TTL.\n");
        sb.append("  V2: Envia por todas menos a de chegada; ignora TTL.\n");
        sb.append("  V3: V2 + decrementa TTL antes de encaminhar; entrega no destino\n");
        sb.append("      reconhecida antes do descarte por TTL.\n");
        sb.append("  V4: V3 + deduplicacao temporal por no (janela configuravel).\n\n");

        return sb.toString();
    }

}//fim da classe AboutDialog
