package dev.aikido.agent_api.helpers.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPAddress {

    public static String getIpAddress() {
        try {
            NetworkInterface bestInterface = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Check if the network interface is up and not a loop-back
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    // Check if it has at least one valid IP address
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress()) { // Check if the address is not a loop-back address
                            bestInterface = networkInterface;
                            break; // Found a valid interface, no need to check further
                        }
                    }
                }
            }

            if (bestInterface != null) {
                Enumeration<InetAddress> inetAddresses = bestInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {}
        return "0.0.0.0";
    }
}
