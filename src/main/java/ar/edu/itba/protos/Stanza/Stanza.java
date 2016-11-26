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

    private String body;


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
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public String getBody() {
        return this.body;
    }

    public Element getElement() {
        return this.element;
    }

    public Stanza (String message) {
        this.xml = message;
        this.accepted = true;

        int typeIndex = message.indexOf("type='");
        if (typeIndex >= 0) {
            this.type = message.substring(typeIndex+6, message.indexOf("'", typeIndex+6));

            if (this.type.equals("chat")) {
                int bodyBeggins = message.indexOf("<body>");
                int bodyEnds = message.indexOf("</body>");
                if (bodyBeggins >= 0 && bodyEnds >= 0) {
                    this.body = message.substring(bodyBeggins+6, bodyEnds);
                }
            }
        } else if (message.equals("</stream:stream>")) {
            this.type = "CLOSE";
        } else {
            System.out.println("check this out: " + message);
            this.type = message.substring(1, message.indexOf(" ", 0));
        }
    }

    public boolean isChat() {
        return this.type.equals("chat");
    }

    public void replaceBody(String newBody) {
        this.xml.replaceAll("<body>" + this.body + "</body>", "<body>" + newBody + "</body>");
    }

}
