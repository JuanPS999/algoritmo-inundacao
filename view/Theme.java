/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 07/10/2026
* Ultima alteracao.: 07/10/2026
* Nome.............: Theme
* Funcao...........: Constantes de cor e helpers de estilo (Gruvbox escuro)
*************************************************************** */
package view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Tema visual: paleta Gruvbox escuro, fonte Monospaced, estilo brutalista.
 * Sem CSS externo: uso de setStyle direto.
 */
public final class Theme {

    private Theme() {
    }

    // ==================== PALETA GRUVBOX ESCURO (Strings para setStyle) ====================

    /** Fundo principal: #1d2021 */
    public static final String BG = "#1d2021";

    /** Painel: #282828 */
    public static final String PANEL = "#282828";

    /** Cabecalhos / zebra: #3c3836 */
    public static final String HEADER = "#3c3836";

    /** Bordas: #504945 */
    public static final String BORDER = "#504945";

    /** Inativo / desabilitado: #665c54 */
    public static final String INACTIVE = "#665c54";

    /** Texto principal: #ebdbb2 */
    public static final String TEXT = "#ebdbb2";

    /** Texto secundario: #a89984 */
    public static final String TEXT_SECONDARY = "#a89984";

    /** Amarelo sujo (valores): #d79921 */
    public static final String YELLOW = "#d79921";

    /** Laranja queimado (preenchimentos): #d65d0e */
    public static final String ORANGE_FILL = "#d65d0e";

    /** Laranja queimado claro (texto/linhas finas - UNICO destaque): #fe8019 */
    public static final String ORANGE_ACCENT = "#fe8019";

    /** Vermelho (SOMENTE para erros): #fb4934 */
    public static final String RED = "#fb4934";

    /** Transmissor (TX): #98971a */
    public static final String TX_COLOR = "#98971a";

    /** Receptor (RX): #458588 */
    public static final String RX_COLOR = "#458588";

    /** Pacote: #83a598 (azul Gruvbox) */
    public static final String PACKET_COLOR = "#83a598";

    /** Grade de fundo: #2d2c2b */
    public static final String GRID = "#2d2c2b";

    // ==================== COMO OBJETOS Color (PARA GraphPane) ====================

    public static final Color BG_COLOR = Color.web(BG);
    public static final Color PANEL_COLOR = Color.web(PANEL);
    public static final Color HEADER_COLOR = Color.web(HEADER);
    public static final Color BORDER_COLOR = Color.web(BORDER);
    public static final Color INACTIVE_COLOR = Color.web(INACTIVE);
    public static final Color TEXT_COLOR = Color.web(TEXT);
    public static final Color TEXT_SECONDARY_COLOR = Color.web(TEXT_SECONDARY);
    public static final Color YELLOW_COLOR = Color.web(YELLOW);
    public static final Color ORANGE_FILL_COLOR = Color.web(ORANGE_FILL);
    public static final Color ORANGE_ACCENT_COLOR = Color.web(ORANGE_ACCENT);
    public static final Color RED_COLOR = Color.web(RED);
    public static final Color TX_COLOR_OBJ = Color.web(TX_COLOR);
    public static final Color RX_COLOR_OBJ = Color.web(RX_COLOR);

    public static final Color PACKET_COLOR_OBJ = Color.web(PACKET_COLOR);

    public static final Color GRID_COLOR = Color.web(GRID);

    // ==================== ALIASES Color PARA COMPATIBILIDADE (GraphPane) ====================

    /** @deprecated use BG_COLOR */
    public static final Color BG0 = BG_COLOR;

    /** @deprecated use TEXT_COLOR */
    public static final Color TEXTO = TEXT_COLOR;

    /** @deprecated use RED_COLOR */
    public static final Color ERRO = RED_COLOR;

    /** @deprecated use INACTIVE_COLOR */
    public static final Color MUDO = INACTIVE_COLOR;

    /** @deprecated use PANEL_COLOR */
    public static final Color BG2 = PANEL_COLOR;

    /** @deprecated use ORANGE_ACCENT_COLOR */
    public static final Color ACENTO = ORANGE_ACCENT_COLOR;

