// Adapted from https://examples.javacodegeeks.com/core-java/nio/java-nio-socket-example/

package ar.edu.itba.protos.Server;

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
import java.util.Iterator;

/**
 * Created by sebastian on 10/18/16.
 */

public class SocketServer {
    private Selector selector;
    private InetSocketAddress listenAddress;
    private SocketChannel clientOfXmppServer;
    private SocketChannel adiumChannel;

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
    }

    // create server channel
    private void startServer() throws IOException {
        this.selector = Selector.open();
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

                if (key.isAcceptable()) {
                    this.accept(key);
                }
                else if (key.isReadable()) {
                    this.read(key);
                }
            }
        }
    }

    //accept a connection made to this channel's socket
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        System.out.println("Connected to: " + remoteAddr);

        // register channel with selector for further IO
        channel.register(this.selector, SelectionKey.OP_READ);
        adiumChannel = channel;
    }

    //read from the socket channel
    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024*100);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            System.out.println("Connection closed by client: " + remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);

        String stringRead = new String(data);
        System.out.println("Got: " + stringRead);

        if (channel == clientOfXmppServer) {
            sendToClient(stringRead, adiumChannel);
        } else {
            sendToXmppServer(stringRead);
        }
    }

    private void sendToClient(String s, SocketChannel channel) {
        writeInChannel(s, channel);
    }

    private void sendToXmppServer(String s) throws IOException {
        if (clientOfXmppServer == null) {
            InetSocketAddress hostAddress = new InetSocketAddress("protos-tpe", 5228);
            clientOfXmppServer = SocketChannel.open(hostAddress);
        }
        writeInChannel(s, clientOfXmppServer);
        clientOfXmppServer.configureBlocking(false);
        clientOfXmppServer.register(this.selector, SelectionKey.OP_READ);
    }

    private void writeInChannel(String s, SocketChannel channel) {

        StringBuffer sb = Conversor.apply(s);
        System.out.println("esto es lo convertido: " + sb.toString());
        ByteBuffer buffer = ByteBuffer.wrap(s.getBytes());

        try {
            System.out.print("QUIERO QUE MIRES ACA *********************************");
            System.out.println(sb.toString());
            channel.write(buffer);
        } catch (IOException e) {
            // TODO Handle the exception
            System.out.println("error");
        }
        String clientOrServer = channel == clientOfXmppServer ? "server" : "client";
        System.out.println("Escribiendo al " + clientOrServer + " xmpp..");
        buffer.clear();
    }
}