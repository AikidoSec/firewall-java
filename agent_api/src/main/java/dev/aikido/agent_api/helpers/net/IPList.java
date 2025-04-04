package dev.aikido.agent_api.helpers.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * IPList implements an efficient IP address and CIDR subnet matching using separate tries for IPv4 and IPv6.
 * Each node in the trie represents a bit in the IP address, allowing for fast prefix matching.
 */
public class IPList {
    private static class TrieNode {
        TrieNode[] children;
        boolean isTerminal;
        String value;
        
        TrieNode() {
            children = new TrieNode[2];
            isTerminal = false;
            value = null;
        }
    }

    private final TrieNode ipv4Root;
    private final TrieNode ipv6Root;
    private final ReentrantReadWriteLock lock;
    private static final int IPV4_LENGTH = 32;
    private static final int IPV6_LENGTH = 128;

    public IPList() {
        this.ipv4Root = new TrieNode();
        this.ipv6Root = new TrieNode();
        this.lock = new ReentrantReadWriteLock();
    }

    public boolean hasItems() {
        lock.readLock().lock();
        try {
            return Arrays.stream(ipv4Root.children).anyMatch(child -> child != null) ||
                   Arrays.stream(ipv6Root.children).anyMatch(child -> child != null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the total number of IP addresses and CIDR subnets in the list
     */
    public int length() {
        lock.readLock().lock();
        try {
            return countTerminalNodes(ipv4Root) + countTerminalNodes(ipv6Root);
        } finally {
            lock.readLock().unlock();
        }
    }

    private int countTerminalNodes(TrieNode node) {
        if (node == null) {
            return 0;
        }
        
        int count = node.isTerminal ? 1 : 0;
        for (TrieNode child : node.children) {
            count += countTerminalNodes(child);
        }
        return count;
    }

    /**
     * Adds an IP address or CIDR subnet to the list.
     */
    public void add(String ipOrCIDR) {
        if (ipOrCIDR == null || ipOrCIDR.isEmpty()) {
            return;
        }

        lock.writeLock().lock();
        try {
            String[] parts = ipOrCIDR.split("/");
            String ipString = parts[0];
            
            InetAddress address = InetAddress.getByName(ipString);
            boolean isIPv6 = address instanceof Inet6Address;
            int prefixLength = parts.length > 1 ? Integer.parseInt(parts[1]) : (isIPv6 ? IPV6_LENGTH : IPV4_LENGTH);

            TrieNode currentNode = isIPv6 ? ipv6Root : ipv4Root;
            byte[] addressBytes = address.getAddress();

            for (byte byteValue : addressBytes) {
                for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                    if (prefixLength == 0) {
                        break;
                    }

                    int bit = (byteValue >> bitIndex) & 1;
                    if (currentNode.children[bit] == null) {
                        currentNode.children[bit] = new TrieNode();
                    }
                    currentNode = currentNode.children[bit];
                    prefixLength--;
                }
            }

            currentNode.isTerminal = true;

        } catch (Exception e) {
            // Invalid IP format - silently ignore
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if an IP address matches any of the IPs or subnets in the list
     */
    public boolean matches(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        lock.readLock().lock();
        try {
            InetAddress address = InetAddress.getByName(ip);
            boolean isIPv6 = address instanceof Inet6Address;
            TrieNode currentNode = isIPv6 ? ipv6Root : ipv4Root;
            byte[] addressBytes = address.getAddress();

            for (byte byteValue : addressBytes) {
                for (int bitIndex = 7; bitIndex >= 0; bitIndex--) {
                    if (currentNode.isTerminal) {
                        return true;
                    }

                    int bit = (byteValue >> bitIndex) & 1;
                    if (currentNode.children[bit] == null) {
                        return false;
                    }

                    currentNode = currentNode.children[bit];
                }
            }

            return currentNode.isTerminal;

        } catch (Exception e) {
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }
}
