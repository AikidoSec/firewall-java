package dev.aikido.agent_api.helpers.net;

import java.util.Arrays;
import java.util.Collection;

// Exact port of zen-internals' IpMatcher (src/ip_matcher/mod.rs).
// Based on https://github.com/demskie/netparser (MIT, Copyright (c) 2019 alex).
public final class IPMatcher {
    private static final int IPV4_BYTES = 4;
    private static final int IPV6_BYTES = 16;
    private static final long NO_INT = Long.MIN_VALUE;
    private static final int NO_BYTE_INDEX = Integer.MIN_VALUE;

    private final int[] ipv4Addresses;
    private final byte[] ipv4Cidrs;
    private final long[] ipv6High;
    private final long[] ipv6Low;
    private final byte[] ipv6Cidrs;
    private final int rejected;

    private static final class Network {
        int byteLength;
        long high;
        long low;
        int cidr;
    }

    private static final class NetworkBuffer {
        long[] high = new long[16];
        long[] low = new long[16];
        byte[] cidrs = new byte[16];
        int count;

        void add(long networkHigh, long networkLow, int cidr) {
            if (count == high.length) {
                int capacity = count * 2;
                high = Arrays.copyOf(high, capacity);
                low = Arrays.copyOf(low, capacity);
                cidrs = Arrays.copyOf(cidrs, capacity);
            }
            high[count] = networkHigh;
            low[count] = networkLow;
            cidrs[count] = (byte) cidr;
            count++;
        }
    }

    public IPMatcher(Collection<String> networks) {
        this(null, networks);
    }

    private IPMatcher(IPMatcher base, Collection<String> networks) {
        NetworkBuffer ipv4 = new NetworkBuffer();
        NetworkBuffer ipv6 = new NetworkBuffer();
        int rejectedCount = 0;
        if (base != null) {
            for (int index = 0; index < base.ipv4Addresses.length; index++) {
                ipv4.add((long) base.ipv4Addresses[index] << 32, 0, base.ipv4Cidrs[index] & 0xff);
            }
            for (int index = 0; index < base.ipv6High.length; index++) {
                ipv6.add(base.ipv6High[index], base.ipv6Low[index], base.ipv6Cidrs[index] & 0xff);
            }
            rejectedCount = base.rejected;
        }

        Network network = new Network();
        if (networks != null) {
            for (String value : networks) {
                if (value == null || !parseBaseNetwork(value, network)) {
                    rejectedCount++;
                } else if (network.byteLength == IPV4_BYTES) {
                    ipv4.add(network.high, network.low, network.cidr);
                } else {
                    ipv6.add(network.high, network.low, network.cidr);
                }
            }
        }

        sortIpv4(ipv4);
        sortIpv6(ipv6);
        int ipv4Count = summarizeSortedNetworks(ipv4, IPV4_BYTES);
        int ipv6Count = summarizeSortedNetworks(ipv6, IPV6_BYTES);

        this.ipv4Addresses = new int[ipv4Count];
        for (int index = 0; index < ipv4Count; index++) {
            this.ipv4Addresses[index] = (int) (ipv4.high[index] >>> 32);
        }
        this.ipv4Cidrs = Arrays.copyOf(ipv4.cidrs, ipv4Count);
        this.ipv6High = Arrays.copyOf(ipv6.high, ipv6Count);
        this.ipv6Low = Arrays.copyOf(ipv6.low, ipv6Count);
        this.ipv6Cidrs = Arrays.copyOf(ipv6.cidrs, ipv6Count);
        this.rejected = rejectedCount;
    }

    IPMatcher withNetworks(Collection<String> networks) {
        return new IPMatcher(this, networks);
    }

    public int rejectedCount() {
        return rejected;
    }

    public long memorySize() {
        return ipv4Addresses.length * (long) Integer.BYTES
                + ipv4Cidrs.length
                + ipv6High.length * (long) Long.BYTES
                + ipv6Low.length * (long) Long.BYTES
                + ipv6Cidrs.length;
    }

    int size() {
        return ipv4Addresses.length + ipv6High.length;
    }

