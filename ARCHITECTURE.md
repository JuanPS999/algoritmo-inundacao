Arquitetura - Simulador de Inundacao
====================================

1. Visao geral
--------------

O projeto implementa um simulador de algoritmos de roteamento por inundacao
para a disciplina de Redes II. Possui:

- Simulador de eventos discretos em tempo virtual;
- Quatro estrategias de encaminhamento (V1 a V4);
- Interface grafica em JavaFX;
- Execucao em modo console;
- Execucao de lotes para comparacao estatistica;
- Visualizacao dos pacotes em transito;
- Registro de eventos;
- Suporte a seed para reproducibilidade;
- Deduplicacao temporal na estrategia V4.

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
--------------------

FloodingApp
Responsavel pela composicao inicial da interface:

- Carregar o backbone;
- Criar os componentes visuais;
- Criar o SimulationController;
- Conectar os componentes;
- Montar a Scene;
- Configurar o Stage;
- Aplicar o tema.

FloodingApp nao controla o ciclo de vida da simulacao.

ControlPanel
Concentra os controles da interface:

- Origem e destino;
- Estrategia;
- TTL;
- Seed;
- Velocidade;
- Start;
- Pause;
- Reset;
- Step;
- Batch;
- Contadores e estado.

As acoes sao comunicadas ao SimulationController por callbacks.

SimulationController
E o controlador principal da interface grafica.

Responsabilidades:

- Receber comandos do ControlPanel;
- Controlar o estado da simulacao;
- Criar/recriar o Simulator;
- Controlar o tempo virtual;
- Controlar o AnimationTimer;
- Processar frames;
- Atualizar a visualizacao;
- Atualizar resultados;
- Atualizar o log;
- Controlar Start, Pause, Step e Reset;
- Executar batches solicitados pela interface.

O AnimationTimer pertence ao SimulationController.

GraphPane
Representa visualmente:

- Nos;
- Enlaces;
- Pacotes em transito;
- Direcao dos pacotes;
- TTL quando habilitado.

A direcao usa a origem registrada no evento de voo (fromNode), evitando depender da identidade de uma instancia especifica de Packet.

ResultsTable
Apresenta resultados da simulacao, incluindo quando aplicavel:

- Estrategia;
- Snapshot;
- Tempo virtual;
- Quantidade de pacotes;
- Estatisticas de RunResult.

Fornece seu Node para insercao no layout.

EventLogPane
Exibe os eventos produzidos durante a simulacao. O SimulationController encaminha os eventos para o painel por meio de um consumidor de log.

Theme
Concentra a configuracao visual. A aplicacao deve utilizar:

Theme.apply(scene);

Nao existe dependencia de um arquivo externo theme.css.

3. Modelo da simulacao
-----------------------

Graph
Representa a topologia carregada de backbone.txt.

Os nos sao identificados por IDs e os enlaces conectam os nos. O peso do enlace
representa o custo/peso da topologia e nao a latencia de transmissao.

Node
Representa um no da rede e suas conexoes.

Link
Representa um enlace entre dois nos. Enlaces desabilitados devem descartar
transmissoes. Cada link tem ids a e b e um peso. Pode ser habilitado ou
desabilitado pela GUI.

Packet
Representa uma copia de pacote em transito.

Dados do pacote:

- id: identifica a copia;
- messageId: identifica a mensagem logica;
- ttl: valor TTL;
- origem: no de origem;
- destino: no destino;
- enlace de entrada, quando aplicavel.

Cada copia encaminhada possui seu proprio id. Copias da mesma mensagem logica
compartilham o mesmo messageId. O messageId e obrigatorio para a deduplicacao da V4.

Simulator
E o nucleo da simulacao.

Responsabilidades:

- Criar o pacote inicial;
- Controlar o tempo virtual;
- Agendar transmisssoes;
- Processar chegadas;
- Aplicar estrategias;
- Contar pacotes;
- Registrar a primeira chegada no destino;
- Produzir RunResult.

