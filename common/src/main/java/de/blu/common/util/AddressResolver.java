package de.blu.common.util;

import com.google.inject.Singleton;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
}
