package flooding.sim;

/**
 * Resultado de uma execucao (runUntilFirstArrival).
 */
public class RunResult {

    private final boolean reached;
    private final long snapshotCounter;
    private final long snapshotTime;
    private final long packetsGenerated;
    private final long virtualTime;
    private final boolean abortedByLimit;

    public RunResult(boolean reached, long snapshotCounter, long snapshotTime,
                     long packetsGenerated, long virtualTime, boolean abortedByLimit) {
        this.reached = reached;
        this.snapshotCounter = snapshotCounter;
        this.snapshotTime = snapshotTime;
        this.packetsGenerated = packetsGenerated;
        this.virtualTime = virtualTime;
        this.abortedByLimit = abortedByLimit;
    }

    public boolean isReached() {
        return reached;
    }

    public long getSnapshotCounter() {
        return snapshotCounter;
    }

    public long getSnapshotTime() {
        return snapshotTime;
    }

    public long getPacketsGenerated() {
        return packetsGenerated;
    }

    public long getVirtualTime() {
        return virtualTime;
    }

    public boolean isAbortedByLimit() {
        return abortedByLimit;
    }
}
