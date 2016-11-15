package ar.edu.itba.protos.Admin;

/**
 * Created by sebastian on 10/27/16.
 */
public class Admin {

    String username;
    String pass;

    public Admin(String username, String pass) {
        this.username = username;
        this.pass = pass;
    }

    public String getUsername() { return this.username; }
    public String getPass() { return this.pass; }

    public void setPass(String pass){ this.pass = pass; }
}
