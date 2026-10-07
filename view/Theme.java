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

    // ==================== FONTE MONOESPAÇADA ====================

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
     * Aplica estilo basico a TabPane.
     * @param tabPane TabPane a estilizar
     */
    public static void styleTabPane(javafx.scene.control.TabPane tabPane) {
        tabPane.setStyle(
            "-fx-background-color: " + BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;"
        );
    }

    /**
     * Aplica o tema a uma Scene (fundo global).
     * @param scene Scene a estilizar
     */
    public static void apply(javafx.scene.Scene scene) {
        scene.setFill(Color.web(BG));
        scene.getRoot().setStyle("-fx-background-color: " + BG + ";");
    }
}//fim da classe Theme