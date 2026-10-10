# Roteamento por Inundacao - Redes de Computadores II (UESB)

Trabalho academico: implementacao de 4 versoes de roteamento por inundacao
com GUI em JavaFX e medicao de pacotes gerados ate o destino.

## Requisitos
- JDK 1.8.0_351 com JavaFX embutido (jre/lib/ext/jfxrt.jar)
- `javac -version` deve mostrar 1.8.x

## Build e Execucao

### Compilar
```bash
./build.sh
```
Compila todos os .java para a pasta `build/`.

### GUI (modo ao vivo)
```bash
./run.sh
```
Janela com grafo, controles, tabela de resultados e menu.
- ComboBox versao (1 a 4), transmissor, receptor
- Spinner TTL (versoes 3 e 4), Slider velocidade (0.25x a 10x)
- Botoes: Iniciar, Pausar, Resetar
- Spinner N (default 30), botao "Executar N vezes" (modo lote)
- Clique em aresta: alterna habilitado/desabilitado (vermelho tracejado)
- Transmissor verde, receptor azul (laranja quando atingido)
- Contador ao vivo e snapshot na primeira chegada
- Tabela V1-V4: Ultima, N, Media, Desvio

### Console (execucao unica)
```bash
java -cp build util.ConsoleRunner <versao> <origem> <destino> <ttl> <semente>
```
Exemplo:
```bash
java -cp build util.ConsoleRunner 3 1 4 3 42
```
Imprime eventos (transmissao/chegada) e snapshot final.

### Lote em linha de comando (todas versoes)
```bash
java -cp build util.BatchConsole <n> <origem> <destino> <ttl> <sementeBase> [backbone]
```
Exemplo:
```bash
java -cp build util.BatchConsole 30 1 4 3 42
```
Imprime CSV: Versao,Validas,Abortadas,Inalcancaveis,Media,DesvioPadrao

## Formato do backbone.txt
```
N;
A;B;PESO
...
```
- Primeira linha: numero de nos (pode terminar com `;`)
- Demais linhas: aresta nao direcionada (listada uma vez)
- Exemplo (grafo Tanenbaum, 8 nos):
```
8;
1;2;2
1;7;6
2;3;7
2;5;2
3;6;3
3;4;3
4;8;2
5;7;1
5;6;2
6;8;2
7;8;4
```
- Rotulos na GUI: 1->A, 2->B, ... (interno usa numericos 1..N)

## Versoes do Algoritmo

- **V1**: Cada pacote que chega e enviado por TODAS as interfaces.
  - Ignora TTL. Encaminha por todas as interfaces habilitadas, inclusive a de chegada.
- **V2**: Igual a V1, exceto pela interface de chegada.
  - Ignora TTL. Encaminha por todas as interfaces habilitadas, exceto a interface pela qual o pacote chegou.
- **V3**: Igual a V2, mais verificacao/decremento de TTL.
  - Decrementa TTL antes do encaminhamento. Copias cujo TTL chega a <= 0 sao descartadas.
  - A chegada ao destino e reconhecida antes desse descarte.
- **V4**: Baseada na V3, adiciona deduplicacao temporal por no.
  - Cada mensagem logica possui um messageId compartilhado por suas copias.
  - Cada no mantem registro do ultimo instante em que observou cada messageId.
  - Se uma nova chegada ocorre dentro da janela configurada (windowMs), ela e tratada como duplicata e descartada.
  - A duplicata nao reencaminha e nao renova o instante registrado.
  - Caso a entrada esteja expirada (janelas passadas), a observacao e aceita e o instante e atualizado.
  - windowMs padrao e 1000 ms.
  - windowMs = 0 equivale a V3 (sem deduplicacao).
  - A limpeza das entries expiradas e preguiçosa, durante consultas (nao ha timers/eventos especificos para expiracao).
  - A origem registra a mensagem no instante 0.
  - Cada execucao deve possuir uma nova instancia da Strategy4Custom.

## Modo Lote (GUI)
- Botao "Executar N vezes" roda N execucoes (default 30) em thread daemon.
- Semente base fixa 42 + i para cada execucao.
- Sem animacao; usa BatchRunner.
- Execucoes validas (destino atingido) vao para a tabela (Media, Desvio).
- Abortadas (limite 2M pacotes) e inalcançaveis (fila vazia) ficam FORA da media.
- Botao desabilitado durante o lote; reabilitado ao fim (sucesso ou erro).
+- **Progresso**: O lote roda em thread de fundo e atualiza a progressao na barra de status da aba RESULTS.
+- **Cancelamento**: Clique em "Cancelar lote" para interromper entre execucoes. O estado e restaurado ao cancelamento.