A simulacao usa uma fila de prioridade de eventos. Eventos sao processados em
ordem de tempo virtual; empates preservam a ordem de insercao (FIFO).

LatencyProvider
Fornecedor de latencias de link para a simulacao.

- Fornece latencias inteiras em [1, 500] ms, sorteadas a cada transmissao em um link;
- O peso da aresta NAO influencia a latencia;
- Aceita semente opcional para reprodutibilidade.

ForwardingStrategy
Interface de estrategia de reenvio de pacotes.

- Decide por quais interfaces do no o pacote sera retransmitido;
- Pode alterar o pacote (ex.: decremento de TTL na V3);
- O receptor TAMBEM reencaminha (decisao tomada no Simulator).

Strategy1AllInterfaces (V1)
Encaminha por todas as interfaces disponiveis. O enlace de entrada nao impede
o encaminhamento. TTL nao limita V1.

Strategy2ExceptIncoming (V2)
Encaminha por todas as interfaces, exceto a de chegada. O enlace de entrada
impede o encaminhamento. Ignora TTL.

Strategy3Ttl (V3)
Como a V2, e antes de reenviar faz ttl = ttl - 1; se ttl <= 0, descarta.
A checagem de entrega vem antes do descarte (tratado no Simulator, no momento
da chegada). TTL e definido via spinner na interface grafica (padrao: 3). O usuario
pode alterar o TTL durante a simulacao.

Strategy4Custom (V4)
Flooding com deduplicacao por janela de tempo por no.

Possui windowMs, com padrao de 1000 ms.

Estado mantido por no:

messageId -> instante da ultima observacao

Uma mensagem e duplicada quando:

t - instanteVisto < windowMs

Duplicatas:

- nao sao encaminhadas;
- nao renovam o instante da observacao.

Entradas expiradas sao removidas somente durante consultas. Nao existem timers
ou eventos separados para expiracao.

A origem e registrada como primeira observacao no instante zero.

Com windowMs = 0, V4 deve ser equivalente a V3.

StrategyFactory
Centraliza a criacao das estrategias:

- V1;
- V2;
- V3;
- V4 com windowMs especificado.

Cada execucao deve possuir estado de estrategia independente.

RunResult
Resultado de uma execucao do simulador.

Atributos:

- reached: verificacao se o destino foi alcancado;
- snapshotCounter: contador de pacotes gerados na primeira chegada ao destino;
- snapshotTime: tempo virtual da primeira chegada;
- packetsGenerated: total de pacotes gerados durante a simulacao;
- virtualTime: tempo virtual final da simulacao;
- abortedByLimit: verificacao se a simulacao foi encerrada por atingir o limite de transmissoes (2M).

BatchRunner
Executa multiplas simulacoes em lote (sem JavaFX).

- Executa 'count' simulacoes com sementes baseSeed + i;
- Classifica resultados e devolve um BatchResult imutavel;
- Pode variar estrategia, origem, destino, quantidade de execucoes, seed, windowMs.

BatchResult
Resultado imutavel de uma execucao em lote.

Atributos:

- samples: lista imutavel das amostras (contadores de pacotes em execucoes validas);
- aborted: numero de execucoes abortadas por limite de pacotes;
- unreachable: numero de execucoes inalcancaveis (destino nao atingido).

Statistics
Concentra calculos estatisticos usados pelos batches.

- mean: media aritmetica de List<Long>;
- standardDeviation: desvio-padrao amostral de List<Long>.

4. Tempo e latencia
--------------------

Cada transmissao recebe latencia aleatoria entre 1 ms e 500 ms quando a transmissao
e agendada. O peso/metrica do backbone nao representa a latencia.

A seed permite reproduzir a mesma sequencia pseudoaleatoria.

O tempo da simulacao e virtual. Na GUI, o AnimationTimer converte tempo real em

O tempo da simulacao e virtual. Na GUI, o AnimationTimer converte tempo real em
avanco de tempo virtual conforme a velocidade selecionada.

