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

    private ConnectionImpl actualConnection;


    XmppLogger logger = XmppLogger.getInstance();

    public XMPPHandler(Selector selector, int port, String server) {
        this.actualConnection = new ConnectionImpl(selector);
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
    *
    * @param key
    * @param selector
    *
    */
    public Connection handleAccept(SelectionKey key, Selector selector) throws IOException {
        ConnectionImpl connection = new ConnectionImpl(selector);
        Metrics.getInstance().addAccess();

        System.out.println("TESTER: " +  ((ServerSocketChannel)key.channel()).socket().getLocalSocketAddress());

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        Socket socket = clientChannel.socket();

        logger.info("Connected to: " + socket.getRemoteSocketAddress());

         /*
         * Why do we configureBlocking(false)?
         * ------
         * A ServerSocketChannel can be set into non-blocking mode.
         * In non-blocking mode the accept() method returns immediately, and may thus return null, if no incoming connection had arrived.
         * Therefore we will have to check if the returned SocketChannel is null.
         */
        clientChannel.configureBlocking(false);

        // Why does it doesn't accept OP_ACCEPT
        clientChannel.register(key.selector(), SelectionKey.OP_READ, connection);

        connection.setClientChannel(clientChannel);
        connection.setClientKey(key);
        connection.setWriteBuffer(ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));

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


        if (key.attachment() != null) {
            this.actualConnection = (ConnectionImpl) key.attachment();
        }

        boolean shouldSend = true;

        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        this.actualConnection.onlyBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        this.actualConnection.setReadBuffer(ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));

        SocketChannel channel = (SocketChannel) key.channel();
        int read = -1;

        //read = channel.read(buffer);
        read = channel.read(this.actualConnection.getReadBuffer());

        // TODO: What's the difference bet 0 and 1?
        if (read == -1) {
            logger.warn("Connection clossed by " + channel.socket().getRemoteSocketAddress());
            channel.close();
            key.cancel();
            return;
        } else if (read > 0) {

            Metrics.getInstance().addReceivedBytes(read);

            byte[] data = new byte[read];
            //System.arraycopy(buffer.array(), 0, data, 0, read);
            System.arraycopy(this.actualConnection.getReadBuffer().array(), 0, data, 0, read);
            String stringRead = new String(data);

            Stanza stanza = new Stanza(stringRead);
            if (stanza.isChat()) {
                manageBlockAndConvert(stanza);
            }

            String toSendString = stanza.getXml();

            logger.info("Message received: " + stringRead);
            actualConnection.stanza = stanza;


            this.actualConnection.setReadBuffer(ByteBuffer.wrap(stanza.getXml().getBytes()));

            String sss = new String(this.actualConnection.getReadBuffer().array(), "UTF-8");
            System.out.println(sss);

            if(stanza.isAccepted()) {
                //System.arraycopy(buffer.array(), 0, actualConnection.onlyBuffer, 0, read);
                //key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                handleSendMessage(toSendString, channel);
            }

        } else {
            actualConnection.endConnection();
        }
    }

    public void write(SelectionKey key, Iterator<SelectionKey> a) {
        if (key.attachment() != null) {
            this.actualConnection = (ConnectionImpl) key.attachment();
        }

        ByteBuffer writeBuffer = this.actualConnection.getWriteBuffer();

        if (!writeBuffer.hasRemaining()) {
            writeBuffer.clear();
            writeBuffer.flip();
            if (!writeBuffer.hasRemaining()) {
                key.interestOps(SelectionKey.OP_READ);
                return;
            }
        }

        System.out.println("writing to server: " + this.actualConnection.getClientChannel());
        String s = new String(writeBuffer.array());
        System.out.println(s.substring(writeBuffer.position(), writeBuffer.limit()));
        System.out.println();

        handleSendMessage(this.actualConnection.stanza.getXml(), (SocketChannel)key.channel());

        /*
        try {
            ((SocketChannel) key.channel()).write(writeBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }


        SelectionKey key1 = this.actualConnection.getClientKey();
        SelectionKey key2 = this.actualConnection.getServerKey();
        SelectionKey key3 = key;
        handleSendMessage(((ConnectionImpl) key.attachment()).staza.getXml(), (SocketChannel) key.channel());

        ByteBuffer buffer = ((ConnectionImpl)key.attachment()).onlyBuffer;
        try {
            ((SocketChannel) key.channel()).write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buffer.clear();
        */

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
     * @param s
     * @throws IOException
     *
     */
    public void sendToServer(String s) throws IOException {
        if (this.actualConnection.getServerChannel() == null) {
            InetSocketAddress hostAddress = new InetSocketAddress(CONNECT_SERVER, CONNECT_PORT);
            this.actualConnection.setServerChannel(SocketChannel.open(hostAddress));
        }
        String sss = new String(this.actualConnection.getReadBuffer().array(), "UTF-8");
        System.out.println(sss);
        System.out.println(s);
        writeInChannel(sss, this.actualConnection.getServerChannel());
        this.actualConnection.getServerChannel().configureBlocking(false);
        this.actualConnection.getServerChannel().register(this.actualConnection.getSelector(), SelectionKey.OP_READ);
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
     * @param s
     * @throws IOException
     *
     */
    public void sendToClient(String s) throws IOException {
        writeInChannel(s, this.actualConnection.getClientChannel());
    }

    // Private functions


    /**
     *
     * Writes the specified message through the specified channel
     *
     * @param s
     * @param channel
     *
     */
    private void writeInChannel(String s, SocketChannel channel) {
            this.actualConnection.processWrite(s, this.actualConnection.getClientChannel() == channel ? "client" : "server", this.actualConnection);
    }

    /**
     *
     * Decides whether the message should be sent to the server or to the client.
     *
     * @param message
     * @param channel
     *
     */
    private void handleSendMessage(String message, SocketChannel channel) {
        try {
            if (channel == this.actualConnection.getServerChannel()) {
                sendToClient(message);
            } else {
                sendToServer(message);
            }
        } catch (IOException e) {
            logger.error("Error found in sending message");
        }

    }

    private void manageBlockAndConvert(Stanza stanza) {

        if(Conversor.applyLeet) {
            // TODO: Should use Stanzas here?
            Conversor.convert(stanza);
        }
        stanza.setAccepted(!Blocker.apply(stanza.getXml()));
    }

}
