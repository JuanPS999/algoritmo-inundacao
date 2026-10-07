Arquitetura - Simulador de Inundacao
1. Visao geral

O projeto implementa um simulador de algoritmos de roteamento por inundacao para a disciplina de Redes II.

A aplicacao possui:

    simulador de eventos discretos;

    quatro estrategias de encaminhamento;

    interface grafica em JavaFX;

    execucao em modo console;

    execucao de lotes para comparacao estatistica;

    visualizacao dos pacotes em transito;

    registro de eventos;

    suporte a seed para reproducibilidade;

    deduplicacao temporal na estrategia V4.

Fluxo geral:

Principal
    |
    v
FloodingApp
    |
    +-------------------------------+
    |               |               |
    v               v               v
ControlPanel    GraphPane       ResultsTable
    |               |               |
    +---------------+---------------+
                    |
                    v
          SimulationController
                    |
                    v
                Simulator
                    |
                    v
        ForwardingStrategy
          /    /    |    \
        V1    V2    V3    V4

2. Interface grafica
FloodingApp

Responsavel pela composicao inicial da interface:

    carregar o backbone;

    criar os componentes visuais;

    criar o SimulationController;

    conectar os componentes;

    montar a Scene;

    configurar o Stage;

    aplicar o tema.

FloodingApp nao controla o ciclo de vida da simulacao.
ControlPanel

Concentra os controles da interface:

    origem e destino;

    estrategia;

    TTL;

    seed;

    velocidade;

    Start;

    Pause;

    Reset;

    Step;

    batch;

    contadores e estado.

As acoes sao comunicadas ao SimulationController por callbacks.
SimulationController

E o controlador principal da interface grafica.

Responsabilidades:

    receber comandos do ControlPanel;

    controlar o estado da simulacao;

    criar/recriar o Simulator;

    controlar o tempo virtual;

    controlar o AnimationTimer;

    processar frames;

    atualizar a visualizacao;

    atualizar resultados;

    atualizar o log;

    controlar Start, Pause, Step e Reset;

    executar batches solicitados pela interface.

O AnimationTimer pertence ao SimulationController.
GraphPane

Representa visualmente:

    nos;

    enlaces;

    pacotes em transito;

    direcao dos pacotes;

    TTL quando habilitado.

A direcao usa a origem registrada no evento de voo (fromNode), evitando depender da identidade de uma instancia especifica de Packet.
ResultsTable

Apresenta resultados da simulacao, incluindo quando aplicavel:

    estrategia;

    snapshot;

    tempo virtual;

    quantidade de pacotes;

    estatisticas de RunResult.

Fornece seu Node para insercao no layout.
EventLogPane

Exibe os eventos produzidos durante a simulacao. O SimulationController encaminha os eventos para o painel por meio de um consumidor de log.
Theme

Concentra a configuracao visual. A aplicacao deve utilizar:

Theme.apply(scene);

Nao existe dependencia de um arquivo externo theme.css.
3. Modelo da simulacao
Graph

Representa a topologia carregada de backbone.txt.

Os nos sao identificados por IDs e os enlaces conectam os nos. O peso do enlace representa o custo/peso da topologia e nao a latencia de transmissao.
Node

Representa um no da rede e suas conexoes.
Link

Representa um enlace entre dois nos. Enlaces desabilitados devem descartar transmisssoes.
Packet

Representa uma copia de pacote em transito.

Entre seus dados estao:

    id: identifica a copia;

    messageId: identifica a mensagem logica;

    TTL;

    origem;

    destino;

    enlace de entrada, quando aplicavel.

Cada copia encaminhada possui seu proprio id. Copias da mesma mensagem logica compartilham o mesmo messageId.
Simulator

E o nucleo da simulacao.

Responsabilidades:

    criar o pacote inicial;

    controlar o tempo virtual;

    agendar transmisssoes;

    processar chegadas;

    aplicar estrategias;

    contar pacotes;

    registrar a primeira chegada no destino;

    produzir RunResult.

A simulacao usa uma fila de prioridade de eventos. Eventos sao processados em ordem de tempo virtual; empates preservam a ordem de insercao.
4. Tempo e latencia

Cada transmissao recebe latencia aleatoria entre 1 ms e 500 ms.

A seed permite reproduzir a mesma sequencia pseudoaleatoria.

O peso do enlace nao representa a latencia.

O tempo da simulacao e virtual. Na GUI, o AnimationTimer converte tempo real em avanco de tempo virtual conforme a velocidade selecionada.
5. Entrega no destino

Quando um pacote chega ao destino:

    a chegada e validada;

    se for a primeira chegada, o snapshot e registrado;

    a estrategia ainda pode decidir encaminhar o pacote;

    as copias encaminhadas seguem normalmente.

O snapshot representa o numero de pacotes observado na primeira chegada ao destino.
6. Estrategias de inundacao
V1 - Todas as interfaces

