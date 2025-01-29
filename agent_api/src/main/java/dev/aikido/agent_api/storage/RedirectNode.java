package dev.aikido.agent_api.storage;

import java.net.URL;

public class RedirectNode {
    private final RedirectNode origin;
    private RedirectNode child;
    private final URL url;

    public RedirectNode(RedirectNode origin, URL destination) {
        this.origin = origin;
        if (origin != null) {
            origin.setChild(this);
        }
        this.url = destination;
        this.child = null;
    }

    public RedirectNode(URL destination) {
        // Create a starter node :
        this(null, destination);
    }

    public RedirectNode getOrigin() {
        return origin;
    }

    public RedirectNode getChild() {
        return child;
    }

    public URL getUrl() {
        return url;
    }

    public void setChild(RedirectNode child) {
        RedirectNode currentOrigin = this.origin;
        while (currentOrigin != null) {
            if (currentOrigin == child) {
                // Child already present in chain, return.
                return;
            }
            currentOrigin = currentOrigin.getOrigin(); // Move up a node.
        }
        this.child = child;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RedirectNode other = (RedirectNode) obj;
        return other.getUrl().toString().equals(other.getUrl().toString());
    }
}
