package ar.edu.itba.protos.Logger;
/**
 * Created by seguido on 15/11/16.
 */
public class Metrics {
    private static long sentBytes = 0;
    private static long receivedBytes = 0;
    private static int messagesBlocked = 0;
    private static int adminConnections = 0;
    private static int blockResponses = 0;

    private static Metrics ins = null;

    public Metrics getInstance(){
        if (ins == null){
            return ins=new Metrics();
        }else{
            return ins;
        }
    }

    public long getSentBytes(){
        return sentBytes;
    }

    public void incSentBytes(long add){
        sentBytes+=add;
    }

    public long getReceivedBytes(){
        return receivedBytes;
    }

    public void incReceivedBytes(long add){
        receivedBytes+=add;
    }

    public int getAdminConnections(){
        return adminConnections;
    }

    public void incAdminConnections(){
        adminConnections++;
    }

    public int getMessagesBlocked(){
        return messagesBlocked;
    }

    public void incMessagesBlocked(){
        messagesBlocked++;
    }

    public int getBlockResponses(){
        return blockResponses;
    }

    public void incBlockRespones(){
        blockResponses++;
    }

}
