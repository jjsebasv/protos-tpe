package ar.edu.itba.protos.Proxy.Connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by sebastian on 11/15/16.
 */
public class AdminConnectionImpl {

    private SocketChannel channel;
    private AdminState state;
    public static enum AdminState {
        LOGGED_IN,
        NO_STATUS
    }

    public AdminConnectionImpl(SocketChannel channel) {
        this.setState(AdminState.NO_STATUS);
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public AdminState getState() {
        return state;
    }

    public void setState(AdminState state) {
        this.state = state;
    }

    public void closeConnection() throws IOException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
    }
}
