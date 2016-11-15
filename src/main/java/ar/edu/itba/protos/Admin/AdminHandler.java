package ar.edu.itba.protos.Admin;

import ar.edu.itba.protos.Logger.XmppLogger;
import ar.edu.itba.protos.Proxy.Connection.ConnectionImpl;
import ar.edu.itba.protos.Proxy.Filters.Conversor;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import ar.edu.itba.protos.Proxy.Metrics.Metrics;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Created by sebastian on 10/27/16.
 */
public class AdminHandler extends DefaultHandler {

    private static final int DEFAULT_BUFFER_SIZE = 1024*100;
    private ServerSocketChannel channel;

    private Map<SocketChannel, ByteBuffer> config;
    private AdminParser parser;
    private boolean logged = false;
    private XmppLogger logger = XmppLogger.getInstance();
    private Selector selector;

    public AdminHandler(Selector selector) {
        config = new HashMap<>();
        parser = new AdminParser();
    }

    public void setChannel(ServerSocketChannel channel) {
        this.channel = channel;
    }
    public void setSelector(Selector selector) { this.selector = selector; }

    /**
     * Handles incoming connections to admin port.
     *
     * Creates a new ChannelBuffers object which will contain the read and write
     * buffers related to the channel.
     *
     */

    public void accept(SelectionKey key, Selector selector) throws IOException {

        logger.info("New admin connected");
        System.out.println("hey, new admin!");

        ConnectionImpl connection = new ConnectionImpl(selector);


        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocketChannel.accept();
        Socket socket = clientChannel.socket();

        logger.info("Connected to: " + socket.getRemoteSocketAddress());
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(), SelectionKey.OP_READ, connection);

        connection.setClientChannel(clientChannel);
        connection.setClientKey(key);


