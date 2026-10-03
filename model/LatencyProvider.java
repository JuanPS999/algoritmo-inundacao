/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 01/10/2026
* Ultima alteracao.: 02/10/2026
* Nome.............: LatencyProvider.java
* Funcao...........: Fornecedor de latencias de link para a simulacao.
*************************************************************** */

package model;

import java.util.Random;

/**
 * Fornece latencias de link em ms: inteiro em [1, 500], sorteado A CADA
 * transmissao em um link. O peso da aresta NAO influencia a latencia.
 * Semente opcional para reprodutibilidade.
 */
public class LatencyProvider {

    public static final int MIN_MS = 1;
    public static final int MAX_MS = 500;

    private final Random random;

    /* ***************************************************************
* Metodo: LatencyProvider
* Funcao: Construtor padrao com semente nao definida.
* Parametros: nenhum
* Retorno: void
*************************************************************** */
public LatencyProvider() {
        this.random = new Random();
    }

    /* ***************************************************************
* Metodo: LatencyProvider
* Funcao: Construtor com semente para reprodutibilidade.
* Parametros: seed - semente para o gerador randomico.
* Retorno: void
*************************************************************** */
public LatencyProvider(long seed) {
        this.random = new Random(seed);
    }

    /* ***************************************************************
* Metodo: nextLatencyMs
* Funcao: Retorna uma latencia aleatoria em milissegundos entre 1 e 500.
* Parametros: nenhum
* Retorno: int - latencia em ms.
*************************************************************** */
public int nextLatencyMs() {
        return MIN_MS + random.nextInt(MAX_MS - MIN_MS + 1);
    }
}
