package dev.aikido.agent_api.helpers.net;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import java.io.IOException;
import java.util.Scanner;

public final class Hostname {
    private Hostname() {}

    private static final Logger logger = LogManager.getLogger(Hostname.class);
    private static final String HOSTNAME;

    // getHostName function seem unreliable, so using "hostname" command which works for both UNIX(-like) systems and Windows
    // See https://stackoverflow.com/a/7800008 for more info.
    static {
        String hostname = "unknown";
        try (Scanner s = new Scanner(Runtime.getRuntime().exec("hostname").getInputStream()).useDelimiter("\\A")) {
            if (s.hasNext()) {
                hostname = s.next().trim();
            }
        } catch (IOException e) {
            logger.debug(e);
        }
        HOSTNAME = hostname;
    }

    public static String get() {
        return HOSTNAME;
    }
}
