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
            // Pre-split the string to avoid multiple splits
            int slashIndex = ipOrCIDR.indexOf('/');
            String ipPart = slashIndex == -1 ? ipOrCIDR : ipOrCIDR.substring(0, slashIndex);
            InetAddress address = InetAddress.getByName(ipPart);
            int prefixLength;
            
            if (slashIndex == -1) {
                prefixLength = address instanceof Inet4Address ? IPV4_LENGTH : IPV6_LENGTH;
            } else {
                prefixLength = Integer.parseInt(ipOrCIDR.substring(slashIndex + 1));
                if ((address instanceof Inet4Address && prefixLength > IPV4_LENGTH) ||
                    (address instanceof Inet6Address && prefixLength > IPV6_LENGTH)) {
                    return;
                }
            }

            byte[] addressBytes = address.getAddress();
            
            // Handle IPv4-mapped IPv6 addresses when adding
            if (address instanceof Inet6Address && isIPv4MappedAddressFast(addressBytes)) {
                byte[] ipv4Bytes = extractIPv4Bytes(addressBytes);
                // Adjust prefix length for IPv4 part if needed
                int ipv4PrefixLength = prefixLength > 96 ? prefixLength - 96 : 0;
                if (ipv4PrefixLength > 0 && ipv4PrefixLength <= IPV4_LENGTH) {
                    insertIntoTrie(ipv4Bytes, ipv4PrefixLength, ipv4Root);
                }
            }
            
            insertIntoTrie(addressBytes, prefixLength, address instanceof Inet4Address ? ipv4Root : ipv6Root);
            
        } catch (Exception e) {
            // Invalid IP format - silently ignore
        }
    }

    private void insertIntoTrie(byte[] addressBytes, int prefixLength, TrieNode root) {
        TrieNode current = root;
        int bitIndex = 0;
        
        // Process bit by bit for more accurate prefix matching
        for (int i = 0; i < prefixLength; i++) {
            int bytePos = bitIndex / 8;
            int bitPos = 7 - (bitIndex % 8);
            int bit = (addressBytes[bytePos] >> bitPos) & 1;
            
            if (current.children[bit] == null) {
                current.children[bit] = new TrieNode();
            }
            current = current.children[bit];
            bitIndex++;
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
            }
            
            // For IPv6, first check if it's an IPv4-mapped address
            if (isIPv4MappedAddressFast(addressBytes)) {
                byte[] ipv4Bytes = extractIPv4Bytes(addressBytes);
                if (matchesInTrie(ipv4Bytes, IPV4_LENGTH, ipv4Root)) {
                    return true;
                }
            }
            
            // Then check native IPv6 format
            return matchesInTrie(addressBytes, IPV6_LENGTH, ipv6Root);
            
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isIPv4MappedAddressFast(byte[] addressBytes) {
        if (addressBytes.length != 16) return false;
        
        // Check the first 10 bytes are zero and bytes 10-11 are 0xFF
        for (int i = 0; i < 10; i++) {
            if (addressBytes[i] != 0) return false;
        }
        return addressBytes[10] == (byte)0xff && addressBytes[11] == (byte)0xff;
    }

    private byte[] extractIPv4Bytes(byte[] ipv6Bytes) {
        byte[] ipv4Bytes = new byte[4];
        System.arraycopy(ipv6Bytes, 12, ipv4Bytes, 0, 4);
        return ipv4Bytes;
    }

    private boolean matchesInTrie(byte[] bytes, int maxBits, TrieNode root) {
        TrieNode current = root;
        int bitIndex = 0;
        
        while (bitIndex < maxBits && current != null) {
            if (current.isEndOfSubnet) {
                return true;
            }
            
            int bytePos = bitIndex / 8;
            int bitPos = 7 - (bitIndex % 8);
            int bit = (bytes[bytePos] >> bitPos) & 1;
            
            current = current.children[bit];
            bitIndex++;
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
