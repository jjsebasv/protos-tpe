package ar.edu.itba.protos.Handlers;

import ar.edu.itba.protos.Logger.XmppLogger;
import ar.edu.itba.protos.Proxy.Connection.Connection;
import ar.edu.itba.protos.Proxy.Connection.ConnectionImpl;
import ar.edu.itba.protos.Proxy.Filters.Blocker;
import ar.edu.itba.protos.Proxy.Filters.Conversor;
import ar.edu.itba.protos.Proxy.Metrics.Metrics;
import ar.edu.itba.protos.Stanza.Stanza;

import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
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
    private static int CONNECT_PORT;
    private static String CONNECT_SERVER;

    private static final int DEFAULT_BUFFER_SIZE = 1024*100;

    public List<Stanza> stanzas = new LinkedList<>();;
    public Stanza actualStanza = null;


    XmppLogger logger = XmppLogger.getInstance();

    public XMPPHandler(Selector selector, int port, String server) {
        this.CONNECT_PORT = port;
        this.CONNECT_SERVER = server;
    }


   /**
    *
    * Handles incoming connections to client port
    *
    * Handles the received key - which is acceptable - and creates the clientChannel
    *
    * Registers the key with a ConnectionImpl that afterwards will be accessed by the
    * attachment field.
    * It uses the selector inside the key.
    *
    * @param key
    *
    */
    public Connection handleAccept(SelectionKey key) throws IOException {
        ConnectionImpl connection = new ConnectionImpl(key.selector());
        Metrics.getInstance().addAccess();

        System.out.println("TESTER: " +  ((ServerSocketChannel)key.channel()).socket().getLocalSocketAddress());

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        Socket socket = clientChannel.socket();

        logger.info("Connected to: " + socket.getRemoteSocketAddress());

        clientChannel.configureBlocking(false);

        connection.setClientChannel(clientChannel);
        connection.setClientKey(key);

        SelectionKey clientKey = clientChannel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
        clientKey.attach(connection);
        return connection;
    }

    /**
     * Handles incoming reads from both server and clients
     *
     * Handles the received key - which is readable - and gets its channel that afterwards will
     * be compared with the connection's client channel. If equal, the message will be sent to
     * the client, else to the server.
     *
     * Saves the key connection to the actualConnection variable, every other function called in
     * this scope will be using that instance of ConnectionImpl.
     *
     * If the blocking or the filter are turned on, the handler will deal with it.
     *
     *  @param key
     *
     */
    public void read(SelectionKey key) throws IOException {
        ConnectionImpl connection = (ConnectionImpl) key.attachment();

        SocketChannel channel = (SocketChannel) key.channel();
        int read = -1;

        connection.onlyBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);;
        read = channel.read(connection.onlyBuffer);

        // TODO: What's the difference bet 0 and 1?
        if (read == -1) {
            logger.warn("Connection clossed by " + channel.socket().getRemoteSocketAddress());
            channel.close();
            key.cancel();
            return;
        } else if (read > 0) {
            key.interestOps(SelectionKey.OP_WRITE);
            Metrics.getInstance().addReceivedBytes(read);

            byte[] data = new byte[read];
            System.arraycopy(connection.onlyBuffer.array(), 0, data, 0, read);
            String stringRead = new String(data);

            Stanza stanza = new Stanza(stringRead);
            if (stanza.isChat()) {
                manageBlockAndConvert(stanza);
            }

            logger.info("Message received: " + stringRead);
            connection.stanza = stanza;

            connection.onlyBuffer = ByteBuffer.wrap(stanza.getXml().getBytes());

            if(stanza.isAccepted()) {
                //System.arraycopy(buffer.array(), 0, actualConnection.onlyBuffer, 0, read);
                //key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                //handleSendMessage(channel);
            }

        } else {
            connection.endConnection();
        }
    }

    public void write(SelectionKey key) throws IOException {
        ConnectionImpl connection = (ConnectionImpl) key.attachment();

        SocketChannel channel = (SocketChannel) key.channel();
        SocketChannel clientChannel = connection.getClientChannel();
        SocketChannel serverChannel = connection.getServerChannel();

        if(!channel.equals(clientChannel) && !channel.equals(serverChannel) && channel.getRemoteAddress().equals(serverChannel.getRemoteAddress())) {
            connection.setServerChannel(serverChannel);
        }

        handleSendMessage(key);
        

        key.interestOps(SelectionKey.OP_READ);
        //((SocketChannel) key.channel()).write(writeBuffer);

    }


    /**
     *
     * Sends the specific string to the server.
     *
     * Checks our the connection to see if exists a server channel. If not, creates a new one.
     *
     * At this point we can be sure that no user is blocked in this channel and that the message
     * is already converted if demanded.
     *
     * @param key
     * @throws IOException
     *
     */
    public void sendToServer(SelectionKey key) throws IOException {
        ConnectionImpl connection = (ConnectionImpl) key.attachment();
        if (connection.getServerChannel() == null) {
            InetSocketAddress hostAddress = new InetSocketAddress(CONNECT_SERVER, CONNECT_PORT);
            connection.setServerChannel(SocketChannel.open(hostAddress));
        }
        connection.getServerChannel().configureBlocking(false);
        connection.processWrite("server");
        //writeInChannel(connection.getServerChannel());
        SelectionKey serverKey = connection.getServerChannel().register(connection.getSelector(), SelectionKey.OP_READ);
        serverKey.attach(connection);
    }

    /**
     *
     * Send the specific message to the client
     *
     * Checks our the connection to get the client channel.
     *
     * At this point we can be sure that no user is blocked in this channel and that the message
     * is already converted if demanded.
     *
     * @param key
     * @throws IOException
     *
     */
    public void sendToClient(SelectionKey key) throws IOException {
        ConnectionImpl connection = (ConnectionImpl) key.attachment();
        connection.processWrite("client");
        //writeInChannel(connection.getClientChannel());
    }

    // Private functions


    /**
     *
     * Writes the specified message through the specified channel
     *
     * @param channel
     *
     */

    /* FIXME or REMOVE
    private void writeInChannel(SocketChannel channel) {
        this.actualConnection.processWrite(this.actualConnection.getClientChannel() == channel ? "client" : "server");
    }
    */

    /**
     *
     * Decides whether the message should be sent to the server or to the client.
     *
     * @param key
     *
     */
    private void handleSendMessage(SelectionKey key) {
        ConnectionImpl connection = (ConnectionImpl) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel == connection.getServerChannel()) {
                sendToClient(key);
            } else {
                sendToServer(key);
            }
        } catch (IOException e) {
            logger.error("Error found in sending message");
        }

    }

    private void manageBlockAndConvert(Stanza stanza) {

        if(Conversor.applyLeet) {
            Conversor.convert(stanza);
        }
        stanza.setAccepted(!Blocker.apply(stanza.getXml()));
    }

}
