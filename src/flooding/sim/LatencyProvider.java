package flooding.sim;

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

    public LatencyProvider() {
        this.random = new Random();
    }

    public LatencyProvider(long seed) {
        this.random = new Random(seed);
    }

    public int nextLatencyMs() {
        return MIN_MS + random.nextInt(MAX_MS - MIN_MS + 1);
    }
}
