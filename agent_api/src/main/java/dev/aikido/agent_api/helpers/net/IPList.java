package dev.aikido.agent_api.helpers.net;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

import java.util.HashSet;
import java.util.Set;

/**
 * IPList implements an efficient IP address and CIDR subnet matching using a binary trie data structure.
 * Each node in the trie represents a bit in the IP address, allowing for fast prefix matching.
 * Supports both IPv4 and IPv6 addresses.
 */
public class IPList {
    /**
     * Node in the binary trie structure. Each node has two children (0 and 1)
     * and tracks whether it represents the end of a subnet.
     */
    private static class TrieNode {
        TrieNode[] children;
        boolean isEndOfSubnet;
        
        TrieNode() {
            children = new TrieNode[2]; // Binary trie - each level represents a bit
            isEndOfSubnet = false;
        }
    }

    private final TrieNode root;
    private int size;
    private static final int IPV4_BIT_LENGTH = 32;
    private static final int IPV6_BIT_LENGTH = 128;

    public IPList() {
        this.root = new TrieNode();
        this.size = 0;
    }

    /**
     * Adds an IP address or CIDR subnet to the list.
     * For single IPs, all bits are matched.
     * For CIDR blocks, only the network prefix bits are matched.
     * Supports both IPv4 and IPv6.
     */
    public void add(String ipOrCIDR) {
        if (ipOrCIDR == null || ipOrCIDR.isEmpty()) {
            return;
        }

        try {
            IPAddress ip = parseIPAddress(ipOrCIDR);
            if (ip != null) {
                insertIntoTrie(ip);
            }
        } catch (Exception e) {
            // Invalid IP format - silently ignore
        }
    }

    /**
     * Parses an IP address or CIDR notation string into an IPAddress object
     */
    private IPAddress parseIPAddress(String ipOrCIDR) {
        try {
            IPAddressString ipAddressString = new IPAddressString(ipOrCIDR);
            if (!ipAddressString.isValid()) {
                return null;
            }
            
            IPAddress ip = ipAddressString.getAddress();
            return ipOrCIDR.contains("/") ? ip.toPrefixBlock() : ip;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Inserts an IP address into the trie structure
     */
    private void insertIntoTrie(IPAddress ip) {
        byte[] bytes = ip.getBytes();
        int maxBits = ip.isIPv4() ? IPV4_BIT_LENGTH : IPV6_BIT_LENGTH;
        int prefixLength = ip.getNetworkPrefixLength() != null ? 
                         Math.min(ip.getNetworkPrefixLength(), maxBits) : maxBits;
        
        TrieNode current = root;
        for (int i = 0; i < prefixLength; i++) {
            int bit = getBitFromBytes(bytes, i);
            
            if (current.children[bit] == null) {
                current.children[bit] = new TrieNode();
            }
            current = current.children[bit];
        }
        
        if (!current.isEndOfSubnet) {
            current.isEndOfSubnet = true;
            size++;
        }
    }

    /**
     * Checks if an IP address matches any of the IPs or subnets in the list
     * Supports both IPv4 and IPv6.
     */
    public boolean matches(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        try {
            IPAddressString ipAddressString = new IPAddressString(ip);
            if (!ipAddressString.isValid()) {
                return false;
            }

            IPAddress ipAddress = ipAddressString.getAddress();
            return matchesInTrie(ipAddress.getBytes(), ipAddress.isIPv4() ? IPV4_BIT_LENGTH : IPV6_BIT_LENGTH);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Traverses the trie to find a match for the given IP address bytes
     */
    private boolean matchesInTrie(byte[] bytes, int maxBits) {
        TrieNode current = root;
        
        for (int i = 0; i < maxBits && current != null; i++) {
            if (current.isEndOfSubnet) {
                return true;
            }
            current = current.children[getBitFromBytes(bytes, i)];
        }
        
        return current != null && current.isEndOfSubnet;
    }

    /**
     * Extracts a specific bit from a byte array representing an IP address
     * Optimized bit extraction using bit shifting
     */
    private int getBitFromBytes(byte[] bytes, int bitPosition) {
        return (bytes[bitPosition >>> 3] >>> (7 - (bitPosition & 7))) & 1;
    }

    /**
     * Returns the number of IP addresses/subnets in the list
     */
    public int length() {
        return size;
    }
}
