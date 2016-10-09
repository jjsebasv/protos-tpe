package ar.edu.itba.protos.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * Created by sebastian on 10/3/16.
 */
public class TCPSelector {
    public static final int BUFSIZE = 512; // Buffer size (bytes)
    private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
    private static final int PORT_XMPP = 8085;
    private static final int PORT_PCP = 8086;

    public static void main(String[] args) throws IOException {

    }

}
