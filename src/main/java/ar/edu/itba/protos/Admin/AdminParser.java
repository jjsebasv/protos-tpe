package ar.edu.itba.protos.Admin;

import ar.edu.itba.protos.Proxy.Filters.Blocker;
import ar.edu.itba.protos.Proxy.Filters.Conversor;
import ar.edu.itba.protos.Proxy.Filters.Multiplexer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sebastian on 10/27/16.
 */
public class AdminParser {

    private Map<String,String> adminBeta = new HashMap<>();

    private final Set<Admin> admins = new HashSet<>();
    private Map<String,Integer> comNumber = new HashMap<>();


    public AdminParser() {

        admins.add(new Admin("sebas-admin@protos-tpe", "123456789"));
        comNumber.put("log",2);
        comNumber.put("logout",0);
        comNumber.put("leeton",0);
        comNumber.put("leetoff",0);
        comNumber.put("block",1);
        comNumber.put("unblock",1);
        comNumber.put("multiplex",2);
        comNumber.put("unplex",1);
        comNumber.put("metrics",0);
        comNumber.put("passchange",3);
        adminBeta.put("sebas-admin@protos-tpe","123456789");


    }

    /**
     * Parses a command from the read buffer and validates that it is a valid
     * sentence inside our defined protocol.
     *
     * @param readBuffer
     * @param bytesRead
     * @return
     */




    public int parseCommand(ByteBuffer readBuffer, int bytesRead, boolean logged) {

        String fullCommand = new String(readBuffer.array()).substring(0, bytesRead);
        String commands[] = fullCommand.split(" ");
        if(!logged && !commands[0].equals("LOG")) {
            return -2;
        }


        if(comNumber.containsKey(commands[0].toLowerCase()) && commands.length == comNumber.get(commands[0].toLowerCase())) {
            switch (commands[0].toString().toUpperCase()) {
                case "LOG":
                    if (commands[1] == null || commands[2] == null) {
                        return -1;
                    }
                    return login(commands[1], commands[2]);
                case "LEETON\n":
                    return 4;
                case "LEETOFF\n":
                    return 5;
                case "LOGOUT\n":
                    return 7;
                case "BLOCK":
                    if (commands[1] == null) {
                        return -1;
                    }
                    return block(commands[1]);
                case "UNBLOCK":
                    if (commands[1] == null) {
                        return -1;
                    }
                    return unblock(commands[1]);
                case "MULTIPLEX":
                    return mplex(commands[1],commands[2]);
                case "UNPLEX":
                    return uplex(commands[1]);
                case "PASSCHANGE":
                    return cPass(commands[1],commands[2],commands[3]);
                default:
                    return -1;

            }
        }else{
            return -1;
        }
    }

    /**
     * Once the commands were parsed, takes the appropriate action using the
     * executors stored in the commandTypes map.
     *
     * @param commands
     * @return
     */

    private String takeActions(Map<String, String> commands) {

        String responseToAdmin = null;
        for (String cmd : commands.keySet()) {
            System.out.println(cmd);
        }
        return responseToAdmin + '\n';
    }

    private int login(String username, String pass) {
        for (Admin admin : admins) {
            if (username.equals(admin.getUsername())) {
                if (pass.equals(admin.getPass().concat("\n")) || pass.equals(admin.getPass())) {
                    return 3; // Connected
                } else {
                    return 2; // Wrong pass
                }
            }
        }
        return 1;
    }

    private int block(String username) {
        Blocker.add(username);
        return 8;
    }

    private int unblock(String username) {
        Blocker.remove(username);
        return 9;
    }

    private int mplex(String username, String server){
        Multiplexer.multiplex(username,server);
        return 10;
    }

    private int uplex(String username){
        Multiplexer.unplex(username);
        return 11;
    }


    //missing connection state
    private int cPass(String adminUser, String currentPass, String newPass){
        if(adminBeta.containsKey(adminUser){
            if (adminBeta.get(adminUser).compareTo(currentPass)==0){
                adminBeta.put(adminUser,newPass);
                return 12;//password changed succesfully
            }else{
                return -4;// wrong pass
            }
        }
        return -3;//wrong username

    }


}


/*
if(stringRead.indexOf("LOG") == 0) {
            String command[] = stringRead.split(" ");
            boolean foundUser = false;
            for (Admin admin : admins) {
                if (command[1].equals(admin.getUsername())) {
                    foundUser = true;
                    if (command[2].equals(admin.getPass())) {
                        logged = true;
                        channel.write(ByteBuffer.wrap("Logged in\n".getBytes()));
                    } else {
                        foundUser = false;
                        channel.write(ByteBuffer.wrap("Wrong pass\n".getBytes()));
                    }
                }
            }
            if (!foundUser) {
                channel.write(ByteBuffer.wrap("Wrong User!\n".getBytes()));
            }
        }
 */
