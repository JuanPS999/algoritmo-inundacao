package flooding.model;

/**
 * No (roteador) da rede. Identificado por id numerico em [1..N].
 * Rotulo na GUI: 1->A, 2->B, ...
 */
public class Node {

    private final int id;

    public Node(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return String.valueOf((char) ('A' + id - 1));
    }

    @Override
    public String toString() {
        return getLabel() + "(" + id + ")";
    }
}
