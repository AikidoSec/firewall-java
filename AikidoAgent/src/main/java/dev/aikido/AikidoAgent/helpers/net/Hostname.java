package dev.aikido.AikidoAgent.helpers.net;

import java.io.IOException;
import java.util.Scanner;

public class Hostname {
    public static String getHostname() {
        // getHostName function seem unreliable, so using "hostname" command which works for both UNIX(-like) systems and Windows
        // See https://stackoverflow.com/a/7800008 for more info.
        try (Scanner s = new Scanner(Runtime.getRuntime().exec("hostname").getInputStream()).useDelimiter("\\A")) {
            if (s.hasNext()) {
                return s.next().trim();
            }
        } catch (IOException ignored) {
        }
        return "unknown";
    }
}