Encaminha por todas as interfaces disponiveis. O enlace de entrada nao impede o encaminhamento. TTL nao limita V1.
V2 - Exceto interface de entrada

Encaminha por todas as interfaces, exceto aquela pela qual o pacote acabou de chegar.
V3 - TTL

Usa a logica da V2 e adiciona TTL:

    decrementa o TTL;

    encaminha se ainda puder ser encaminhado;

    reconhece a entrega no destino antes do descarte por TTL.

O TTL padrao pode ser calculado a partir do diametro do grafo.
V4 - Deduplicacao temporal

Usa a logica da V3 e adiciona deduplicacao temporal por no.

Possui windowMs, com padrao de 1000 ms.

O estado e mantido por no:

messageId -> instante da ultima observacao

Uma mensagem e duplicada quando:

tempoAtual - instanteVisto < windowMs

Duplicatas:

    nao sao encaminhadas;

    nao renovam o instante da observacao.

Entradas expiradas sao removidas somente durante consultas. Nao existem timers ou eventos separados para expiracao.

A origem e registrada como primeira observacao no instante zero.

Com windowMs = 0, V4 deve ser equivalente a V3.
7. StrategyFactory

Centraliza a criacao das estrategias:

    V1;

    V2;

    V3;

    V4;

    V4 com windowMs especificado.

Cada execucao deve possuir estado de estrategia independente.
8. WindowSweep

Compara V4 usando as janelas:

0
250
500
1000
1500
2000
5000

Permite observar o efeito da janela temporal na deduplicacao e na inundacao.
9. Execucao em lote
BatchRunner

Executa varias simulacoes para uma configuracao, podendo variar:

    estrategia;

    origem;

    destino;

    quantidade de execucoes;

    seed.

BatchConsole

Fornece execucao de batches em console.

Resultados podem incluir:

    media;

    desvio padrao;

    quantidade de execucoes validas;

    snapshot;

    quantidade de pacotes;

    estatisticas;

    duplicatas na V4.

Statistics

Concentra calculos estatisticos usados pelos batches.
10. Carregamento da topologia

BackboneLoader le backbone.txt e transforma sua descricao em um Graph.

A mesma topologia e usada pela interface grafica e pelas execucoes de console.
11. Organizacao dos pacotes

model/
    ForwardingStrategy.java
    Graph.java
    LatencyProvider.java
    Link.java
    Node.java
    Packet.java
    RunResult.java
    SimulationListener.java
    Simulator.java
    Strategy1AllInterfaces.java
    Strategy2ExceptIncoming.java
    Strategy3Ttl.java
    Strategy4Custom.java

controller/
    BatchResult.java
    BatchRunner.java
    SimulationController.java
    StrategyFactory.java

view/
    ControlPanel.java
    EventLogPane.java
    FloodingApp.java
    GraphPane.java
    ResultsTable.java
    Theme.java

util/
    BackboneLoader.java
    BatchConsole.java
    ConsoleRunner.java
    GraphInfo.java
    NodeLabelUtil.java
    Statistics.java
    WindowSweep.java

12. Separacao de responsabilidades
View

Responsavel por:

    exibicao;

    controles;

    graficos;

    tabela;

    log;

    tema.

Controller

Responsavel por:

    coordenar a interface;

    controlar o ciclo da simulacao grafica;

    traduzir comandos da interface para operacoes do modelo.

Model

Responsavel por:

    grafo;

    nos;

    enlaces;

    pacotes;

    eventos;

    estrategias;

    simulacao.

Util

Responsavel por:

    carregamento;

    estatisticas;

    execucao em console;

    varreduras experimentais;

    informacoes auxiliares.

13. Reprodutibilidade

As simulacoes aceitam seed.

Com os mesmos:

    backbone;

    origem;

    destino;

    estrategia;

    parametros;

    seed

o comportamento deterministico esperado deve ser preservado.
14. Regras de compatibilidade

O projeto deve permanecer compativel com:

    Java 8;

    JavaFX 8.

Compilacao principal:

javac Principal.java

Build:

./build.sh

Execucao grafica:

./run.sh

15. Estado atual da arquitetura

Fluxo da GUI:

Usuario
   |
   v
ControlPanel
   |
 callbacks
   v
SimulationController
   |
   v
Simulator
   |
   v
ForwardingStrategy
   |
   +--> V1
   +--> V2
   +--> V3
   +--> V4

Apresentacao:

SimulationController
       |
       +--> GraphPane
       +--> ResultsTable
       +--> EventLogPane
       +--> ControlPanel

FloodingApp monta essa estrutura e inicia a aplicacao.
16. Principio para manutencao

Alteracoes futuras devem ser pequenas e direcionadas.

Nao fazer grandes refatoracoes estruturais durante a fase final do trabalho.

Prioridade:

    manter a compilacao;

    preservar as regras das estrategias;

    preservar a reproducibilidade;

    corrigir bugs reais;

    validar a interface;

    manter a documentacao sincronizada com o codigo;

    preparar a entrega final.
