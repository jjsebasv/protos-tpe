package ar.edu.itba.protos.Protocols;

import jdk.internal.org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Created by sebastian on 10/3/16.
 */
public interface DefaultTCP {
    void handleAccept(SelectionKey key)throws IOException;
    void handleRead(SelectionKey key) throws IOException;
    void handleWrite(SelectionKey key) throws IOException;
}
