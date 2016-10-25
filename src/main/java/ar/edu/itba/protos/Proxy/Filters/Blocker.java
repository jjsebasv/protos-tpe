package ar.edu.itba.protos.Proxy.Filters;

import ar.edu.itba.protos.Logger.XmppLogger;

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

    public static boolean apply(String s) {
        String fromUser;
        String toUser;

        int fromIndex = s.indexOf("from=");
        int untilFrom = s.indexOf('/', fromIndex);

        int toIndex = s.indexOf("to=");
        int untilTo = s.indexOf('/', toIndex);

        fromUser = s.substring(fromIndex+6, untilFrom);
        toUser = s.substring(toIndex+4, untilTo);
        for (String user : blockedUsers) {
            System.out.println(user);
            System.out.println("from: " + fromUser);
            System.out.println("to: " + toUser);
            if (user.equals(fromUser) || user.equals(toUser)) {
                System.out.println("TENEMOS QUE BLOCKEAR");
                return true;
            }
        }
        return false;
    }

    public static void remove(String s) {
        if(blockedUsers.indexOf(s) >= 0){
            logger.info("removing " + s + " from the blocking list");
            blockedUsers.remove(s);
        }
    }

    public static void add (String s) {
        if(blockedUsers.indexOf(s) == -1){
            logger.info("adding " + s + " to the blocking list");
            blockedUsers.add(s);
        }
    }
}
