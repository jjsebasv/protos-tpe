package ar.edu.itba.protos.Handlers;

import ar.edu.itba.protos.Protocols.DefaultTCP;
import ar.edu.itba.protos.Proxy.Connection.ConnectionImpl;
import ar.edu.itba.protos.Stanza.Stanza;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sebastian on 10/9/16.
 */
public class XMPPHandler extends DefaultHandler {

    /**
     * This are the Prosody Server configurations
     * ------
     * Is needed to add 'protos-server' to de hosts file
     * Relate it to localhost (it's just a name) -> We could change it
     */
    private static final int CONNECT_PORT = 5228;
    private static final String CONNECT_SERVER = "protos-tpe";

    public List<Stanza> stanzas = new LinkedList<>();;
    public Stanza actualStanza = null;


    /*
     * Why do we configureBlocking(false)?
     * ------
     * A ServerSocketChannel can be set into non-blocking mode.
     * In non-blocking mode the accept() method returns immediately, and may thus return null, if no incoming connection had arrived.
     * Therefore we will have to check if the returned SocketChannel is null.
     */
    public SocketChannel handleAccept(SelectionKey key) throws IOException {
        ConnectionImpl actualConnection = new ConnectionImpl();

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();

        Socket socket = clientChannel.socket();
        // FIXME: This should be logged
        System.out.println("Connected to: " + socket.getRemoteSocketAddress());


        clientChannel.configureBlocking(false);

        actualConnection.setClientChannel(clientChannel);
        actualConnection.setReadBuffer(ByteBuffer.allocate(1025*100));
        // Why does it doesn't accept OP_ACCEPT
        clientChannel.register(key.selector(), SelectionKey.OP_READ, actualConnection);

        return clientChannel;
    }

    public String handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ConnectionImpl actualConnection;
        if(key.attachment() != null) {
            actualConnection = (ConnectionImpl) key.attachment();
        } else {
            actualConnection = new ConnectionImpl();
        }

        System.out.println("--> " + key.attachment());
        ByteBuffer readBuffer = ByteBuffer.allocate(1024*100);

        long bytesRead = clientChannel.read(readBuffer);

        // FIXME: Is the same if bytesRead is 0 and -1?
        if(bytesRead == -1) {
            clientChannel.close();
            return "error";
        } else {
            System.out.println("esto aca en el HANDLE READ");

            String answer = actualConnection.process(bytesRead, clientChannel, readBuffer);
            System.out.println(answer);
            return answer;
            /*
            // TODO: Handle read
            Runnable command = new Runnable() {
                public void run() {
                    System.out.println("Entre en el run");
                    try {
                        if (bytesRead > 0) {
                            final String answer = actualConecction.process(bytesRead, clientChannel,readBuffer);
                            actualConecction.setServerName(answer);
                        } else if (bytesRead == -1) {
                            key.cancel();
                        }
                    } catch (Exception e) {
                        // FIXME: This should be in a logger
                        System.out.println("Error when reading");
                        key.cancel();
                    }
                }
            };
            new Thread(command).start();
            */
        }
    }

    public void handleWrite(SelectionKey key, String s) throws IOException {
        SocketChannel clientChannel = (SocketChannel)key.channel();
        ConnectionImpl actualConnection = (ConnectionImpl) key.attachment();
        ByteBuffer writeBuffer = ByteBuffer.wrap(s.getBytes());
        long writtenBytes;

        if(clientChannel.isOpen()) {
            writtenBytes = clientChannel.write(writeBuffer);
            // TODO: Handle write

            System.out.println("Escribiendo al  xmpp..");
            writeBuffer.clear();

        } else {
            actualConnection.endConnection();
        }
    }

    /*
     * Private Functions
     */

}
