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
        alert.setHeaderText("Roteamento por Inundacao - Redes II (UESB)");
        alert.setContentText(
            "Autor: Juan Prado Da Silva | Matricula: 202310291\n" +
            "V1: Envia por todas as interfaces.\n" +
            "V2: Envia por todas menos a de chegada.\n" +
            "V3: V2 + verifica e decrementa TTL.\n" +
            "V4: V3 + deduplica por janela de tempo por no."
        );
        alert.showAndWait();
    }//fim do metodo show

}//fim da classe AboutDialog