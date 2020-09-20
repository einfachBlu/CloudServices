package de.blu.common.util;

import com.google.inject.Singleton;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Singleton
public final class HostnameProvider {
    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return "-";
    }
}
