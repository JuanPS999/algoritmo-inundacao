package flooding.model;

/**
 * Aresta nao direcionada entre dois nos, com peso (metrica) e estado
 * habilitado/desabilitado (util na GUI; links desabilitados nao transmitem).
 */
public class Link {

    private final int id;
    private final int a;
    private final int b;
    private final int weight;
    private boolean enabled = true;

    public Link(int id, int a, int b, int weight) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.weight = weight;
    }

    public int getId() {
        return id;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Retorna o id do no oposto a {@code nodeId} neste link. */
    public int other(int nodeId) {
        if (nodeId == a) return b;
        if (nodeId == b) return a;
        throw new IllegalArgumentException("No " + nodeId + " nao pertence ao link " + id);
    }

    @Override
    public String toString() {
        return a + "-" + b + " (peso " + weight + (enabled ? "" : ", desabilitado") + ")";
    }
}