5. Execucao em Lote
--------------------

O fluxo de execucao em lote atravessas as seguintes componentes:

- **SimulationController**: Recebe o evento "Executar N vezes" do ControlPanel.
  Cria um Task<Void> que itera sobre versoes 1 a 4.
  Para cada versao, executa 'count' simulacoes com sementes baseSeed + i.
  Atualiza progresso via Platform.runLater na aba RESULTS.
  Registra resultados na ResultsTable (samples validas, abortadas, inalcançaveis).
  O BatchRunner executa o trabalho em thread daemon, nao bloqueando a GUI.

- **BatchRunner**: Executa multiplas simulacoes em lote (sem JavaFX).
  Recebe: graph, versao, sourceId, destinationId, ttl, count, baseSeed, windowMs.
  Para cada iteracao i: usa seed = baseSeed + i.
  Classifica resultados: sample (destino atingido), aborted (limite 2M pacotes),
  unreachable (fila vazia).
  Devolve um BatchResult imutavel com lists de samples, aborted count, unreachable count.

- **ResultsTable**: Recebe resultados do BatchController e preenche as colunas:
  - Versao, Ultima, N (quantidade validas), Media, Desvio.
  - Conta tambem abortadas e inalcançaveis separadamente.
  - Suporta escala logaritmica (setLogScale(true/false)).
  - Cada linha representa uma versao; os dados sao agregados das execucoes validas.

- **GraphPane**: Atualiza a visualizacao durante o lote (opcional),
  mostrando pacotes em transito conforme o simulador avanca.

- **WindowSweep**: Ferramenta de teste na utilitario.
  Executa varredura das versoes V1 a V4 com diferentes valores de windowMs.
  Para V4, testa janelas: 0, 250, 500, 1000, 1500, 2000, 5000 ms.
  Exibe resultados comparativos do efeito da deduplicacao temporal.
  Relaciona-se com o parâmetro windowMs do StrategyFactory.create(version, windowMs).

6. Sementes e Reprodutibilidade
--------------------------------

- O LatencyProvider aceita uma semente opcional.
- Semente base fixa: nas execucoes em lote, cada iteracao usa baseSeed + i.
- Mesmos parametros (backbone, origem, destino, estrategia, TTL, semente)
  geram comportamento deterministico.
- Alteracoes na ordem de eventos ou transmisssoes podem alterar a
  sequencia pseudoaleatoria e, portanto, os resultados.

7. WindowSweep
--------------

Ferramenta de varredura para a estrategia V4 com diferentes janelas temporais.

- Executa simulacoes V1 a V4 para comparar o efeito da deduplicacao.
- Windows testadas (windowMs em ms): 0, 250, 500, 1000, 1500, 2000, 5000.
- windowMs = 0 deve ser equivalente a V3 (sem deduplicacao).
- Para cada janela, reporta-se: quantidade de execucoes validas, snapshots,
  media e desvio-padrao.
- Nao cria timers/eventos especificos para expiracao.
- Entradas expiradas sao removidas somente durante consultas.

8. Entrega no destino
---------------------

Quando um pacote chega ao destino:

- A chegada e validada;
- Se for a primeira chegada, o snapshot e registrado;
- A estrategia ainda pode decidir encaminhar o pacote;
- As copias encaminhadas seguem normalmente.

O snapshot representa o numero de pacotes observado na primeira chegada ao destino.

9. Estrategias de inundacao
----------------------------

V1 - Todas as interfaces
Encaminha por todas as interfaces disponiveis. O enlace de entrada nao impede
o encaminhamento. TTL nao limita V1.

V2 - Exceto interface de entrada
Encaminha por todas as interfaces, exceto aquela pela qual o pacote acabou de
chegar. O enlace de entrada impede o encaminhamento. Ignora TTL.

V3 - TTL
Usa a logica da V2 e adiciona TTL:

- Decrementa o TTL;
- Encaminha se ainda puder ser encaminhado (TTL > 0);
- Reconhece a entrega no destino antes do descarte por TTL.

TTL e definido via spinner na interface grafica (padrao: 3). O usuario pode alterar o TTL durante a simulacao.

V4 - Deduplicacao temporal
Usa a logica da V3 e adiciona deduplicacao temporal por no.

Possui windowMs, com padrao de 1000 ms.

O estado e mantido por no: messageId -> instante da ultima observacao.

Uma mensagem e duplicada quando:

tempoAtual - instanteVisto < windowMs

Duplicatas:

- nao sao encaminhadas;
- nao renovam o instante da observacao.

Entradas expiradas sao removidas somente durante consultas. Nao existem timers
ou eventos separados para expiracao.

A origem e registrada como primeira observacao no instante zero.

Com windowMs = 0, V4 deve ser equivalente a V3.

10. Estrutura de arquivos
-------------------------

model/
    ForwardingStrategy.java   - Interface de estrategia de reenvio
    Graph.java                - Grafo da topologia
    LatencyProvider.java      - Fornecedor de latencias
    Link.java                 - Aresta entre nos
    Node.java                 - No da rede
    Packet.java               - Copia de pacote em transito
    RunResult.java            - Resultado de simulacao
    SimulationListener.java   - Listener de eventos

controller/
    SimulationController.java - Controlador da interface grafica
    StrategyFactory.java      - Fabrica de estrategias
    BatchRunner.java          - Execucao em lote
    BatchResult.java          - Resultado em lote

view/
    FloodingApp.java          - Inicializa a aplicacao JavaFX
    ControlPanel.java         - Controles da interface
    GraphPane.java            - Visualizacao do grafo
    ResultsTable.java         - Tabela de resultados
    EventLogPane.java         - Log de eventos
    AboutDialog.java          - Dialogo "Sobre"
    Theme.java                - Tema visual (Gruvbox escuro)

util/
    BackboneLoader.java       - Carrega backbone.txt
    ConsoleRunner.java        - Simulacao Console
    BatchConsole.java         - Lote em console
    WindowSweep.java          - Varredura de janelas V4
    Statistics.java           - Calculos estatisticos
    GraphInfo.java            - Imprime grafo e diametro
    NodeLabelUtil.java        - Conversao id <-> rotulo

11. Separacao de responsabilidades
-----------------------------------

View
Responsavel por:

- Exibicao;
- Controles;
- Graficos;
- Tabela;
- Log;
- Tema.

Controller
Responsavel por:

- Coordenar a interface;
- Controlar o ciclo da simulacao grafica;
- Traduzir comandos da interface para operacoes do modelo.

Model
Responsavel por:

- Grafo;
- Nos;
- Enlaces;
- Pacotes;
- Eventos;
- Estrategias;
- Simulacao.

Util
Responsavel por:

- Carregamento;
- Estatisticas;
- Execucao em console;
- Varreduras experimentais;
- Informacoes auxiliares.

12. Reprodutibilidade
---------------------

As simulacoes aceitam seed.

Com os mesmos:

- Backbone;
- Origem;
- Destino;
- Estrategia;
- Parametros;
- Seed

O comportamento deterministico esperado deve ser preservado.

13. Regras de compatibilidade

O projeto deve permanecer compativel com:

- Java 8;
- JavaFX 8.

Compilacao principal:

javac Principal.java

Build:

./build.sh

Execucao grafica:

./run.sh

11. Estado atual da arquitetura
--------------------------------

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

12. Principio para manutencao
----------------------------

Alteracoes futuras devem ser pequenas e direcionadas.

Nao fazer grandes refatoracoes estruturais durante a fase final do trabalho.

Prioridade:

- manter a compilacao;
- preservar as regras das estrategias;
- preservar a reproducibilidade;
- corrigir bugs reais;
- validar a interface;
- manter a documentacao sincronizada com o codigo;
- preparar a entrega final.
