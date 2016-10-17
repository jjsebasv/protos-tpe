package ar.edu.itba.protos.Proxy.Elements;

/**
 * Created by sebastian on 10/16/16.
 */
public abstract class Element {
    private String from;

    public Element(String from) {
        this.from = from;
    }

    public static Message createMessage(String from, String to) {
        return new Message(from, to);
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