## Resultados (GUI)
- **Tabela V1-V4**: Cada linha corresponde a uma versao. Colunas:
  - **Ultima**: Snapshot da ultima execucao valida.
  - **N**: Quantidade de execucoes validas (destino atingido).
  - **Media**: Media aritmetica dos snapshots das execucoes validas.
  - **Desvio**: Desvio padrao amostral dos snapshots.
  - Apenas execucoes que atingiram o destino sao contabilizadas na Media e Desvio.
  - Abortadas (limite 2M pacotes) e inalcançaveis (fila vazia) ficam fora desses calculos.
- **Grafico RESULTS**: Barra horizontal por versao (V1 a V4).
  - Clique no icone de lupa para alternar **Escala Logaritmica** (on/off).
  - Em escala log, valores pequenos ficam mais distinguiveis.
  - O chart usa a classe Statistics para calcular media e desvio-padrao.

## Sementes e Reprodutibilidade
- **Console**: Passar a semente como ultimo argumento (`java -cp build util.ConsoleRunner 3 1 4 3 42`).
- **GUI**: Digite a semente no campo "Semente" no painel de controles. A mesma semente com os mesmos parametros (backbone, origem, destino, estrategia, TTL) reproduz o mesmo comportamento.
- **Lote**: Semente base + i para cada iteracao (baseSeed, baseSeed+1, ..., baseSeed+n-1).
- **Reprodutibilidade**: Com os mesmos parametros (backbone, origem, destino, estrategia, TTL, semente), as simulacoes sao deterministas. A ordem das chamadas ao gerador aleatorio nao deve ser alterada.

## WindowSweep (Varredura de Janelas V4)
- **Finalidade**: Testar o efeito da janela temporal (windowMs) na deduplicacao V4.
- **Como executar**: `java -cp build util.WindowSweep <backbone> <origem> <destino> <ttl> <sementeBase>`
- **Windows testadas**: 0, 250, 500, 1000, 1500, 2000, 5000 (ms).
- **Resultado**: Para cada janela, conta-se quantas execucoes atingiram o destino e o snapshot medio. Use para comparar o efeito da deduplicacao V4.
+- Cada execucao usa uma nova instancia da Strategy4Custom com a janela especificada.
+- windowMs = 0 deve gerar resultados equivalentes a V3 (nenhuma deduplicacao).
+- Os resultados permitem comparar o impacto da janela no numero de pacotes finais e na reprodutibilidade.

## Testes com Timeout (obrigatorio)
```bash
# ConsoleRunner (deve terminar)
timeout -k 2 20 java -Xmx512m -cp build util.ConsoleRunner 3 1 4 3 42

# BatchConsole (30 execucoes x 4 versoes)
timeout -k 2 60 java -Xmx512m -cp build util.BatchConsole 30 1 4 3 42

# GUI (apenas verifica inicio)
timeout 8 ./run.sh   # codigo 124 sem excecao = sucesso
```

## Estrutura MVC
```
Principal.java          main (pacote default)
backbone.txt            topologia
build.sh / run.sh       scripts
.gitignore              build/, *.class
model/                  Graph, Node, Link, Packet, Simulator,
                        ForwardingStrategy, Strategies, LatencyProvider, RunResult
controller/             SimulationController, BatchRunner, BatchResult,
                        StrategyFactory
view/                   FloodingApp, GraphPane, ControlPanel,
                        ResultsTable
util/                   BackboneLoader, ConsoleRunner, BatchConsole,
                        Statistics, GraphInfo, WindowSweep, NodeLabelUtil

## Testes com Timeout (obrigatorio)
```bash
# ConsoleRunner (deve terminar)
timeout -k 2 20 java -Xmx512m -cp build util.ConsoleRunner 3 1 4 3 42

# BatchConsole (30 execucoes x 4 versoes)
timeout -k 2 60 java -Xmx512m -cp build util.BatchConsole 30 1 4 3 42

# GUI (apenas verifica inicio)
timeout 8 ./run.sh   # codigo 124 sem excecao = sucesso
```

## Padronizacao de Codigo
- Cabecalho fixo em todo .java (autor, matricula, datas, nome, funcao)
- Bloco de metodo antes de cada metodo/construtor
- Indentacao 2 espacos, chaves K&R, comentario de fechamento
- Linhas <= 80 colunas, sem acentos em comentarios e Strings
- Java 8 apenas (sem var, records, List.of, text blocks, etc.)