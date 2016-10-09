package ar.edu.itba.protos.Proxy.Connection;


import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by sebastian on 10/9/16.
 */
public interface Connection {

    public ByteBuffer getWriteBuffer();

    public void setWriteBuffer (ByteBuffer writeBuffer);

    public ByteBuffer getReadBuffer();

    public void setReadBuffer (ByteBuffer readBuffer);

    public SelectionKey getClientKey();

    public SelectionKey getServerKey();

    public void setClientChannel(SocketChannel channel);

    public void setServerChannel(SocketChannel channel);

    public void endConnection();
}
