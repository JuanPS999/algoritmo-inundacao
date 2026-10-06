/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: Theme
* Funcao...........: Constantes de cor para o tema visual da aplicacao.
*************************************************************** */
package view;

import javafx.scene.paint.Color;

/**
 * Paleta de cores do tema escuro.
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
}