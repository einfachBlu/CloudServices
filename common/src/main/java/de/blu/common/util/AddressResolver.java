package de.blu.common.util;

import com.google.inject.Singleton;

import java.io.IOException;
import java.net.*;

@Singleton
public final class AddressResolver {
    public String getIPAddress() {
        try {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                return socket.getLocalAddress().getHostAddress();
            }
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }

        return "-";
    }

    public boolean isPortInUse(String host, int port) {
        // Check in Network
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
