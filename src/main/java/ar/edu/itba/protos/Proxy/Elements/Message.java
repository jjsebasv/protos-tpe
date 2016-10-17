package ar.edu.itba.protos.Proxy.Elements;

/**
 * Created by sebastian on 10/16/16.
 */
public class Message extends Element {

    // from is on parent though
    private String from;
    private String to;

    // Message itself
    private String content;
    private boolean active;
    private String xmlns;

    // Error handling
    private String error;
    private int nError;
    private String tError;

    public Message (String from, String to) {
        super(from);
        this.from = from;
        this.to = to;
    }

    public Message (String from, String to, String content) {
        super(from);
        this.from = from;
        this.to = to;
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isActive() {
        return this.active;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void setXmlns(String xmlns) {
        this.xmlns = xmlns;
    }

    public String getXmlns() {
        return this.xmlns;
    }

    public String convertToXml() {
        StringBuffer xmlMessage = new StringBuffer();
        xmlMessage.append("<message from='" + this.from + "' to='" + this.to + "' type='chat'>");

        if (this.active)
            xmlMessage.append("<active xmlns='" + this.xmlns + "'>"  + this.active + "</active>");

        xmlMessage.append("<body>" + this.content + "</body>");

        if (nError != 0)
            xmlMessage.append("<error code='" + this.nError + "' type='" + this.tError + "'>" + this.error + "</error>");

        xmlMessage.append("</message>");

        return xmlMessage.toString();
    }

}
