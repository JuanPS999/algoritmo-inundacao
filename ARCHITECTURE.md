# Arquitetura do Simulador de Roteamento por Inundacao

## Visao Geral
Arquitetura MVC (Model-View-Controller) em Java 8 com JavaFX.
Pacotes: model, controller, view, util (na raiz do projeto, sem pasta src).

## Fluxo de um Pacote

1. **Inicio (Simulator.start)**
   - O transmissor cria uma copia do pacote para cada interface (link) habilitada.
   - Cada copia recebe ID unico, TTL inicial e interface de chegada = null.
   - A copia e colocada na fila de eventos (PriorityQueue) com tempo de chegada = tempo atual + latencia aleatoria [1,500] ms.
   - Contador de pacotes gerados incrementa a cada copia.

2. **Processamento de Evento (Simulator.process)**
   - Evento removido da fila (menor tempo, desempate FIFO).
   - Se o link foi desabilitado durante o transito: descarta.
   - Se o no e o receptor e ainda nao ha snapshot: registra snapshot (contador atual, tempo virtual).
   - Chama a estrategia de encaminhamento para obter lista de links de saida.
   - Para cada link de saida: cria nova copia com interface de chegada = esse link, agenda chegada em tempo atual + latencia, incrementa contador.

3. **Estrategias (ForwardingStrategy.forward)**
   - V1: retorna todas as interfaces do no.
   - V2: retorna todas exceto a interface de chegada do pacote.
   - V3: como V2, mas decrementa TTL; se TTL <= 0, retorna lista vazia (descartado).
   - V4: stub que delega para V3.

4. **Tempo Virtual**
   - GUI: AnimationTimer avanca relogio virtual proporcional ao tempo real * fator de velocidade.
   - Chamadas pull: `advanceTo(tempo)` processa eventos ate esse instante; `inFlight()` devolve pacotes em transito para desenho.
   - Lote: `runUntilFirstArrival()` processa ate primeira chegada, fila vazia ou limite ABORT_LIMIT (2M).

5. **Desenho (GraphPane)**
   - Nos em circulo, links com peso. Clique alterna habilitado/desabilitado.
   - Pacotes interpolados ao longo do link; TTL exibido nas versoes 3 e 4.

## Modelo (model/)
- Graph, Node, Link: topologia carregada de backbone.txt.
- Packet: id, ttl, incomingLink.
- Simulator: nucleo de eventos, tempo virtual, snapshot, listener.
- ForwardingStrategy + 4 implementacoes.
- LatencyProvider: latencia aleatoria [1,500] por transmissao.
- RunResult: resultado de execucao em lote.

## Controlador (controller/)
- SimulationController: liga GUI (ControlPanel, GraphPane, ResultsTable) ao Simulator; AnimationTimer; Task para lote.
- BatchRunner: executa N simulacoes em lote (sem JavaFX), usa StrategyFactory.
- StrategyFactory: cria estrategia por numero da versao.
- BatchResult: imutavel, contem amostras, abortadas, inalcançaveis.

## Visao (view/)
- FloodingApp: Application, carrega backbone, monta janela (MenuBar, GraphPane centro, ControlPanel direita, ResultsTable baixo).
- ControlPanel: ComboBox versao/origem/destino, Spinner TTL/N, Slider velocidade, botoes, labels.
- GraphPane: desenho do grafo e pacotes em transito.
- ResultsTable: TableView 4 linhas (V1-V4) com colunas Versao, Ultima, N, Media, Desvio.
- AboutDialog: Alert com autor, matricula, disciplina, versoes, OPTION4_TEXT.

## Util (util/)
- BackboneLoader: le backbone.txt (N; linhas A;B;PESO), valida ids, calcula diametro (BFS).
- ConsoleRunner: execucao unica via linha de comando, imprime eventos e snapshot.
- BatchConsole: executa lote para todas versoes via linha de comando.
- Statistics: mean e standardDeviation amostral de List<Long>.
- GraphInfo: diametro do grafo (BFS).