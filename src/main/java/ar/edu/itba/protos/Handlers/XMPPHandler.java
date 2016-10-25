package ar.edu.itba.protos.Handlers;

import ar.edu.itba.protos.Logger.XmppLogger;
import ar.edu.itba.protos.Protocols.DefaultTCP;
import ar.edu.itba.protos.Proxy.Connection.Connection;
import ar.edu.itba.protos.Proxy.Connection.ConnectionImpl;
import ar.edu.itba.protos.Proxy.Filters.Blocker;
import ar.edu.itba.protos.Proxy.Filters.Conversor;
import ar.edu.itba.protos.Stanza.Stanza;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import jdk.nashorn.internal.ir.Block;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    Map<SocketAddress, ConnectionImpl> connections = new HashMap<>();
    XmppLogger logger = XmppLogger.getInstance();

    /*
     * Why do we configureBlocking(false)?
     * ------
     * A ServerSocketChannel can be set into non-blocking mode.
     * In non-blocking mode the accept() method returns immediately, and may thus return null, if no incoming connection had arrived.
     * Therefore we will have to check if the returned SocketChannel is null.
     */
    public Connection handleAccept(SelectionKey key, Selector selector) throws IOException {
        ConnectionImpl connection = new ConnectionImpl(selector);

        System.out.println("TESTER: " +  ((ServerSocketChannel)key.channel()).socket().getLocalSocketAddress());

        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        Socket socket = clientChannel.socket();

        // FIXME: This should be logged
        logger.info("Connected to: " + socket.getRemoteSocketAddress());

        clientChannel.configureBlocking(false);

        // Why does it doesn't accept OP_ACCEPT
        clientChannel.register(key.selector(), SelectionKey.OP_READ, connection);

        connection.setClientChannel(clientChannel);
        connection.setClientKey(key);

        return connection;
    }


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
            if(this.actualConnection.applyLeet()) {
                // TODO: Should use Stanzas here?
                toSendString = doc.text(Conversor.apply(doc.text()).toString()).toString();
            }
            shouldSend = !Blocker.apply(doc.toString());
        }

        if(shouldSend){
            handleSendMessage(toSendString, channel);
        }

    }

    public void sendToServer(String s) throws IOException {
        if (this.actualConnection.getServerChannel() == null) {
            InetSocketAddress hostAddress = new InetSocketAddress(CONNECT_SERVER, CONNECT_PORT);
            this.actualConnection.setServerChannel(SocketChannel.open(hostAddress));
        }
        writeInChannel(s, this.actualConnection.getServerChannel());
        this.actualConnection.getServerChannel().configureBlocking(false);
        this.actualConnection.getServerChannel().register(this.actualConnection.getSelector(), SelectionKey.OP_READ);
    }

    public void sendToClient(String s) throws IOException {
        writeInChannel(s, this.actualConnection.getClientChannel());
    }

    // Private functions

    private void writeInChannel(String s, SocketChannel channel) {
        this.actualConnection.processWrite(s, this.actualConnection.getClientChannel() == channel ? "client" : "server");
    }

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
