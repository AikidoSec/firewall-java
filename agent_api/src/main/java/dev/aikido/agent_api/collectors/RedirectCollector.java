package dev.aikido.agent_api.collectors;


import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.storage.RedirectNode;

import java.net.URL;
import java.util.List;

public class RedirectCollector {
    public static void report(URL origin, URL dest) {
        ContextObject context = Context.get();

        List<RedirectNode> redirectStarterNodes = context.getRedirectStartNodes();
        for (RedirectNode node : redirectStarterNodes) {
            RedirectNode currentChild = node.getChild();
            while (currentChild.getChild() != null) {
                currentChild = currentChild.getChild();
            }
            // We've got the last node in the chain, check if it matches w/ origin :
            if(currentChild.getUrl().toString().equals(origin.toString())) {
                // Origins match: Set as child
                new RedirectNode(currentChild, dest);
                return;
            }
        }

        // Create a starter node :
        RedirectNode starterNode = new RedirectNode(origin);
        new RedirectNode(starterNode, dest); // Create child node
        context.addRedirectNode(starterNode);
        Context.set(context); // Update context.
    }
}