package ar.edu.itba.protos.Handlers;

import ar.edu.itba.protos.Logger.XmppLogger;
import ar.edu.itba.protos.Proxy.Connection.Connection;
import ar.edu.itba.protos.Proxy.Connection.ConnectionImpl;
import ar.edu.itba.protos.Proxy.Filters.Blocker;
import ar.edu.itba.protos.Proxy.Filters.Conversor;
import ar.edu.itba.protos.Stanza.Stanza;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

    private static final int DEFAULT_BUFFER_SIZE = 1024*100;

    public List<Stanza> stanzas = new LinkedList<>();;
    public Stanza actualStanza = null;

    private ConnectionImpl actualConnection;


    XmppLogger logger = XmppLogger.getInstance();

    public XMPPHandler(Selector selector) {
        this.actualConnection = new ConnectionImpl(selector);
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

        System.out.println("TESTER: " +  ((ServerSocketChannel)key.channel()).socket().getLocalSocketAddress());

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        Socket socket = clientChannel.socket();

        // FIXME: This should be logged
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

        SocketChannel channel = (SocketChannel) key.channel();
        int read = -1;
        read = channel.read(buffer);

        // TODO: What's the difference bet 0 and 1?
        if (read == -1) {
            logger.info("Connection clossed by " + channel.socket().getRemoteSocketAddress());
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[read];
        System.arraycopy(buffer.array(), 0, data, 0, read);
        String stringRead = new String(data);
        String toSendString = stringRead;
        logger.info("Message received: " + stringRead);

        Document doc = Jsoup.parse(stringRead, "UTF-8", Parser.xmlParser());

        // This is to check if the message has a body, hence if its a message
        if (doc != null && doc.body() != null) {
            if(Conversor.applyLeet) {
                // TODO: Should use Stanzas here?
                toSendString = doc.text(Conversor.apply(doc.text()).toString()).toString();
            }
            shouldSend = !Blocker.apply(doc.toString());
        }

        if(shouldSend){
            handleSendMessage(toSendString, channel);
        }

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
        writeInChannel(s, this.actualConnection.getServerChannel());
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
        this.actualConnection.processWrite(s, this.actualConnection.getClientChannel() == channel ? "client" : "server");
    }

    /**
     *
     * Decides wheter the message should be sent to the server or to the client.
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

}
