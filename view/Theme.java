/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: Theme
* Funcao...........: Constantes de cor para o tema visual da
*                    aplicacao e CSS global.
*************************************************************** */
package view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

/**
 * Paleta de cores do tema escuro e CSS global.
 */
public final class Theme {

    private Theme() {
    }

    // Fundos
    public static final Color BG0 = Color.web("#0d1117");
    public static final Color BG1 = Color.web("#151b23");
    public static final Color BG2 = Color.web("#1c2430");

    // Bordas e texto
    public static final Color BORDA = Color.web("#2b3544");
    public static final Color TEXTO = Color.web("#e6edf3");
    public static final Color MUDO = Color.web("#8b98a9");

    // Acentos e estados
    public static final Color ACENTO = Color.web("#4c9aff");
    public static final Color TRANSMISSOR = Color.web("#3fb950");
    public static final Color RECEPTOR = Color.web("#22d3ee");
    public static final Color LINK = Color.web("#3a4658");
    public static final Color ERRO = Color.web("#f85149");

    // Cores por versao (V1 a V4)
    public static final Color[] VERSAO = {
        Color.web("#f0883e"), // V1
        Color.web("#a371f7"), // V2
        Color.web("#56d4dd"), // V3
        Color.web("#e3b341")  // V4
    };

