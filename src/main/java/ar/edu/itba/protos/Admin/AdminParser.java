package ar.edu.itba.protos.Admin;

import ar.edu.itba.protos.Proxy.Filters.Blocker;
import ar.edu.itba.protos.Proxy.Filters.Conversor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sebastian on 10/27/16.
 */
public class AdminParser {

    private final Set<Admin> admins = new HashSet<>();

    public AdminParser() {
        admins.add(new Admin("sebas-admin@protos-tpe", "123456789"));
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

        switch (commands[0].toString()) {
            case "LOG":
                if( commands[1] == null || commands[2] == null) {
                    return -1;
                }
                return login(commands[1], commands[2]);
            case "LeetOn\n":
                return 4;
            case "LeetOff\n":
                return 5;
            case "LOGOUT\n":
                return 7;
            case "BLOCK":
                if(commands[1] == null) {
                    return -1;
                }
                return block(commands[1]);
            case "UNBLOCK":
                if(commands[1] == null) {
                    return -1;
                }
                return unblock(commands[1]);
            default:
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
