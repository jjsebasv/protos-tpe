package ar.edu.itba.protos.Stanza;

import ar.edu.itba.protos.Proxy.Elements.Element;

/**
 * Created by sebastian on 10/16/16.
 */
public class Stanza {

    private String xml;
    private String type;
    private boolean accepted;

    private String body;
    private String wholeFrom;
    private String wholeTo;
    private String from;
    private String to;

    private boolean blockFrom;
    private boolean blockTo;


    public String getXml() {
        return this.xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public boolean isAccepted() {
        return this.accepted;
    }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }

    public void setBlockFrom (boolean block) {
        this.blockFrom = block;
    }
    public void setBlockTo (boolean block) {
        this.blockTo = block;
    }

    public String getBody() {
        return this.body;
    }

    public Stanza (String message) {
        this.xml = message;
        this.accepted = true;

        int typeIndex = message.indexOf("type='");
        if (typeIndex >= 0) {
            this.type = message.substring(typeIndex + 6, message.indexOf("'", typeIndex + 6));

            if (this.type.equals("chat")) {
                int bodyBeggins = message.indexOf("<body>");
                int bodyEnds = message.indexOf("</body>");
                if (bodyBeggins >= 0 && bodyEnds >= 0) {
                    this.body = message.substring(bodyBeggins + 6, bodyEnds);
                }
            }
        } else if (message.equals("</stream:stream>")) {
            this.type = "CLOSE";
        } else if (message.length() == 0){
            this.type = "Empty";
        } else {
            System.out.println("check this out: " + message);
            this.type = message.substring(1, message.indexOf(" ", 0));
        }

        int fromInitial = message.indexOf("from='");
        int toInitial = message.indexOf("to='");
        if (fromInitial == -1) {
            this.from = null;
            this.wholeFrom = null;
        } else {
            int endingPoint = message.indexOf("'", fromInitial+6);
            this.wholeFrom =  message.substring(fromInitial+6, endingPoint);
            int middlePoint = this.wholeFrom.indexOf("/");
            this.from = middlePoint == -1 ? this.wholeFrom : this.wholeFrom.substring(0, middlePoint);
        }

        if (toInitial == -1) {
            this.to = null;
            this.wholeTo = null;
        } else {
            int endingPoint = message.indexOf("'", toInitial+4);
            this.wholeTo =  message.substring(toInitial+4, endingPoint);
            int middlePoint = this.wholeTo.indexOf("/");
            this.to = middlePoint == -1 ? this.wholeTo : this.wholeTo.substring(0, middlePoint);
        }

    }

    public boolean isChat() {
        return this.type.equals("chat");
    }

    public void replaceBody(String newBody) {
        this.setXml(this.xml.replaceAll("<body>" + this.body + "</body>", "<body>" + newBody + "</body>"));
        this.body = newBody;
    }

    public void transformXml() {
        this.setXml(this.xml.replaceAll("from='" + this.wholeFrom + "'", "from='XMPP-ADMIN'"));
        this.setXml(this.xml.replaceAll("to='" + this.wholeTo + "'", "to='" + this.wholeFrom + "'"));
    }

    public void showBlockMessage() {
        if (this.blockFrom) {
            replaceBody("Your user is currently blocked. Please contact an admin to unblock it.");
        }
        if (this.blockTo) {
            replaceBody("The user you are trying to contact (" + this.to + "), is currently blocked. Sorry");
        }
    }


}
