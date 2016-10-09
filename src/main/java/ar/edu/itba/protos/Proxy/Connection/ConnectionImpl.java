package ar.edu.itba.protos.Proxy.Connection;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by sebastian on 10/9/16.
 */
public class ConnectionImpl implements Connection {

    private SocketChannel clientChannel;
    private SocketChannel serverChannel;

    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;

    private SelectionKey clientKey;
    private SelectionKey serverKey;

    public void endConnection() {
        if(this.clientChannel != null) {
            try {
                clientChannel.close();
            } catch (IOException e) {
                // TODO: catch this exception
            }
        }
        if(this.serverChannel != null) {
            try {
                serverChannel.close();
            } catch (IOException e) {
                // TODO: catch this exception
            }
        }
    }

    public ByteBuffer getWriteBuffer() {
        return this.writeBuffer;
    }

    public ByteBuffer getReadBuffer() {
        return this.readBuffer;
    }

    public void setReadBuffer(ByteBuffer readBuffer) {
        this.readBuffer = readBuffer;
    }

    public void setWriteBuffer(ByteBuffer writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    public SelectionKey getClientKey() {
        return this.clientKey;
    }

    public SelectionKey getServerKey() {
        return this.serverKey;
    }

    public void setClientChannel(SocketChannel channel) {
        this.clientChannel = channel;
    }

    public void setServerChannel(SocketChannel channel) {
        this.serverChannel = channel;
    }
}
