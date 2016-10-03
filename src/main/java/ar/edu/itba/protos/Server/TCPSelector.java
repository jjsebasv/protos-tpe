package ar.edu.itba.protos.Server;

import ar.edu.itba.protos.Protocols.CustomTCP;
import handler.HPPClientHandler;
import handler.TCPProtocol;
import util.Attachment;
import util.ProxyAttachment;
import util.ProxyConfigurator;

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
        if (args.length < 1) {
            throw new IllegalArgumentException("Parameter(s): <Port> ...");
        }

        Selector selector = Selector.open();

//		TCPProtocol handler = new XMPPHandler(BUFSIZE);
        addServerSocketChannel(selector, PORT_XMPP, ProxyAttachment.clientXmpp(configurator, handler, BUFSIZE, null));
        addServerSocketChannel(selector, PORT_PCP, Attachment.pcp(configurator, BUFSIZE));

        while (true) {
            if (selector.select(TIMEOUT) == 0) {
                System.out.println(".");
                continue;
            }

            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
            while(keyIter.hasNext()) {
                SelectionKey key = keyIter.next();

                if (key.isAcceptable()) {
                    ((Attachment) key.attachment()).handler().handleAccept(key);
                }

                if (key.isValid() && key.isWritable()) {
                    ((Attachment) key.attachment()).handler().handleWrite(key);
                }

                if (key.isReadable()) {
                    ((Attachment) key.attachment()).handler().handleRead(key);
                }

                keyIter.remove();
            }
        }
    }

    private static void addServerSocketChannel(Selector selector, int port, Attachment attachment) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector,  SelectionKey.OP_ACCEPT, attachment);
    }

}
