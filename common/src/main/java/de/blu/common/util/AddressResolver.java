package de.blu.common.util;

import com.google.inject.Singleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            System.out.println("Port " + port + " is in Use on " + host + "!");
            socket.close();
            return true;
        } catch (IOException e) {
            System.out.println("Port " + port + " is available on " + host + "!");
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

    public boolean isPortInUse(int port) {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            /*
            socket.setReuseAddress(true);
            socket.setSoTimeout(500);
            socket.accept(); */
            //System.out.println("Port " + port + " is available on localhost!");
            return false;
        } catch (IOException e) {
            System.out.println("Port " + port + " is in Use on localhost!");
            return true;
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

    public String execCmd(String cmd) throws java.io.IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);

        StringBuilder stringBuilder = new StringBuilder("");

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        //System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            //System.out.println(s);
            stringBuilder.append("\n" + s);
        }

        // Read any errors from the attempted command
        //System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            //System.out.println(s);
            stringBuilder.append("\n" + s);
        }

        return stringBuilder.toString();
    }
}
