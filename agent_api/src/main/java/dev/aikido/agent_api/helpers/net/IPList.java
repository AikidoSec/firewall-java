package dev.aikido.agent_api.helpers.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * IPList implements an efficient IP address and CIDR subnet matching using separate tries for IPv4 and IPv6.
 * Each node in the trie represents a bit in the IP address, allowing for fast prefix matching.
 */
public class IPList {
    private static class TrieNode {
        TrieNode[] children;
        boolean isEndOfSubnet;
        
        TrieNode() {
            children = new TrieNode[2];
            isEndOfSubnet = false;
        }
    }

    private final TrieNode ipv4Root;
    private final TrieNode ipv6Root; 
    private int size;
    private static final int IPV4_LENGTH = 32;
    private static final int IPV6_LENGTH = 128;
    private static final byte[] IPV4_MAPPED_PREFIX = new byte[] {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte)0xff, (byte)0xff
    };

    public IPList() {
        this.ipv4Root = new TrieNode();
        this.ipv6Root = new TrieNode();
        this.size = 0;
    }

    /**
     * Adds an IP address or CIDR subnet to the list.
     */
    public void add(String ipOrCIDR) {
        if (ipOrCIDR == null || ipOrCIDR.isEmpty()) {
            return;
        }

        try {
            String[] parts = ipOrCIDR.split("/");
            InetAddress address = InetAddress.getByName(parts[0]);
            int prefixLength = getPrefixLength(parts, address);
            if (prefixLength < 0) {
                return;
            }

            byte[] addressBytes = address.getAddress();
            if (address instanceof Inet4Address) {
                insertIntoTrie(addressBytes, prefixLength, ipv4Root);
            } else {
                insertIntoTrie(addressBytes, prefixLength, ipv6Root);
            }
        } catch (Exception e) {
            // Invalid IP format - silently ignore
        }
    }

    private int getPrefixLength(String[] parts, InetAddress address) {
        if (parts.length > 1) {
            int prefixLength = Integer.parseInt(parts[1]);
            if (address instanceof Inet4Address && prefixLength > IPV4_LENGTH) {
                return -1;
            }
            if (address instanceof Inet6Address && prefixLength > IPV6_LENGTH) {
                return -1;
            }
            return prefixLength;
        }
        return address instanceof Inet4Address ? IPV4_LENGTH : IPV6_LENGTH;
    }

    private void insertIntoTrie(byte[] addressBytes, int prefixLength, TrieNode root) {
        TrieNode current = root;
        
        for (int i = 0; i < prefixLength; i++) {
            int byteIndex = i / 8;
            int bitIndex = 7 - (i % 8);
            int bit = (addressBytes[byteIndex] >> bitIndex) & 1;
            
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
     */
    public boolean matches(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        try {
            InetAddress address = InetAddress.getByName(ip);
            byte[] addressBytes = address.getAddress();

            if (address instanceof Inet4Address) {
                return matchesInTrie(addressBytes, IPV4_LENGTH, ipv4Root);
            } else {
                // First try matching as IPv6
                if (matchesInTrie(addressBytes, IPV6_LENGTH, ipv6Root)) {
                    return true;
                }
                // Then check if it's an IPv4-mapped address and try matching against IPv4 trie
                if (isIPv4MappedAddress(addressBytes)) {
                    byte[] ipv4Bytes = extractIPv4Bytes(addressBytes);
                    return matchesInTrie(ipv4Bytes, IPV4_LENGTH, ipv4Root);
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isIPv4MappedAddress(byte[] addressBytes) {
        if (addressBytes.length != 16) {
            return false;
        }
        return Arrays.equals(Arrays.copyOfRange(addressBytes, 0, 12), IPV4_MAPPED_PREFIX);
    }

    private byte[] extractIPv4Bytes(byte[] ipv6Bytes) {
        byte[] ipv4Bytes = new byte[4];
        System.arraycopy(ipv6Bytes, 12, ipv4Bytes, 0, 4);
        return ipv4Bytes;
    }

    private boolean matchesInTrie(byte[] bytes, int maxBits, TrieNode root) {
        TrieNode current = root;
        
        for (int i = 0; i < maxBits && current != null; i++) {
            if (current.isEndOfSubnet) {
                return true;
            }
            int byteIndex = i / 8;
            int bitIndex = 7 - (i % 8);
            int bit = (bytes[byteIndex] >> bitIndex) & 1;
            current = current.children[bit];
        }
        
        return current != null && current.isEndOfSubnet;
    }

    /**
     * Returns the number of IP addresses/subnets in the list
     */
    public int length() {
        return size;
    }
}
