package ar.edu.itba.protos.Proxy.Metrics;

/**
 * Created by sebastian on 11/14/16.
 */
public class Metrics {

    private long totalAccesses;
    private long receivedBytes;
    private long transferedBytes;
    private long blockedMessages;
    private long convertedCharacters;
    private static Metrics instance;

    private Metrics() {
        this.totalAccesses = 0;
        this.receivedBytes = 0;
        this.transferedBytes = 0;
        this.blockedMessages = 0;
        this.convertedCharacters = 0;
    }

    public static Metrics getInstance() {
        if (instance == null) {
            instance = new Metrics();
        }
        return instance;
    }

    public long getTotalAccesses() {
        return totalAccesses;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public long getTransferedBytes() {
        return transferedBytes;
    }

    public long getBlockedMessages() {
        return blockedMessages;
    }

    public long getConvertedCharacters() {
        return convertedCharacters;
    }

    public void addAccess() {
        this.totalAccesses++;
    }

    public void addReceivedBytes(long receivedBytes) {
        this.receivedBytes += receivedBytes;
    }

    public void addTransferedBytes(long transferedBytes) {
        this.transferedBytes += transferedBytes;
    }

    public void addBlockedMessages() {
        this.blockedMessages++;
    }

    public void addConvertedCharacter() {
        this.convertedCharacters ++;
    }

}
