package dev.aikido.agent_api.vulnerabilities.ssrf;

import static dev.aikido.agent_api.helpers.url.PortParser.getPortFromURL;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.storage.RedirectNode;
import java.net.URL;
import java.util.List;

public final class RedirectOriginFinder {
    private RedirectOriginFinder() {}

    public static URL getRedirectOrigin(String hostname, int port) {
        List<RedirectNode> redirectStartNodes = Context.get().getRedirectStartNodes();

        // Loop over all start nodes :
        for (RedirectNode node : redirectStartNodes) {
            RedirectNode currentChild = node.getChild();
            while (currentChild != null) {
                int childPort = getPortFromURL(currentChild.getUrl());
                if (childPort == port && currentChild.getUrl().getHost().equals(hostname)) {
                    // Check if this child has the same Hostname and port as provided
                    return node.getUrl();
                }

                // Traverse chain downwards :
                currentChild = currentChild.getChild();
            }
        }
        return null;
    }
}
