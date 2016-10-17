package ar.edu.itba.protos.Stanza;

import ar.edu.itba.protos.Proxy.Elements.Element;

/**
 * Created by sebastian on 10/16/16.
 */
public class Stanza {

    private Element element;
    private String xml;
    private String type;
    private boolean accepted;
    private boolean completed;

    public Stanza(String type, Element element) {
        this.accepted = true;
        this.completed = false;

        this.type = type;
        this.element = element;
    }

    public String getXml() {
        return this.xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public void reject() {
        this.accepted = false;
    }

    public void finish() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return this.completed;
    }

    public boolean isAccepted() {
        return this.accepted;
    }

    public Element getElement() {
        return this.element;
    }

}
