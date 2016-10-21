package ar.edu.itba.protos.Proxy.Connection;

import ar.edu.itba.protos.Handlers.XMPPHandler;
import ar.edu.itba.protos.Proxy.Elements.Message;
import ar.edu.itba.protos.Stanza.Stanza;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.html.parser.Parser;


/**
 * Created by sebastian on 10/9/16.
 */
public class ConnectionImpl implements Connection {

    final String TO_CLIENT_INVALID_XML = "<?xml version='1.0' ?><stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>";

    private SocketChannel clientChannel;
    private SocketChannel serverChannel;

    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;

    private SelectionKey clientKey;
    private SelectionKey serverKey;

    private String serverName;
    private String clientName;

    // Client Streams
    protected static final String INITIAL_STREAM = "<?xml version='1.0' ?><stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0' ";

    // Server Streams
    protected static final byte[] INITIAL_SERVER_STREAM = ("<?xml version='1.0' ?><stream:stream xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>")
            .getBytes();
    protected static final byte[] NEGOTIATION = ("<stream:features><mechanisms xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\"><mechanism>PLAIN</mechanism></mechanisms><auth xmlns=\"http://jabber.org/features/iq-auth\"/></stream:features>")
            .getBytes();

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

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

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

    public SelectionKey getServerKey() {
        return this.serverKey;
    }

    public void setClientChannel(SocketChannel channel) {
        this.clientChannel = channel;
    }

    public void setServerChannel(SocketChannel channel) {
        this.serverChannel = channel;
    }

    public String getServerName() { return this.serverName; }

    public String process(long bytesRead, SocketChannel channel, ByteBuffer byteBuffer) {
        System.out.println("En el process " + bytesRead);
        if (bytesRead > 0) {
            System.out.println(bytesRead);
            List<Stanza> stanzaList = null;
            System.out.println(byteBuffer.toString());
            try {
                this.clientName = channel.getLocalAddress().toString();
                this.serverName = channel.getRemoteAddress().toString();
            } catch (IOException e) {
                System.out.println("ERROR");
            }

            stanzaList = parseStream(byteBuffer);
            for (Stanza stanza : stanzaList) {
                System.out.println("Hay elemento en el for");
                if (stanza.getElement() != null) {
                    if (stanza.getElement().getFrom() == null) {
                        stanza.getElement().setFrom(this.clientName + '@' + this.serverName);
                    }
                }
                System.out.println(stanza.isAccepted());
                String msg = stanza.getXml();
                System.out.println("ESTE ES EL MENSAJE " + msg);
                return msg;
                /*
                boolean rejected = !stanza.isAccepted();
                System.out.println("Y esto que es?? " + ((Message) stanza.getElement()).getContent());
                if (rejected)
                    // TODO: Handle rejected

                    if (!rejected) {

                        if (((Message) stanza.getElement()).getContent() != null) {
                            // TODO: Send message to end point
                        }
                    }
                */
            }
        }
        return TO_CLIENT_INVALID_XML;
    }

    // Private functions

    private List<Stanza> parseStream(ByteBuffer xmlStream) {
        String xmlString = new String(xmlStream.array());
        xmlString = xmlString.substring(0, xmlStream.position());
        List<String> messages = new ArrayList<>();
        List<Stanza> streamList = new LinkedList<Stanza>();
        System.out.println("estamos en el parser");
        if (xmlString.contains("<stream:")) {
            // FIXME: What other types we could have?
            Stanza s = new Stanza("message", null);
            s.setXml(xmlString);
            streamList.add(s);
            return streamList;
        } else {
            int i = 0;
            // Get only the bodies of the messages
            while (xmlString.indexOf("<message", i) > -1) {
                int bodyPosition = xmlString.indexOf("<body", i);
                i = xmlString.indexOf(">", bodyPosition);
                int bodyEndingPosition = xmlString.indexOf("</body>", bodyPosition);
                if (bodyPosition > -1 && bodyEndingPosition > -1) {
                    String bodyMessage = xmlString.substring(i + 1, bodyEndingPosition);
                    xmlString = xmlString.substring(0, i + 1) + xmlString.substring(bodyEndingPosition, xmlString.length());
                    messages.add(bodyMessage);
                }
            }
            String newString = "<xmpp-proxy>" + xmlString + "</xmpp-proxy>";
            byte[] xmlBytes = newString.getBytes();
            InputStream is = new ByteArrayInputStream(xmlBytes);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            SAXParser saxParser;
            try {
                saxParser = factory.newSAXParser();
                XMPPHandler handler = new XMPPHandler();
                saxParser.parse(is, handler);
                if (handler.actualStanza == null || !handler.actualStanza.isCompleted()) {
                    // TODO: hanlde incomplete elements
                }
                List<Stanza> stanzas = handler.stanzas;
                Iterator<String> iterator = messages.iterator();
                for (Stanza s : stanzas) {
                    if(iterator.hasNext()){
                        ((Message) s.getElement()).setContent(iterator.next());
                    }
                }
                return stanzas;
            } catch (Exception e)  {
                // TODO: handle execptions and add to logger
                System.out.println(e);
            }
        }
        return streamList;
    }
}
