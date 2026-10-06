/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: AboutDialog.java
* Funcao...........: Dialogo "Sobre" com informacoes do autor,
*                    disciplina e descricao das 4 versoes.
*************************************************************** */

package view;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Dialogo "Sobre" exibido no menu Ajuda.
 */
public final class AboutDialog {

    public static final String OPTION4_TEXT =
        "Opcao 4: ainda nao definida; a V4 delega a V3.";

    private AboutDialog() {
    }

    /* ***************************************************************
    * Metodo: show
    * Funcao: cria e exibe o Alert INFORMATION com as informacoes.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public static void show() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Sobre");
        alert.setHeaderText("Roteamento por Inundacao - Redes de Computadores II");
        alert.setContentText(
            "Autor: Juan Prado Da Silva\n" +
            "Matricula: 202310291\n" +
            "Disciplina: Redes de Computadores II (UESB)\n\n" +
            "Versoes implementadas:\n" +
            "V1 - Envia por todas as interfaces\n" +
            "V2 - Envia por todas menos a de chegada\n" +
            "V3 - V2 + verificacao e decremento de TTL\n" +
            "V4 - " + OPTION4_TEXT
        );
        alert.showAndWait();
    }//fim do metodo show

}//fim da classe AboutDialog