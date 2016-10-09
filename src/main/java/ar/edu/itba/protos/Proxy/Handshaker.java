package ar.edu.itba.protos.Proxy;

import ar.edu.itba.protos.Proxy.Connection.ConnectionImpl;
import com.fasterxml.aalto.AsyncByteBufferFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;

import javax.xml.stream.XMLStreamException;
import java.nio.ByteBuffer;

/**
 * Created by sebastian on 10/9/16.
 */
public class Handshaker {

    private AsyncXMLStreamReader<AsyncByteBufferFeeder> streamReader;
    private AsyncByteBufferFeeder feeder;
    private ByteBuffer byteBuffer;

    public Handshaker(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        this.streamReader = (new InputFactoryImpl()).createAsyncForByteBuffer();
        feeder = this.streamReader.getInputFeeder();
    }

    public String iterate(ConnectionImpl actualConnection) throws XMLStreamException {
        int actualElem = streamReader.next();
        while (actualElem != AsyncXMLStreamReader.END_DOCUMENT && streamReader.hasNext()) {
            if (actualElem == AsyncXMLStreamReader.START_DOCUMENT) {
                actualElem = streamReader.next();
                continue;
            } else if (actualElem == AsyncXMLStreamReader.START_ELEMENT) {
                String tagName = streamReader.getLocalName();
                if (tagName.equals("stream")) {
                    for (int i = 0; i < streamReader.getAttributeCount(); i++) {
                        String attributeName = streamReader.getAttributeLocalName(i);
                        if (attributeName == "to")
                            actualConnection.setServerName(streamReader.getAttributeValue(i));
                    }
                }
                return tagName;
            } else if (actualElem == AsyncXMLStreamReader.CHARACTERS) {
                return streamReader.getText();
            } else if (actualElem == AsyncXMLStreamReader.EVENT_INCOMPLETE) {
                // TODO: Handle event incomplete
            } else if (actualElem == AsyncXMLStreamReader.END_ELEMENT) {
                String name = streamReader.getName().getLocalPart();
                if (name.equals("features")) {
                    return name;
                }
            }
            actualElem = streamReader.next();
        }
        return null;
    }

    public void end() {
        try {
            streamReader.close();
        } catch (XMLStreamException e) {
            //TODO: Handle the exception
        }
    }

}
