package ar.edu.itba.protos.Handlers;

import ar.edu.itba.protos.Protocols.DefaultTCP;
import ar.edu.itba.protos.Proxy.Connection.ConnectionImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by sebastian on 10/9/16.
 */
public class XMPPHandler implements DefaultTCP {

    /**
     * This are the Prosody Server configurations
     * ------
     * Is needed to add 'protos-server' to de hosts file
     * Relate it to localhost (it's just a name) -> We could change it
     */
    private static final int CONNECT_PORT = 5228;
    private static final String CONNECT_SERVER = "protos-tpe";


    /*
     * Why do we configureBlocking(false)?
     * ------
     * A ServerSocketChannel can be set into non-blocking mode.
     * In non-blocking mode the accept() method returns immediately, and may thus return null, if no incoming connection had arrived.
     * Therefore we will have to check if the returned SocketChannel is null.
     */
    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel = ((ServerSocketChannel)key.channel()).accept();
        clientChannel.configureBlocking(false);

        ConnectionImpl actualConnection = new ConnectionImpl();
        actualConnection.setClientChannel(clientChannel);
        clientChannel.register(key.selector(), SelectionKey.OP_ACCEPT, actualConnection);
    }

    public void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ConnectionImpl actualConecction = (ConnectionImpl) key.attachment();
        ByteBuffer readBuffer = actualConecction.getReadBuffer();

        long bytesRead = clientChannel.read(readBuffer);

        // FIXME: Is the same if bytesRead is 0 and -1?
        if(bytesRead == -1) {
            clientChannel.close();
        } else {
            // TODO: Handle read
        }

    }

    public void handleWrite(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel)key.channel();
        ConnectionImpl actualConnection = (ConnectionImpl) key.attachment();
        ByteBuffer writeBuffer = actualConnection.getWriteBuffer();
        long writtenBytes;

        if(clientChannel.isOpen()) {
            writtenBytes = clientChannel.write(writeBuffer);
            // TODO: Handle write
        } else {
            actualConnection.endConnection();
        }
    }
}
