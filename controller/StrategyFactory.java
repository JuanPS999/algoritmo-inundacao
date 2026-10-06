/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 06/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: StrategyFactory
* Funcao...........: Fabrica de estrategias de encaminhamento; cria
*                    a estrategia conforme o numero da versao.
*************************************************************** */
package controller;

import model.ForwardingStrategy;
import model.Strategy1AllInterfaces;
import model.Strategy2ExceptIncoming;
import model.Strategy3Ttl;
import model.Strategy4Custom;

public class StrategyFactory {

  /* ***************************************************************
  * Metodo: create
  * Funcao: cria a estrategia de encaminhamento da versao escolhida.
  * Parametros: version = versao de 1 a 4
  * Retorno: ForwardingStrategy da versao
  *************************************************************** */
  public static ForwardingStrategy create(int version) {
    switch (version) {
      case 1:
        return new Strategy1AllInterfaces();
      case 2:
        return new Strategy2ExceptIncoming();
      case 4:
        return new Strategy4Custom();
      default:
        return new Strategy3Ttl();
    }//fim do switch
  }//fim do metodo create

}//fim da classe StrategyFactory