        config.put(clientChannel, ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    /**
     * Handles incoming reads from administrators.
     *
     * Parses the message and validates the syntax.
     *
     */

    public void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = config.get(channel);
        int bytesRead = channel.read(buffer);
        System.out.println("Estoy leyendo del admin");

        int read = -1;
        read = channel.read(buffer);

        if (read == -1) {
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[read];
        System.arraycopy(buffer.array(), 0, data, 0, read);
        String stringRead = new String(data);

        int response = parser.parseCommand(buffer, bytesRead, this.logged);
        switch (response) {
            case 0:
                channel.write(ByteBuffer.wrap("OK\n".getBytes()));
                channel.write(ByteBuffer.wrap("**********************************************************************************\n".getBytes()));
                channel.write(ByteBuffer.wrap("Welcome to HELP.\n These are your available commands\n".getBytes()));
                channel.write(ByteBuffer.wrap("LOG [username] [password] - Log in\n".getBytes()));
                channel.write(ByteBuffer.wrap("LEET_ON - Turn on the Leet Converter\n".getBytes()));
                channel.write(ByteBuffer.wrap("LEET_OFF - Turn off the Leet Converter\n".getBytes()));
                channel.write(ByteBuffer.wrap("LOG_OUT - Log out\n".getBytes()));
                channel.write(ByteBuffer.wrap("BLOCK [username] - Blocks the given username\n".getBytes()));
                channel.write(ByteBuffer.wrap("UNBLOCK [username] - Unblocks the given username\n".getBytes()));
                channel.write(ByteBuffer.wrap("MULTIPLEX [username] [server] - Multiplexes the given userneme to the given server\n".getBytes()));
                channel.write(ByteBuffer.wrap("UNPLEX [username] - Unplexes the given username\n".getBytes()));
                channel.write(ByteBuffer.wrap("PASSCHANGE [username] [new password] - Sets the new password to the given username\n".getBytes()));
                channel.write(ByteBuffer.wrap("ACCESSES - * METRICS * Returns the ammount of accesses\n".getBytes()));
                channel.write(ByteBuffer.wrap("BLOCKED - * METRICS * Returns the ammount of blocked messages\n".getBytes()));
                channel.write(ByteBuffer.wrap("BYTES_SENT - * METRICS * Returns the amount of bytes sent\n".getBytes()));
                channel.write(ByteBuffer.wrap("BYTES_RECEIVED - * METRICS * Returns the amount of bytes received\n".getBytes()));
                channel.write(ByteBuffer.wrap("CHARACTERS_CONVERTED - * METRICS * Returns the amount of characters converted\n".getBytes()));
                channel.write(ByteBuffer.wrap("**********************************************************************************\n".getBytes()));
                break;
            case 1: // Wrong username
                channel.write(ByteBuffer.wrap("ERROR, Invalid Username!\n".getBytes()));
                break;
            case 2: // Wrong pass
                channel.write(ByteBuffer.wrap("ERROR, Invalid Pass!\n".getBytes()));
                break;
            case 3: // Connected
                logged = true;
                channel.write(ByteBuffer.wrap("OK, Logged in!\n".getBytes()));
                break;
            case 4: // Enable leet
                Conversor.applyLeet = true;
                channel.write(ByteBuffer.wrap("OK, 4pply1ng l33t!\n".getBytes()));
                break;
            case 5:
                Conversor.applyLeet = false;
                channel.write(ByteBuffer.wrap("OK, Leet disabled!\n".getBytes()));
                break;
            case 7:
                channel.write(ByteBuffer.wrap("OK, Good Bye!\n".getBytes()));
                channel.close();
                key.cancel();
                return;
            case 8:
                channel.write(ByteBuffer.wrap("OK, User blocked!\n".getBytes()));
                return;
            case 9:
                channel.write(ByteBuffer.wrap("OK, User unblocked!\n".getBytes()));
                return;
            case 10:
                channel.write(ByteBuffer.wrap("OK, User redirected!\n".getBytes()));
                return;
            case 11:
                channel.write(ByteBuffer.wrap("OK, User unplexed!\n".getBytes()));
                return;
            case 12:
                channel.write(ByteBuffer.wrap("OK, Password changed succesfully!\n".getBytes()));
                return;
            case 13:
                channel.write(ByteBuffer.wrap(String.format("OK, [Metrics] Number of total accesses: %d\n", Metrics.getInstance().getTotalAccesses()).getBytes()));
            case 14:
                channel.write(ByteBuffer.wrap(String.format("OK, [Metrics] Number of blocked messages: %d\n", Metrics.getInstance().getBlockedMessages()).getBytes()));
            case 15:
                channel.write(ByteBuffer.wrap(String.format("OK, [Metrics] Number of bytes sent: %d\n", Metrics.getInstance().getReceivedBytes()).getBytes()));
            case 16:
                channel.write(ByteBuffer.wrap(String.format("OK, [Metrics] Number of bytes received: %d\n", Metrics.getInstance().getReceivedBytes()).getBytes()));
            case 17:
                channel.write(ByteBuffer.wrap(String.format("OK, [Metrics] Number of converted characters: %d\n", Metrics.getInstance().getConvertedCharacters()).getBytes()));
            case -2: // You are not logged in
                channel.write(ByteBuffer.wrap("ERROR, You're not logged in!\n".getBytes()));
                break;
            case -3:
                channel.write(ByteBuffer.wrap("ERROR, Unexisting admin user!\n".getBytes()));
                break;
            case -4:
                channel.write(ByteBuffer.wrap("ERROR, Password does not match!\n".getBytes()));
                break;
            default: // wrong command
                // FIXME: What do we do with this? How do we allow to write something ese once is wrong?
                channel.write(ByteBuffer.wrap("OK, Wrong command\n".getBytes()));
                buffer.clear();
                break;
                /*                 logger.error("Lost connection with the admin");
                channel.close();
                key.cance
                l();
                return;
                */
        }

        return;
    }

    /**
     * Handles write operations.
     *
     * Gets the buffer from the ChannelBuffers object and writes directly into
     * it.
     *
     */

    public void write(SelectionKey key) throws IOException {
        SocketChannel s = (SocketChannel) key.channel();
        ByteBuffer wrBuffer = config.get(s);
        wrBuffer.flip();
        s.write(wrBuffer);
        wrBuffer.compact();
    }

}