    /** @deprecated use BORDER_COLOR */
    public static final Color BORDA = BORDER_COLOR;

    /** @deprecated use TX_COLOR_OBJ */
    public static final Color TRANSMISSOR = TX_COLOR_OBJ;

    /** @deprecated use RX_COLOR_OBJ */
    public static final Color RECEPTOR = RX_COLOR_OBJ;

    // ==================== FONTE MONOESPACADA ====================

    /**
     * Fonte Monospaced no tamanho e peso indicados.
     * @param size tamanho em pontos (>= 12)
     * @param bold true para negrito
     * @return Font configurada
     */
    public static Font monoFont(double size, boolean bold) {
        return Font.font("Monospaced", bold ? FontWeight.BOLD : FontWeight.NORMAL, size);
    }

    /** Fonte padrao 12px regular. */
    public static final Font FONT_12 = monoFont(12, false);

    /** Fonte padrao 12px negrito. */
    public static final Font FONT_12_BOLD = monoFont(12, true);

    /** Fonte 14px negrito para titulos. */
    public static final Font FONT_14_BOLD = monoFont(14, true);

    // ==================== HELPERS DE ESTILO ====================

    /**
     * Aplica estilo de painel (fundo, borda, padding).
     * @param region regiao a estilizar
     */
    public static void stylePanel(Region region) {
        region.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 12;"
        );
    }

    /**
     * Aplica estilo de titulo de secao (CAIXA ALTA, cor secundaria, fonte 14 bold).
     * @param label label a estilizar
     */
    public static void styleSectionTitle(Label label) {
        label.setFont(FONT_14_BOLD);
        label.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");
        label.setText(label.getText().toUpperCase());
    }

    /**
     * Aplica estilo de botao plano PREENCHIDO (fundo laranja, texto escuro, borda 1px).
     * @param btn botao a estilizar
     */
    public static void styleFlatButtonFilled(Button btn) {
        btn.setFont(FONT_12_BOLD);
        btn.setStyle(
            "-fx-background-color: " + ORANGE_FILL + ";" +
            "-fx-text-fill: " + BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 6 12 6 12;" +
            "-fx-cursor: hand;"
        );
    }

    /**
     * Aplica estilo de botao plano CONTORNADO (fundo transparente, borda laranja, texto laranja).
     * @param btn botao a estilizar
     */
    public static void styleFlatButtonOutlined(Button btn) {
        btn.setFont(FONT_12_BOLD);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + ORANGE_ACCENT + ";" +
            "-fx-border-color: " + ORANGE_ACCENT + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 6 12 6 12;" +
            "-fx-cursor: hand;"
        );
    }

    /**
     * Aplica estilo de botao desabilitado (inativo).
     * @param btn botao a estilizar
     */
    public static void styleButtonDisabled(Button btn) {
        btn.setStyle(
            "-fx-background-color: " + INACTIVE + ";" +
            "-fx-text-fill: " + TEXT_SECONDARY + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 6 12 6 12;" +
            "-fx-cursor: default;"
        );
    }

    /**
     * Aplica estilo basico a ComboBox (fundo painel, borda, texto).
     * @param combo ComboBox a estilizar
     */
    public static void styleComboBox(javafx.scene.control.ComboBox<?> combo) {
        combo.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;"
        );
    }

    /**
     * Aplica estilo basico a Spinner.
     * @param spinner Spinner a estilizar
     */
    public static void styleSpinner(javafx.scene.control.Spinner<?> spinner) {
        spinner.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;"
        );
    }

    /**
     * Aplica estilo basico a Slider.
     * @param slider Slider a estilizar
     */
    public static void styleSlider(javafx.scene.control.Slider slider) {
        slider.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT + ";"
        );
    }

    /**
     * Aplica estilo basico a TextField.
     * @param field TextField a estilizar
     */
    public static void styleTextField(javafx.scene.control.TextField field) {
        field.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 4 8 4 8;"
        );
    }

    /**
     * Aplica estilo basico a CheckBox.
     * @param check CheckBox a estilizar
     */
    public static void styleCheckBox(javafx.scene.control.CheckBox check) {
        check.setStyle(
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;"
        );
    }

    /**
     * Aplica estilo ao campo SEMENTE (entrada de texto).
     * Texto digitado: #ebdbb2, placeholder: #a89984,
     * fundo: #1d2021, borda 1px #504945, fonte 13px Monospaced.
     * @param field TextField a estilizar
     */
    public static void styleSeedField(javafx.scene.control.TextField field) {
        field.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 4 8 4 8;"
        );
    }

    /**
     * Aplica estilo ao campo REPRODUCAO (somente leitura).
     * Mostra a semente em amarelo-sujo #d79921,
     * placeholder: #a89984, fundo: #1d2021, borda 1px #504945, fonte 13px Monospaced.
     * @param field TextField a estilizar
     */
    public static void styleReplayField(javafx.scene.control.TextField field) {
        field.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + YELLOW + ";" +
            "-fx-prompt-text-fill: " + TEXT_SECONDARY + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 4 8 4 8;"
        );
    }

    /**
     * Aplica estilo basico a TabPane.

    /**
     * Aplica estilo basico a TabPane.
     * @param tabPane TabPane a estilizar
     */
    public static void styleTabPane(javafx.scene.control.TabPane tabPane) {
        tabPane.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-tab-min-width: 80;" +
            "-fx-tab-max-width: 160;"
        );
    }

    /**
     * Aplica estilo a Tab individual (aba).
     * @param tab Tab a estilizar
     */
    public static void styleTab(javafx.scene.control.Tab tab) {
        tab.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 1 0 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0 0 0 0;" +
            "-fx-padding: 6 16 6 16;" +
            "-fx-text-fill: " + TEXT_SECONDARY + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
    }

    /**
     * Aplica estilo a MenuBar.
     * @param menuBar MenuBar a estilizar
     */
    public static void styleMenuBar(javafx.scene.control.MenuBar menuBar) {
        menuBar.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;"
        );
    }

    /**
     * Aplica estilo a Menu.
     * @param menu Menu a estilizar
     */
    public static void styleMenu(javafx.scene.control.Menu menu) {
        menu.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 6 16 6 16;"
        );
    }

    /**
     * Aplica estilo a MenuItem.
     * @param item MenuItem a estilizar
     */
    public static void styleMenuItem(javafx.scene.control.MenuItem item) {
        item.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;" +
            "-fx-padding: 6 16 6 16;"
        );
    }

    /**
     * Aplica estilo completo a TableView (ResultsTable).
     * @param table TableView a estilizar
     */
    public static void styleTableView(javafx.scene.control.TableView<?> table) {
        table.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + TEXT + ";" +
            "-fx-table-cell-border-color: " + BORDER + ";" +
            "-fx-table-header-border-color: " + BORDER + ";"
        );
    }

    /**
     * Aplica estilo a coluna de TableView (cabecalho).
     * @param column TableColumn a estilizar
     */
    public static void styleTableColumnHeader(javafx.scene.control.TableColumn<?, ?> column) {
        column.setStyle(
            "-fx-background-color: " + HEADER + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 1 1 0;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-text-fill: " + TEXT_SECONDARY + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-alignment: CENTER_LEFT;" +
            "-fx-padding: 4 8 4 8;"
        );
    }

    /**
     * Aplica estilo a ListView (EventLogPane).
     * @param listView ListView a estilizar
     */
    public static void styleListView(javafx.scene.control.ListView<?> listView) {
        listView.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-control-inner-background: " + BG + ";" +
            "-fx-font-family: Monospaced;" +
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + TEXT + ";"
        );
    }

    /**
     * Aplica estilo a SplitPane divisores.
     * @param splitPane SplitPane a estilizar
     */
    public static void styleSplitPane(javafx.scene.control.SplitPane splitPane) {
        splitPane.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0;" +
            "-fx-background-radius: 0;"
        );
    }

    /**
     * Aplica estilo a ToggleButton para abas do painel de controle.
     * @param btn ToggleButton a estilizar
     * @param selected true se deve aplicar estilo de selecionado
     */
    public static void styleToggleButton(javafx.scene.control.ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle(
                "-fx-background-color: " + ORANGE_FILL + ";" +
                "-fx-text-fill: " + BG + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 0 1 0 1;" +
                "-fx-border-radius: 0;" +
                "-fx-background-radius: 0;" +
                "-fx-padding: 6 16 6 16;" +
                "-fx-font-family: Monospaced;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: " + HEADER + ";" +
                "-fx-text-fill: " + TEXT + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-width: 0 1 0 1;" +
                "-fx-border-radius: 0;" +
                "-fx-background-radius: 0;" +
                "-fx-padding: 6 16 6 16;" +
                "-fx-font-family: Monospaced;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-cursor: hand;"
            );
        }
    }

    /**
     * Aplica estilo a ScrollBar (vertical/horizontal) para tema escuro.
     * Deve ser aplicado apos o controle ser adicionado a cena (ex: via lookup).
     * @param control Controle pai que contem a ScrollBar
     */
    public static void styleScrollBars(javafx.scene.control.Control control) {
        String scrollBarStyle =
            "-fx-background-color: " + BG + ";" +
            "-fx-background-radius: 0;";
        String thumbStyle =
            "-fx-background-color: " + INACTIVE + ";" +
            "-fx-background-radius: 0;" +
            "-fx-background-insets: 2;";
        String trackStyle =
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 0;";
        String incrementStyle =
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 0;";
        String decrementStyle =
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 0;";

        control.applyCss();
        for (javafx.scene.Node node : control.lookupAll(".scroll-bar")) {
            if (node instanceof javafx.scene.control.ScrollBar) {
                javafx.scene.control.ScrollBar sb = (javafx.scene.control.ScrollBar) node;
                sb.setStyle(scrollBarStyle);
                javafx.scene.Node thumb = sb.lookup(".thumb");
                if (thumb != null) thumb.setStyle(thumbStyle);
                javafx.scene.Node track = sb.lookup(".track");
                if (track != null) track.setStyle(trackStyle);
                javafx.scene.Node incBtn = sb.lookup(".increment-button");
                if (incBtn != null) incBtn.setStyle(incrementStyle);
                javafx.scene.Node decBtn = sb.lookup(".decrement-button");
                if (decBtn != null) decBtn.setStyle(decrementStyle);
            }
        }
        for (javafx.scene.Node n : control.lookupAll(".scroll-bar:vertical .thumb")) {
            n.setStyle(thumbStyle);
        }
        for (javafx.scene.Node n : control.lookupAll(".scroll-bar:horizontal .thumb")) {
            n.setStyle(thumbStyle);
        }
    }

    /**
     * Aplica estilo a label de titulo da secao MEDICAO (cor secundaria, mono 12px bold, caixa alta).
     * @param label Label a estilizar
     */
    public static void styleMeasurementTitle(Label label) {
        label.setFont(Theme.FONT_12_BOLD);
        label.setStyle("-fx-text-fill: " + TEXT_SECONDARY + ";");
        label.setText(label.getText().toUpperCase());
    }

    /**
     * Aplica estilo a label de valor da secao MEDICAO (amarelo-sujo, mono 12px).
     * @param label Label a estilizar
     */
    public static void styleMeasurementValue(Label label) {
        label.setFont(Theme.FONT_12);
        label.setStyle("-fx-text-fill: " + YELLOW + ";");
    }

    /**
     * Aplica estilo a bloco destacado DESTINO ATINGIDO (borda esquerda laranja, fundo painel).
     * @param region Regiao a estilizar
     */
    public static void styleDestinoAtingido(Region region) {
        region.setStyle(
            "-fx-background-color: " + PANEL + ";" +
            "-fx-border-color: " + ORANGE_FILL + " transparent transparent transparent;" +
            "-fx-border-width: 0 0 0 3;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-padding: 8 12 8 12;"
        );
    }

    /**
     * Aplica o tema a uma Scene (fundo global).
     * @param scene Scene a estilizar
     */
    public static void apply(javafx.scene.Scene scene) {
        scene.setFill(Color.web(BG));
        String existing = scene.getRoot().getStyle();
        scene.getRoot().setStyle((existing == null || existing.isEmpty() ? "" : existing + ";") + "-fx-background-color: " + BG + ";");
    }
}//fim da classe Theme