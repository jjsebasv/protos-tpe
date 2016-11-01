package ar.edu.itba.protos.Proxy.Filters;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by seguido on 31/10/16.
 */
public class Multiplexer {

    public static Map<String,String> mplexer= new HashMap<>();

    public static void multiplex(String user, String server){
        mplexer.put(user,server);
    }

    public static void unplex(String user){
        mplexer.remove(user);
    }

}
