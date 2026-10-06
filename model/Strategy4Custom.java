/* ***************************************************************
* Autor............: Juan Prado Da Silva
* Matricula........: 202310291
* Inicio...........: 02/10/2026
* Ultima alteracao.: 06/10/2026
* Nome.............: Strategy4Custom.java
* Funcao...........: Versao 4 de estrategia de flooding com deduplicacao
 * por janela de tempo por no.
*************************************************************** */
package model;

import model.Graph;
import model.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Versao 4: flooding com deduplicacao por janela de tempo por no.
 * Cada no mantem o instante da ultima observacao de cada messageId.
 * Se uma copia chegar dentro da janela configurada, e descartada como
 * duplicata. Duplicatas NAO renovam o registro.
 * Janela 0 => nunca existe duplicata => V4 equivalente a V3.
 */
public class Strategy4Custom implements ForwardingStrategy {

    private final long janelaMs;
    private final Map<Integer, Map<Long, Long>> visto;
    // contador de duplicatas descartadas nesta execucao
    private long duplicatasDescartadas;

    /* ***************************************************************
    * Metodo: Strategy4Custom
    * Funcao: Construtor com janela padrao de 1000ms.
    * Parametros: nenhum
    * Retorno: void
    *************************************************************** */
    public Strategy4Custom() {
        this(1000);
    }

    /* ***************************************************************
    * Metodo: Strategy4Custom
    * Funcao: Construtor com janela de deduplicacao especificada.
    * Parametros: janelaMs - janela em milissegundos
    * Retorno: void
    *************************************************************** */
    public Strategy4Custom(long janelaMs) {
        this.janelaMs = janelaMs;
        this.visto = new HashMap<Integer, Map<Long, Long>>();
        this.duplicatasDescartadas = 0;
    }

    /* ***************************************************************
    * Metodo: registrarNaOrigem
    * Funcao: Registra um messageId na origem em t=0 (para V4).
    *          O registro e feito per-no: o messageId e marcado como visto
    *          no no especificado no instante t.
 * Parametros: messageId - id logico da mensagem; nodeId - no da origem;
 *             t - instante virtual.
 * Retorno: void
 *************************************************************** */
    public void registrarNaOrigem(long messageId, int nodeId, long t) {
        visto.computeIfAbsent(nodeId, k -> new HashMap<Long, Long>())
             .put(messageId, t);
    }

    /* ***************************************************************
    * Metodo: getDuplicatasDescartadas
    * Funcao: Retorna o numero de duplicatas descartadas nesta execucao.
    * Parametros: nenhum
    * Retorno: long - contador de duplicatas.
    *************************************************************** */
    public long getDuplicatasDescartadas() {
        return duplicatasDescartadas;
    }

    /* ***************************************************************
    * Metodo: forward
    * Funcao: Decide por quais interfaces do no o pacote sera retransmitido.
 * Em V4, ANTES da logica de TTL, verifica deduplicacao por janela de tempo.
 * Se a copia for duplicata dentro da janela, descarta (retorna lista vazia).
 * Caso contrario, atualiza o registro e aplica a logica V3 (decremento de TTL,
 * reenvio exceto pela interface de chegada).
 * Parametros: graph - topologia da rede; nodeId - no que recebeu o pacote;
 *           packet - pacote recebido (pode ser alterado).
 * Retorno: links por onde retransmitir; lista vazia = descartar.
 *************************************************************** */
    @Override
    public List<Link> forward(Graph graph, int nodeId, Packet packet) {
        long t = packet.getArrivalTime();

        // Get or create the per-node visto map
        Map<Long, Long> nodeVisto = visto.computeIfAbsent(nodeId, k -> new HashMap<Long, Long>());

        Long instanteVisto = nodeVisto.get(packet.getMessageId());
        if (instanteVisto != null && (t - instanteVisto) < janelaMs) {
            // Duplicate within window - discard, DO NOT update visto
            duplicatasDescartadas++;
            return new ArrayList<Link>();
        }

        // Not a duplicate (outside window or first observation): update visto
        // and apply V3 logic. A duplicata NAO renova o registro, então só
        // atualizamos quando a copia nao e duplicata.
        nodeVisto.put(packet.getMessageId(), t);

        // V3 logic: decrement TTL
        packet.setTtl(packet.getTtl() - 1);
        if (packet.getTtl() <= 0) {
            return new ArrayList<Link>(); // descarta por TTL expirado
        }

        List<Link> out = new ArrayList<Link>();
        for (Link link : graph.linksOf(nodeId)) {
            if (link.isEnabled() && link != packet.getArrivalLink()) {
                out.add(link);
            }
        }
        return out;
    }
}