    public boolean has(String value) {
        if (value == null) {
            return false;
        }
        long strict = parseStrictIpv4Address(value);
        if (strict != NO_INT) {
            return hasIpv4((int) strict);
        }

        Network network = new Network();
        if (!parseBaseNetwork(value, network)) {
            return false;
        }
        long mapped = mappedIpv4(network);
        if (mapped != NO_INT && hasIpv4((int) mapped)) {
            return true;
        }
        return network.byteLength == IPV4_BYTES ? hasInIpv4(network) : hasInIpv6(network);
    }

    private boolean hasInIpv4(Network network) {
        int low = 0;
        int high = ipv4Addresses.length;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (compareNetworks((long) ipv4Addresses[middle] << 32, 0, ipv4Cidrs[middle] & 0xff, network) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        int index = low;
        return (index < ipv4Addresses.length
                        && contains(
                                (long) ipv4Addresses[index] << 32,
                                0,
                                ipv4Cidrs[index] & 0xff,
                                network.high,
                                network.low,
                                network.cidr))
                || (index > 0
                        && contains(
                                (long) ipv4Addresses[index - 1] << 32,
                                0,
                                ipv4Cidrs[index - 1] & 0xff,
                                network.high,
                                network.low,
                                network.cidr));
    }

    private boolean hasInIpv6(Network network) {
        int low = 0;
        int high = ipv6High.length;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (compareNetworks(ipv6High[middle], ipv6Low[middle], ipv6Cidrs[middle] & 0xff, network) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        int index = low;
        return (index < ipv6High.length
                        && contains(
                                ipv6High[index],
                                ipv6Low[index],
                                ipv6Cidrs[index] & 0xff,
                                network.high,
                                network.low,
                                network.cidr))
                || (index > 0
                        && contains(
                                ipv6High[index - 1],
                                ipv6Low[index - 1],
                                ipv6Cidrs[index - 1] & 0xff,
                                network.high,
                                network.low,
                                network.cidr));
    }

    private boolean hasIpv4(int address) {
        int low = 0;
        int high = ipv4Addresses.length;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (Integer.compareUnsigned(ipv4Addresses[middle], address) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        if (low == 0) {
            return false;
        }

        int candidate = low - 1;
        int cidr = ipv4Cidrs[candidate] & 0xff;
        if (cidr == 0) {
            return true;
        }
        return address >>> (32 - cidr) == ipv4Addresses[candidate] >>> (32 - cidr);
    }

    private static int compareNetworks(long high, long low, int cidr, Network other) {
        int addresses = compareAddresses(high, low, other.high, other.low);
        return addresses != 0 ? addresses : Integer.compare(cidr, other.cidr);
    }

    private static int compareAddresses(long leftHigh, long leftLow, long rightHigh, long rightLow) {
        int high = Long.compareUnsigned(leftHigh, rightHigh);
        return high != 0 ? high : Long.compareUnsigned(leftLow, rightLow);
    }

    private static boolean contains(long high, long low, int cidr, long otherHigh, long otherLow, int otherCidr) {
        if (cidr == 0) {
            return true;
        }
        if (otherCidr == 0 || cidr > otherCidr || compareAddresses(high, low, otherHigh, otherLow) > 0) {
            return false;
        }
        return maskHigh(high ^ otherHigh, cidr) == 0 && maskLow(low ^ otherLow, cidr) == 0;
    }

    private static boolean isBaseAddress(long high, long low, int byteLength, int cidr) {
        if (cidr < 0 || cidr > byteLength * 8) {
            return false;
        }
        return maskHigh(high, cidr) == high && maskLow(low, cidr) == low;
    }

    private static long maskHigh(long high, int cidr) {
        if (cidr >= 64) {
            return high;
        }
        return cidr == 0 ? 0 : high & (-1L << (64 - cidr));
    }

    private static long maskLow(long low, int cidr) {
        if (cidr <= 64) {
            return 0;
        }
        return cidr >= 128 ? low : low & (-1L << (128 - cidr));
    }

    private static void applySubnetMask(Network network) {
        network.high = maskHigh(network.high, network.cidr);
        network.low = maskLow(network.low, network.cidr);
    }

    // Top alignment makes the IPv4 and IPv6 increments the same 128-bit addition.
    private static long[] next(long high, long low, int cidr) {
        if (cidr == 0) {
            return null;
        }

        int position = 128 - cidr;
        if (position >= 64) {
            long nextHigh = high + (1L << (position - 64));
            return Long.compareUnsigned(nextHigh, high) < 0 ? null : new long[] {nextHigh, low};
        }
        long nextLow = low + (1L << position);
        if (Long.compareUnsigned(nextLow, low) >= 0) {
            return new long[] {high, nextLow};
        }
        return high == -1L ? null : new long[] {high + 1, nextLow};
    }

    private static boolean adjacent(long high, long low, int cidr, long otherHigh, long otherLow, int otherCidr) {
        if (cidr == 0 || otherCidr == 0) {
            return true;
        }

        long[] next;
        long secondHigh;
        long secondLow;
        if (compareAddresses(high, low, otherHigh, otherLow) < 0) {
            next = next(high, low, cidr);
            secondHigh = otherHigh;
            secondLow = otherLow;
        } else {
            next = next(otherHigh, otherLow, otherCidr);
            secondHigh = high;
            secondLow = low;
        }
        return next != null && compareAddresses(next[0], next[1], secondHigh, secondLow) == 0;
    }

    // Packed (address << 8 | cidr) keys sort by address, then cidr.
    private static void sortIpv4(NetworkBuffer networks) {
        long[] keys = new long[networks.count];
        for (int index = 0; index < networks.count; index++) {
            keys[index] = (networks.high[index] >>> 24) | (networks.cidrs[index] & 0xff);
        }
        Arrays.sort(keys);
        for (int index = 0; index < networks.count; index++) {
            networks.high[index] = (keys[index] >>> 8) << 32;
            networks.low[index] = 0;
            networks.cidrs[index] = (byte) keys[index];
        }
    }

    // Heapsort sorts the parallel arrays in place without boxing.
    private static void sortIpv6(NetworkBuffer networks) {
        for (int index = networks.count / 2 - 1; index >= 0; index--) {
            siftDown(networks, index, networks.count);
        }
        for (int end = networks.count - 1; end > 0; end--) {
            swap(networks, 0, end);
            siftDown(networks, 0, end);
        }
    }

    private static void siftDown(NetworkBuffer networks, int root, int end) {
        while (true) {
            int child = 2 * root + 1;
            if (child >= end) {
                return;
            }
            if (child + 1 < end && compareAt(networks, child, child + 1) < 0) {
                child++;
            }
            if (compareAt(networks, root, child) >= 0) {
                return;
            }
            swap(networks, root, child);
            root = child;
        }
    }

    private static int compareAt(NetworkBuffer networks, int left, int right) {
        int addresses =
                compareAddresses(networks.high[left], networks.low[left], networks.high[right], networks.low[right]);
        return addresses != 0 ? addresses : Integer.compare(networks.cidrs[left] & 0xff, networks.cidrs[right] & 0xff);
    }

    private static void swap(NetworkBuffer networks, int left, int right) {
        long high = networks.high[left];
        networks.high[left] = networks.high[right];
        networks.high[right] = high;
        long low = networks.low[left];
        networks.low[left] = networks.low[right];
        networks.low[right] = low;
        byte cidr = networks.cidrs[left];
        networks.cidrs[left] = networks.cidrs[right];
        networks.cidrs[right] = cidr;
    }

    // In place: the output never outruns the input.
    private static int summarizeSortedNetworks(NetworkBuffer sorted, int byteLength) {
        long[] high = sorted.high;
        long[] low = sorted.low;
        byte[] cidrs = sorted.cidrs;
        int length = 0;
        for (int index = 0; index < sorted.count; index++) {
            long networkHigh = high[index];
            long networkLow = low[index];
            int networkCidr = cidrs[index] & 0xff;
            if (length > 0
                    && contains(
                            high[length - 1],
                            low[length - 1],
                            cidrs[length - 1] & 0xff,
                            networkHigh,
                            networkLow,
                            networkCidr)) {
                continue;
            }

            high[length] = networkHigh;
            low[length] = networkLow;
            cidrs[length] = (byte) networkCidr;
            length++;
            while (length >= 2) {
                int second = length - 1;
                int first = second - 1;
                int firstCidr = cidrs[first] & 0xff;
                if (firstCidr != (cidrs[second] & 0xff)
                        || !isBaseAddress(high[first], low[first], byteLength, firstCidr - 1)
                        || !adjacent(
                                high[first], low[first], firstCidr, high[second], low[second], cidrs[second] & 0xff)) {
                    break;
                }

                firstCidr--;
                cidrs[first] = (byte) firstCidr;
                high[first] = maskHigh(high[first], firstCidr);
                low[first] = maskLow(low[first], firstCidr);
                length--;
            }
        }
        return length;
    }

    private static boolean parseBaseNetwork(String value, Network network) {
        if (!parseNetwork(value, network)) {
            return false;
        }
        applySubnetMask(network);
        return true;
    }

    private static boolean parseNetwork(String value, Network network) {
        long trimmed = trimAsciiWhitespace(value, 0, value.length());
        int start = sliceStart(trimmed);
        int end = sliceEnd(trimmed);
        int slash = indexOf(value, '/', start, end);
        int addressEnd = slash < 0 ? end : slash;
        if (slash >= 0 && indexOf(value, '/', slash + 1, end) >= 0) {
            return false;
        }

        int isIpv4 = looksLikeIpv4(value, start, end);
        if (isIpv4 < 0) {
            return false;
        }
        int byteLength = isIpv4 == 1 ? IPV4_BYTES : IPV6_BYTES;
        int maxCidr = byteLength * 8;
        int cidr = slash < 0 ? maxCidr : parseIntRange(value, slash + 1, end, maxCidr);
        if (cidr < 0) {
            return false;
        }

        network.high = 0;
        network.low = 0;
        boolean parsed = isIpv4 == 1
                ? parseIpv4(value, start, addressEnd, network)
                : parseIpv6(value, start, addressEnd, network);
        if (!parsed) {
            return false;
        }
        network.byteLength = byteLength;
        network.cidr = cidr;
        return true;
    }

    // A (start, end) slice of the input, packed into a long to avoid allocating.
    private static long slice(int start, int end) {
        return ((long) start << 32) | end;
    }

    private static int sliceStart(long slice) {
        return (int) (slice >>> 32);
    }

    private static int sliceEnd(long slice) {
        return (int) slice;
    }

    private static int indexOf(String value, char character, int start, int end) {
        for (int index = start; index < end; index++) {
            if (value.charAt(index) == character) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isAsciiDigit(char character) {
        return character >= '0' && character <= '9';
    }

    private static boolean isAsciiWhitespace(char character) {
        return character == ' '
                || character == '\t'
                || character == '\n'
                || character == '\r'
                || character == 0x0c
                || character == 0x0b;
    }

    private static long trimAsciiWhitespace(String value, int start, int end) {
        while (start < end && isAsciiWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isAsciiWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return slice(start, end);
    }

    private static long parseInt10(String value, int start, int end) {
        int index = start;
        while (index < end && isAsciiWhitespace(value.charAt(index))) {
            index++;
        }

        boolean negative = false;
        if (index < end && (value.charAt(index) == '+' || value.charAt(index) == '-')) {
            negative = value.charAt(index) == '-';
            index++;
        }

        int digitsStart = index;
        long number = 0;
        while (index < end && isAsciiDigit(value.charAt(index))) {
            int digit = value.charAt(index) - '0';
            // Rust returns None above i64::MAX.
            if (number > (Long.MAX_VALUE - digit) / 10) {
                return NO_INT;
            }
            number = number * 10 + digit;
            index++;
        }
        if (index == digitsStart) {
            return NO_INT;
        }
        return negative ? -number : number;
    }

    private static int parseIntRange(String value, int start, int end, int max) {
        int number = 0;
        int digits = 0;
        for (int index = start; index < end && isAsciiDigit(value.charAt(index)); index++) {
            number = number * 10 + (value.charAt(index) - '0');
            digits++;
            if (number > max) {
                return -1;
            }
        }
        return digits > 0 && number <= max ? number : -1;
    }

    // 1 = IPv4, 0 = IPv6, -1 = neither.
    private static int looksLikeIpv4(String value, int start, int end) {
        for (int index = start; index < end; index++) {
            char character = value.charAt(index);
            if (character == '.') {
                return 1;
            }
            if (character == ':') {
                return 0;
            }
        }
        return -1;
    }

    private static boolean parseIpv4(String value, int start, int end, Network network) {
        int partStart = start;
        for (int byteIndex = 0; byteIndex < IPV4_BYTES; byteIndex++) {
            if (partStart > end) {
                return false;
            }
            int partEnd = indexOf(value, '.', partStart, end);
            if (partEnd < 0) {
                partEnd = end;
            }
            long parsed = parseInt10(value, partStart, partEnd);
            if (parsed < 0 || parsed > 255) {
                return false;
            }
            setByte(network, byteIndex, (int) parsed);
            partStart = partEnd + 1;
        }
        return partStart > end;
    }

    private static long parseStrictIpv4Address(String value) {
        int length = value.length();
        int position = 0;
        long address = 0;

        for (int index = 0; index < IPV4_BYTES; index++) {
            int start = position;
            int number = 0;
            while (position < length && isAsciiDigit(value.charAt(position))) {
                number = number * 10 + (value.charAt(position) - '0');
                if (number > 255) {
                    return NO_INT;
                }
                position++;
            }
            if (position == start) {
                return NO_INT;
            }
            address = (address << 8) | number;

            if (index < 3) {
                if (position >= length || value.charAt(position) != '.') {
                    return NO_INT;
                }
                position++;
            }
        }
        return position == length ? address : NO_INT;
    }

    private static long mappedIpv4(Network network) {
        boolean isMappedAddress = network.byteLength == IPV6_BYTES
                && network.cidr == 128
                && network.high == 0
                && (network.low >>> 32) == 0xffffL;
        if (!isMappedAddress) {
            return NO_INT;
        }
        return network.low & 0xffffffffL;
    }

    private static void setByte(Network network, int index, int value) {
        int shift = 56 - 8 * (index % 8);
        if (index < 8) {
            network.high = (network.high & ~(0xffL << shift)) | ((long) value << shift);
        } else {
            network.low = (network.low & ~(0xffL << shift)) | ((long) value << shift);
        }
    }

    private static int parseHextet(String value, int start, int end) {
        long trimmed = trimAsciiWhitespace(value, start, end);
        int trimmedLength = sliceEnd(trimmed) - sliceStart(trimmed);
        // Rust counts UTF-8 bytes; non-ASCII fails the digit loop below anyway.
        if (trimmedLength == 0 || trimmedLength > 4) {
            return -1;
        }

        int parsed = 0;
        for (int index = start; index < end; index++) {
            char character = value.charAt(index);
            int digit;
            if (character >= '0' && character <= '9') {
                digit = character - '0';
            } else if (character >= 'a' && character <= 'f') {
                digit = character - 'a' + 10;
            } else if (character >= 'A' && character <= 'F') {
                digit = character - 'A' + 10;
            } else {
                return -1;
            }
            parsed = parsed * 16 + digit;
        }
        return parsed;
    }

    private static long removeBrackets(String value, int start, int end) {
        if (start == end || value.charAt(start) != '[') {
            return slice(start, end);
        }

        for (int closingBracket = end - 1; closingBracket >= start; closingBracket--) {
            if (value.charAt(closingBracket) == ']') {
                return slice(start + 1, closingBracket);
            }
        }
        return slice(start, end);
    }

    private static long removePortInfo(String value, int start, int end) {
        for (int index = start; index < end; index++) {
            char character = value.charAt(index);
            if (character == '#' || character == 'p' || character == '.') {
                return trimAsciiWhitespace(value, start, index);
            }
        }
        return slice(start, end);
    }

    private static boolean hasFourIpv4Parts(String value, int start, int end) {
        int dots = 0;
        for (int index = start; index < end; index++) {
            if (value.charAt(index) == '.') {
                dots++;
            }
        }
        return dots == 3;
    }

    private static int parseIpv4Part(
            String value, int start, int end, Network network, int byteIndex, boolean reverse) {
        int firstDot = indexOf(value, '.', start, end);
        int secondDot = firstDot < 0 ? -1 : indexOf(value, '.', firstDot + 1, end);
        int thirdDot = secondDot < 0 ? -1 : indexOf(value, '.', secondDot + 1, end);
        if (thirdDot < 0 || indexOf(value, '.', thirdDot + 1, end) >= 0) {
            return NO_BYTE_INDEX;
        }

        for (int offset = 0; offset < 4; offset++) {
            int index = reverse ? 3 - offset : offset;
            int partStart = index == 0 ? start : index == 1 ? firstDot + 1 : index == 2 ? secondDot + 1 : thirdDot + 1;
            int partEnd = index == 0 ? firstDot : index == 1 ? secondDot : index == 2 ? thirdDot : end;
            long parsed = parseInt10(value, partStart, partEnd);
            if (parsed < 0 || parsed > 255 || byteIndex < 0 || byteIndex >= 16) {
                return NO_BYTE_INDEX;
            }
            setByte(network, byteIndex, (int) parsed);
            byteIndex += reverse ? -1 : 1;
        }
        return byteIndex;
    }

    private static int parseIpv6LeftHalf(Network network, String value, int start, int end) {
        int byteIndex = 0;
        if (start == end) {
            return byteIndex;
        }

        int partStart = start;
        while (true) {
            int partEnd = indexOf(value, ':', partStart, end);
            if (partEnd < 0) {
                partEnd = end;
            }
            if (byteIndex >= 16) {
                return NO_BYTE_INDEX;
            }
            if (hasFourIpv4Parts(value, partStart, partEnd)) {
                byteIndex = parseIpv4Part(value, partStart, partEnd, network, byteIndex, false);
                if (byteIndex == NO_BYTE_INDEX) {
                    return NO_BYTE_INDEX;
                }
            } else {
                int parsed = parseHextet(value, partStart, partEnd);
                if (parsed < 0 || byteIndex + 1 >= 16) {
                    return NO_BYTE_INDEX;
                }
                setByte(network, byteIndex, parsed / 256);
                byteIndex++;
                setByte(network, byteIndex, parsed % 256);
                byteIndex++;
            }
            if (partEnd == end) {
                return byteIndex;
            }
            partStart = partEnd + 1;
        }
    }

    private static boolean parseIpv6RightHalf(Network network, String value, int start, int end, int leftByteIndex) {
        if (start == end) {
            return true;
        }

        int rightByteIndex = 15;
        int partEnd = end;
        for (int offset = 0; ; offset++) {
            int colon = partEnd - 1;
            while (colon >= start && value.charAt(colon) != ':') {
                colon--;
            }
            int partStart = colon < start ? start : colon + 1;

            long trimmed = trimAsciiWhitespace(value, partStart, partEnd);
            if (sliceStart(trimmed) == sliceEnd(trimmed) || leftByteIndex > rightByteIndex) {
                return false;
            }
            if (hasFourIpv4Parts(value, partStart, partEnd)) {
                rightByteIndex = parseIpv4Part(value, partStart, partEnd, network, rightByteIndex, true);
                if (rightByteIndex == NO_BYTE_INDEX) {
                    return false;
                }
            } else {
                long part = offset == 0 ? removePortInfo(value, partStart, partEnd) : slice(partStart, partEnd);
                int parsed = parseHextet(value, sliceStart(part), sliceEnd(part));
                if (parsed < 0 || rightByteIndex - 1 < 0) {
                    return false;
                }
                setByte(network, rightByteIndex, parsed % 256);
                rightByteIndex--;
                setByte(network, rightByteIndex, parsed / 256);
                rightByteIndex--;
            }

            if (colon < start) {
                return true;
            }
            partEnd = colon;
        }
    }

    private static boolean parseIpv6(String value, int start, int end, Network network) {
        long withoutBrackets = removeBrackets(value, start, end);
        start = sliceStart(withoutBrackets);
        end = sliceEnd(withoutBrackets);
        if (start == end) {
            return false;
        }
        int zoneIndex = indexOf(value, '%', start, end);
        if (zoneIndex >= 0) {
            if (zoneIndex == end - 1) {
                return false;
            }
            end = zoneIndex;
        }
        if (end - start == 2 && value.charAt(start) == ':' && value.charAt(start + 1) == ':') {
            return true;
        }

        int splitIndex = value.indexOf("::", start);
        if (splitIndex + 2 > end) {
            splitIndex = -1;
        }
        if (splitIndex >= 0) {
            int nextSplit = value.indexOf("::", splitIndex + 2);
            if (nextSplit >= 0 && nextSplit + 2 <= end) {
                return false;
            }
        }

        int leftLength = splitIndex >= 0 ? splitIndex : end;
        int leftByteIndex = parseIpv6LeftHalf(network, value, start, leftLength);
        if (leftByteIndex == NO_BYTE_INDEX) {
            return false;
        }
        return splitIndex < 0 || parseIpv6RightHalf(network, value, splitIndex + 2, end, leftByteIndex);
    }
}
