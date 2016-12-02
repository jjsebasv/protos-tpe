package ar.edu.itba.protos.Proxy.Connection;

import ar.edu.itba.protos.Logger.XmppLogger;
import ar.edu.itba.protos.Proxy.Metrics.Metrics;
import ar.edu.itba.protos.Stanza.Stanza;

import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


/**
 * Created by sebastian on 10/9/16.
 */
public class ConnectionImpl implements Connection {

    private static final int DEFAULT_BUFFER_SIZE = 1024*100;

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
        this.onlyBuffer = ByteBuffer.wrap(new byte[DEFAULT_BUFFER_SIZE]);
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
        if (this.clientChannel.keyFor(this.selector) == null) {
            return null;
        }
        return this.clientChannel.keyFor(this.selector);
    }

    public void setClientKey(SelectionKey key) {
        this.clientKey = key;
    }

    public SelectionKey getServerKey() {
        if (this.serverChannel.keyFor(this.selector) == null) {
            return null;
        }
        return this.serverChannel.keyFor(this.selector);
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
     * @param toWhom
     */
    public void processWrite(String toWhom, boolean isAccepted) {
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
        try {
            if (!isAccepted) {
                String oldxml = stanza.getXml();
                stanza.showBlockMessage();
                stanza.transformXml();
                String newxml = stanza.getXml();
                System.out.println("********************************** " + oldxml.equals(newxml));
                this.onlyBuffer.clear();
                this.onlyBuffer = ByteBuffer.wrap(stanza.getXml().getBytes());
            }
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