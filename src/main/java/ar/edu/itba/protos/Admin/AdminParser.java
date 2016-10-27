package ar.edu.itba.protos.Admin;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sebastian on 10/27/16.
 */
public class AdminParser {

    /**
     * Parses a command from the read buffer and validates that it is a valid
     * sentence inside our defined protocol.
     *
     * @param readBuffer
     * @param bytesRead
     * @return
     */

    public String parseCommand(ByteBuffer readBuffer, int bytesRead) {

        String fullCommand = new String(readBuffer.array()).substring(0, bytesRead);
        Map<String, String> commands = new HashMap<>();
        for (String s : fullCommand.split(";")) {
            System.out.println(s);
        }

        return takeActions(commands);
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

}
