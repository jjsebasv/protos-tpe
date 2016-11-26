package ar.edu.itba.protos.Proxy.Connection;

import ar.edu.itba.protos.Logger.XmppLogger;
import ar.edu.itba.protos.Proxy.Metrics.Metrics;
import ar.edu.itba.protos.Stanza.Stanza;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


/**
 * Created by sebastian on 10/9/16.
 */
public class ConnectionImpl implements Connection {

    private Selector selector;

    private SocketChannel clientChannel;
    private SocketChannel serverChannel;

    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;

    private SelectionKey clientKey;
    private SelectionKey serverKey;

    private String serverName;
    private String clientName;

    private boolean applyLeet = false;

    public ByteBuffer onlyBuffer;

    public Stanza stanza;

    private XmppLogger logger = XmppLogger.getInstance();

    public ConnectionImpl(Selector selector) {
        this.selector = selector;
    }

    /**
     * Ends a connection and close both the client and the server channel
     */
    public void endConnection() {
        if (this.clientChannel != null) {
            try {
                clientChannel.close();
            } catch (IOException e) {
                // TODO: catch this exception
            }
        }
        if (this.serverChannel != null) {
            try {
                serverChannel.close();
            } catch (IOException e) {
                // TODO: catch this exception
            }
        }
    }

    /**
     * Sets the server name
     *
     * @param serverName
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Sets the client name
     *
     * @param clientName
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public void setClientKey(SelectionKey key) {
        this.clientKey = key;
    }

    public SelectionKey getServerKey() {
        return this.serverKey;
    }

    public void setClientChannel(SocketChannel channel) {
        this.clientChannel = channel;
    }

    public SocketChannel getClientChannel() {
        return this.clientChannel;
    }

    public SocketChannel getServerChannel() {
        return this.serverChannel;
    }

    public void setServerChannel(SocketChannel channel) {
        this.serverChannel = channel;
    }

    public String getServerName() {
        return this.serverName;
    }

    public void enableLeet() {
        this.applyLeet = true;
    }

    public void disableLeet() {
        this.applyLeet = false;
    }

    public boolean applyLeet() {
        return this.applyLeet;
    }

    public Selector getSelector() {
        return this.selector;
    }

    /**
     * Processes the mesage and decides whether it should be sent to the server or to the client.
     *
     * Once that decision is made, it writes down the message to the corresponding channel
     *
     * @param message
     * @param toWhom
     */
    public void processWrite(String message, String toWhom) {
        SocketChannel channel;
        switch (toWhom) {
            case "server":
                channel = this.serverChannel;
                break;
            // default is toWhom == "client"
            default:
                channel = this.clientChannel;
                break;
        }

        this.onlyBuffer = ByteBuffer.wrap(message.getBytes());

        try {
            Metrics.getInstance().addTransferedBytes(channel.write(this.onlyBuffer));
        } catch (IOException e) {
            // TODO: Handle Exception and log it
            logger.error("Error while writing");
            System.out.println(e);
        }

        logger.info("Writing to " + toWhom);
        this.onlyBuffer.clear();

    }
}