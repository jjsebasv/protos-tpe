// Adapted from https://examples.javacodegeeks.com/core-java/nio/java-nio-socket-example/

package ar.edu.itba.protos.Server;

import ar.edu.itba.protos.Handlers.XMPPHandler;
import ar.edu.itba.protos.Proxy.Connection.*;
import ar.edu.itba.protos.Proxy.Filters.Conversor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

/**
 * Created by sebastian on 10/18/16.
 */

public class SocketServer {
    private Selector selector;
    private InetSocketAddress listenAddress;
    private SocketChannel clientOfXmppServer;
    private SocketChannel pidginChannel;

    XMPPHandler xmppHandler;

    Map<SocketChannel, ConnectionImpl> connections = new HashMap<>();

    public static void main(String[] args) throws Exception {

        Runnable server = new Runnable() {
            public void run() {
                try {
                    new SocketServer("localhost", 5223).startServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        new Thread(server).start();
    }

    public SocketServer(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
        this.selector = Selector.open();
        this.xmppHandler = new XMPPHandler();
    }

    // create server channel
    private void startServer() throws IOException {

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started...");

        while (true) {
            // wait for events
            this.selector.select();

            //work on selected keys
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();

                // this is necessary to prevent the same key from coming up
                // again the next time around.
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }


                // FIXME: Check for different connections
                if (key.isAcceptable()) {
                    this.accept(key, this.selector);
                    //this.handlers.put((SelectionKey)((SortedSet)key.selector().keys()).last(), new XMPPHandler(this.selector));
                    //System.out.println("A ver el Key Channel" + (SelectionKey)((SortedSet)key.selector().keys()).last());

                }
                else if (key.isReadable()) {
                    Object aa = key.attachment();
                    //System.out.println("A ver el Key Channel 2" + (SelectionKey)((SortedSet)key.selector().keys()).last());
                    //this.handlers.get((SelectionKey)((SortedSet)key.selector().keys()).last()).read(key);
                    xmppHandler.read(key);
                }
            }
        }
    }

    private void accept(SelectionKey key, Selector selector) throws IOException{
        //System.out.println("El seletor de la key: " + key.selector());
        ConnectionImpl actualConnection = ((ConnectionImpl)xmppHandler.handleAccept(key, selector));
        connections.put(actualConnection.getClientChannel(), actualConnection);
        pidginChannel = actualConnection.getClientChannel();
    }

}