package de.blu.common.util;

import com.google.inject.Singleton;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Singleton
public final class AddressResolver {
    public String getIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return "-";
    }
}
