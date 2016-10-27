package ar.edu.itba.protos.Admin;

import ar.edu.itba.protos.Logger.XmppLogger;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

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

    public AdminHandler(Selector selector) {
        config = new HashMap<>();
        parser = new AdminParser();
    }

    public void setChannel(ServerSocketChannel channel) {
        this.channel = channel;
    }

    /**
     * Handles incoming connections to admin port.
     *
     * Creates a new ChannelBuffers object which will contain the read and write
     * buffers related to the channel.
     *
     */

    public void accept(SocketChannel channel) throws IOException {
        logger.info("New admin connected");
        config.put(channel, ByteBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }

    /**
     * Handles incoming reads from administrators.
     *
     * Parses the message and validates the syntax.
     *
     */

    public SocketChannel read(SelectionKey key) throws IOException {
        SocketChannel s = (SocketChannel) key.channel();
        ByteBuffer buffer = config.get(s);
        int bytesRead = s.read(buffer);
        System.out.println("Estoy leyendo del admin");
        try {
            String response;
            if ((response = parser.parseCommand(buffer, bytesRead)) != null) {
                if (logged || response.equals("PASSWORD OK\n")) {
                    logged = true;
                    s.write(ByteBuffer.wrap(response.getBytes()));
                } else if (response.equals("INVALID PASSWORD\n")){
                    s.write(ByteBuffer.wrap(response.getBytes()));
                } else {
                    s.write(ByteBuffer.wrap("Not logged in!\n".getBytes()));
                }
            }
        } catch (Exception e) {
            logger.error("Lost connection with the admin");
            s.close();
            key.cancel();
            return null;
        }
        buffer.clear();
        return null;
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
