package ar.edu.itba.protos.Handlers;

import javax.swing.text.html.parser.DTD;

/**
 * Created by sebastian on 10/21/16.
 */
public class HTMLHandler extends DTD {

    String name;

    public HTMLHandler(String name) {
        super(name);
        this.name = super.name;
    }
}