    // CSS global como constante String (linhas <= 80 colunas)
    public static final String CSS =
        ".root { " +
        "  -fx-base: #151b23; " +
        "  -fx-control-inner-background: #1c2430; " +
        "  -fx-accent: #4c9aff; " +
        "  -fx-focus-color: #4c9aff; " +
        "  -fx-faint-focus-color: transparent; " +
        "  -fx-text-base-color: #e6edf3; " +
        "  -fx-font-size: 14px; " +
        "}" +
        ".primario { " +
        "  -fx-background-color: #4c9aff; " +
        "  -fx-text-fill: #0d1117; " +
        "  -fx-font-weight: bold; " +
        "}" +
        ".primario:hover { " +
        "  -fx-background-color: #5ca8ff; " +
        "}" +
        ".secao { " +
        "  -fx-background-color: #1c2430; " +
        "  -fx-border-color: #2b3544; " +
        "  -fx-border-width: 1; " +
        "  -fx-border-radius: 8; " +
        "  -fx-padding: 12; " +
        "  -fx-spacing: 12; " +
        "}" +
        ".secao-titulo { " +
        "  -fx-font-size: 12px; " +
        "  -fx-text-fill: #4c9aff; " +
        "  -fx-font-weight: bold; " +
        "}" +
        "ComboBox, Spinner, TextField, Slider { " +
        "  -fx-background-color: #1c2430; " +
        "  -fx-border-color: #2b3544; " +
        "  -fx-text-fill: #e6edf3; " +
        "}" +
        "ComboBox .list-cell { " +
        "  -fx-text-fill: #e6edf3; " +
        "  -fx-background-color: #1c2430; " +
        "}" +
        "ComboBox .list-cell:hover { " +
        "  -fx-background-color: #2b3544; " +
        "}" +
        "TableView { " +
        "  -fx-background-color: #1c2430; " +
        "  -fx-border-color: #2b3544; " +
        "  -fx-table-cell-border-color: #2b3544; " +
        "}" +
        "TableView .column-header { " +
        "  -fx-background-color: #151b23; " +
        "  -fx-text-fill: #e6edf3; " +
        "  -fx-font-weight: bold; " +
        "  -fx-border-color: transparent transparent #2b3544 transparent; " +
        "}" +
        "TableView .table-row-cell { " +
        "  -fx-background-color: #1c2430; " +
        "  -fx-text-fill: #e6edf3; " +
        "  -fx-border-color: transparent transparent #2b3544 transparent; " +
        "}" +
        "TableView .table-row-cell:selected { " +
        "  -fx-background-color: #4c9aff; " +
        "  -fx-text-fill: #0d1117; " +
        "}" +
        "ListView { " +
        "  -fx-background-color: #1c2430; " +
        "  -fx-border-color: #2b3544; " +
        "  -fx-text-fill: #e6edf3; " +
        "}" +
        "TabPane { " +
        "  -fx-background-color: #0d1117; " +
        "  -fx-border-color: #2b3544; " +
        "}" +
        "TabPane .tab-header-area { " +
        "  -fx-background-color: #151b23; " +
        "  -fx-border-color: transparent transparent #2b3544 transparent; " +
        "}" +
        "TabPane .tab { " +
        "  -fx-background-color: transparent; " +
        "  -fx-text-fill: #8b98a9; " +
        "  -fx-border-color: transparent; " +
        "  -fx-padding: 6 12 6 12; " +
        "}" +
        "TabPane .tab:selected { " +
        "  -fx-background-color: transparent; " +
        "  -fx-text-fill: #e6edf3; " +
        "  -fx-border-color: transparent transparent #4c9aff transparent; " +
        "  -fx-border-width: 0 0 2 0; " +
        "}" +
        "MenuBar { " +
        "  -fx-background-color: #151b23; " +
        "  -fx-text-fill: #e6edf3; " +
        "}" +
        "MenuBar .menu-button { " +
        "  -fx-text-fill: #e6edf3; " +
        "}" +
        "MenuBar .menu-button:hover { " +
        "  -fx-background-color: #2b3544; " +
        "}" +
        "Menu .menu-item { " +
        "  -fx-text-fill: #e6edf3; " +
        "}" +
        "Menu .menu-item:hover { " +
        "  -fx-background-color: #4c9aff; " +
        "}" +
        "ProgressBar { " +
        "  -fx-background-color: #1c2430; " +
        "  -fx-border-color: #2b3544; " +
        "  -fx-accent: #4c9aff; " +
        "}" +
        /* Slider styling */
        ".slider .track { " +
        "  -fx-background-color: #2b3544; " +
        "  -fx-background-radius: 2; " +
        "  -fx-pref-height: 4; " +
        "}" +
        ".slider .thumb { " +
        "  -fx-background-color: #4c9aff; " +
        "  -fx-background-radius: 8; " +
        "  -fx-pref-width: 14; " +
        "  -fx-pref-height: 14; " +
        "}" +
        /* Contador overlay card */
        ".contador-overlay { " +
        "  -fx-background-color: rgba(28, 36, 48, 0.95); " +
        "  -fx-border-color: #2b3544; " +
        "  -fx-border-width: 1; " +
        "  -fx-border-radius: 8; " +
        "  -fx-background-radius: 8; " +
        "  -fx-padding: 12; " +
        "  -fx-spacing: 4; " +
        "}" +
        ".contador-rotulo { " +
        "  -fx-font-size: 12px; " +
        "  -fx-text-fill: #8b98a9; " +
        "}" +
        ".contador-valor { " +
        "  -fx-font-size: 28px; " +
        "  -fx-text-fill: #e6edf3; " +
        "  -fx-font-weight: bold; " +
        "}" +
        ".contador-snapshot { " +
        "  -fx-font-size: 16px; " +
        "  -fx-text-fill: #4c9aff; " +
        "  -fx-font-weight: bold; " +
        "  -fx-padding: 8 0 0 0; " +
        "}" +
        ".contador-finalizada { " +
        "  -fx-font-size: 12px; " +
        "  -fx-text-fill: #f85149; " +
        "  -fx-padding: 8 0 0 0; " +
        "}";

    /* ***************************************************************
    * Metodo: apply
    * Funcao: aplica o CSS global a uma Scene gravando em arquivo
    *         temporario e adicionando aos stylesheets.
    * Parametros: scene = cena JavaFX
    * Retorno: void
    *************************************************************** */
    public static void apply(Scene scene) {
        try {
            File cssFile = File.createTempFile("tema", ".css");
            cssFile.deleteOnExit();
            try (FileWriter writer = new FileWriter(cssFile)) {
                writer.write(CSS);
            }
            scene.getStylesheets().add(cssFile.toURI().toString());
        } catch (IOException e) {
            System.err.println("Erro ao aplicar tema: " + e.getMessage());
        }
    }//fim do metodo apply
}//fim da classe Theme