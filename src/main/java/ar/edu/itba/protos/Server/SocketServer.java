// Adapted from https://examples.javacodegeeks.com/core-java/nio/java-nio-socket-example/

package ar.edu.itba.protos.Server;

import ar.edu.itba.protos.Admin.AdminHandler;
import ar.edu.itba.protos.Handlers.XMPPHandler;
import ar.edu.itba.protos.Proxy.Connection.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by sebastian on 10/18/16.
 */

public class SocketServer {
    private Selector selector;
    private InetSocketAddress listenAddress;
    private ServerSocketChannel serverChannel;
    private ServerSocketChannel adminChannel;

    AdminHandler adminHandler = new AdminHandler(selector);

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

    /**
     *
     * Creates the handlers and opens a new selector by saving the server address into an InetSocketAddress
     *
     * @param address
     * @param port
     * @throws IOException
     *
     */
    public SocketServer(String address, int port) throws IOException {
        listenAddress = new InetSocketAddress(address, port);
        this.selector = Selector.open();
        this.xmppHandler = new XMPPHandler(this.selector);

        // Starting admin
        this.adminChannel = ServerSocketChannel.open();
        this.adminChannel.socket().bind(new InetSocketAddress("localhost", 5224));
        this.adminChannel.configureBlocking(false);
        this.adminChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        this.adminHandler.setChannel(this.adminChannel);
    }

    /**
     *
     * Properly starts the server, opening a server channel configuring it's blocking to false and creating a socket
     * that is bind to the listenAdress.
     *
     * Runs a while true that checks for new keys. If the new key is acceptable then it handles the accept, else handles
     * the read.
     *
     * @throws IOException
     */
    private void startServer() throws IOException {

        System.out.println("Server started...");

        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // retrieve server socket and bind to port
        serverChannel.socket().bind(listenAddress);
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);


        while (!Thread.interrupted()) {
            // wait for events
            if (this.selector.select(3000) == 0)
                continue;

            //work on selected keys
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();

                // this is necessary to prevent the same key from coming up
                // again the next time around.


                System.out.println(key);
                if (!key.isValid()) {
                    continue;
                }


                // FIXME: Check for different connections
                if (key.isAcceptable()) {

                    if (key.channel() == this.adminChannel){
                        System.out.println("something");
                        this.adminHandler.accept(key, this.selector);
                    } else {
                        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
                        this.accept(key, this.selector);
                    }

                    //this.handlers.put((SelectionKey)((SortedSet)key.selector().keys()).last(), new XMPPHandler(this.selector));
                    //System.out.println("A ver el Key Channel" + (SelectionKey)((SortedSet)key.selector().keys()).last());

                }
                else if (key.isReadable()) {
                    ConnectionImpl aa =  (ConnectionImpl) key.attachment();
                    //System.out.println("A ver el Key Channel 2" + (SelectionKey)((SortedSet)key.selector().keys()).last());
                    //this.handlers.get((SelectionKey)((SortedSet)key.selector().keys()).last()).read(key);

                    if (key.channel() == this.adminChannel){
                        System.out.println("something");
                        xmppHandler.read(key);
                    } else {
                        this.adminHandler.read(key);
                    }

                }
                keys.remove();
            }
        }
    }



    /**
     *
     * Accepts the newly requested connection through the acceptable key.
     *
     * @param key
     * @param selector
     * @throws IOException
     */
    private void accept(SelectionKey key, Selector selector) throws IOException{

        ConnectionImpl actualConnection = ((ConnectionImpl)xmppHandler.handleAccept(key, selector));
        actualConnection.setServerChannel(serverChannel.accept());


        connections.put(actualConnection.getClientChannel(), actualConnection);
    }

}