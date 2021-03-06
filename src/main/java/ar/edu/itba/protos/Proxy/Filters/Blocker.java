package ar.edu.itba.protos.Proxy.Filters;

import ar.edu.itba.protos.Logger.XmppLogger;
import ar.edu.itba.protos.Proxy.Metrics.Metrics;
import ar.edu.itba.protos.Stanza.Stanza;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebastian on 10/24/16.
 */
public class Blocker {

    static XmppLogger logger = XmppLogger.getInstance();

    //TODO: Where should be this?
    private static List<String> blockedUsers = new ArrayList<>();

    public static List<String> getBlockedUsers() {return blockedUsers;}

    /**
        Receives an xml string and decides if the message should be sent.
        If the message contains either in the from or the to a blocked
        user, then the function returns true.

        @return whether the message should be blocked or not
        @param stanza the whole stanza
     **/

    public static boolean apply(Stanza stanza) {
        String s = stanza.getXml();
        String fromUser = null;
        String toUser = null;

        int fromIndex = s.indexOf("from=");
        int untilFrom = s.indexOf('/', fromIndex);
        if (fromIndex != -1 && untilFrom != -1) {
            fromUser = s.substring(fromIndex+6, untilFrom);
        }

        int toIndex = s.indexOf("to=");
        int untilTo = s.indexOf(' ', toIndex);
        if (fromIndex != -1 && untilTo != -1) {
            toUser = s.substring(toIndex+4, untilTo-1);
        }

        for (String user : blockedUsers) {
            System.out.println(user);
            System.out.println("from: " + fromUser);
            System.out.println("to: " + toUser);
            if (fromUser != null) {
                if (user.equals(fromUser)) {
                    block(user);
                    stanza.setBlockFrom(true);
                    return true;
                }
            }

            if (toUser != null) {
                int serverIndex = toUser.indexOf('/');
                if (serverIndex != -1) {
                    toUser = toUser.substring(0, serverIndex);
                }
                if (user.equals(toUser)) {
                    block(user);
                    stanza.setBlockTo(true);
                    return true;
                }
            }
        }
        return false;
    }

    private static void block(String user) {
        logger.info("blocking message due to user " + user);
        System.out.println("TENEMOS QUE BLOCKEAR");
        Metrics.getInstance().addBlockedMessages();
    }

    public static void remove(String s) {
        s = s.replace("\n", "").replace("\r", "");
        if(blockedUsers.indexOf(s) >= 0) {
            logger.info("removing " + s + " from the blocking list");
            blockedUsers.remove(s);
        }
    }

    public static void add (String s) {
        s = s.replace("\n", "").replace("\r", "");
        if(blockedUsers.indexOf(s) == -1) {
            logger.info("adding " + s + " to the blocking list");
            blockedUsers.add(s);
        }
    }
}
