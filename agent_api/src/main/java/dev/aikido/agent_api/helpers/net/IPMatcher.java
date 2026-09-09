package dev.aikido.agent_api.helpers.net;

import java.util.Arrays;
import java.util.Collection;

// This parser and matcher are based on firewall-node's netparser-derived implementation.
// netparser is MIT licensed, Copyright (c) 2019 alex.
final class IPMatcher {
  private static final int IPV4 = 4;
  private static final int IPV6 = 16;
  private static final int IPV4_BITS = 32;
  private static final int IPV6_BITS = 128;
  private static final ThreadLocal<ParsedNetwork> LOOKUP_NETWORK =
      ThreadLocal.withInitial(ParsedNetwork::new);
  private static final IPMatcher EMPTY =
      new IPMatcher(new int[0], new byte[0], new long[0], new long[0], new byte[0]);

  private final int[] ipv4Addresses;
  private final byte[] ipv4Prefixes;
  private final long[] ipv6HighAddresses;
  private final long[] ipv6LowAddresses;
  private final byte[] ipv6Prefixes;

  private IPMatcher(
      int[] ipv4Addresses,
      byte[] ipv4Prefixes,
      long[] ipv6HighAddresses,
      long[] ipv6LowAddresses,
      byte[] ipv6Prefixes) {
    this.ipv4Addresses = ipv4Addresses;
    this.ipv4Prefixes = ipv4Prefixes;
    this.ipv6HighAddresses = ipv6HighAddresses;
    this.ipv6LowAddresses = ipv6LowAddresses;
    this.ipv6Prefixes = ipv6Prefixes;
  }

  static IPMatcher from(Collection<String> networks) {
    if (networks == null || networks.isEmpty()) {
      return EMPTY;
    }

    IPv4Builder ipv4 = new IPv4Builder();
    IPv6Builder ipv6 = new IPv6Builder();
    ParsedNetwork parsed = new ParsedNetwork();
    for (String network : networks) {
      if (!Parser.parseBaseNetwork(network, parsed)) {
        continue;
      }
      addParsedNetwork(ipv4, ipv6, parsed);
    }

    IPv4Networks compactIPv4 = ipv4.build();
    IPv6Networks compactIPv6 = ipv6.build();
    if (compactIPv4.addresses.length == 0 && compactIPv6.highAddresses.length == 0) {
      return EMPTY;
    }
    return new IPMatcher(
        compactIPv4.addresses,
        compactIPv4.prefixes,
        compactIPv6.highAddresses,
        compactIPv6.lowAddresses,
        compactIPv6.prefixes);
  }

  IPMatcher add(String network) {
    ParsedNetwork parsed = new ParsedNetwork();
    if (!Parser.parseBaseNetwork(network, parsed)) {
      return this;
    }
    normalizeMappedNetwork(parsed);
    if (parsed.version == IPV4) {
      IPv4Builder builder = new IPv4Builder();
      for (int index = 0; index < ipv4Addresses.length; index++) {
        builder.add(ipv4Addresses[index], Byte.toUnsignedInt(ipv4Prefixes[index]));
      }
      builder.add(parsed.ipv4Address, parsed.prefix);
      IPv4Networks compact = builder.build();
      return new IPMatcher(
          compact.addresses, compact.prefixes, ipv6HighAddresses, ipv6LowAddresses, ipv6Prefixes);
    }

    IPv6Builder builder = new IPv6Builder();
    for (int index = 0; index < ipv6HighAddresses.length; index++) {
      builder.add(
          ipv6HighAddresses[index],
          ipv6LowAddresses[index],
          Byte.toUnsignedInt(ipv6Prefixes[index]));
    }
    builder.add(parsed.ipv6HighAddress, parsed.ipv6LowAddress, parsed.prefix);
    IPv6Networks compact = builder.build();
    return new IPMatcher(
        ipv4Addresses, ipv4Prefixes, compact.highAddresses, compact.lowAddresses, compact.prefixes);
  }

  boolean matches(String value) {
    if (value == null) {
      return false;
    }

    ParsedNetwork parsed = LOOKUP_NETWORK.get();
    if (Parser.parseStrictIPv4Address(value, parsed)) {
      return containsIPv4(parsed.ipv4Address, IPV4_BITS);
    }
    if (!Parser.parseBaseNetwork(value, parsed)) {
      return false;
    }
    if (parsed.version == IPV4) {
      return containsIPv4(parsed.ipv4Address, parsed.prefix);
    }
    if (containsIPv6(parsed.ipv6HighAddress, parsed.ipv6LowAddress, parsed.prefix)) {
      return true;
    }
    if (isIPv4Mapped(parsed.ipv6HighAddress, parsed.ipv6LowAddress)) {
      return containsIPv4((int) parsed.ipv6LowAddress, IPV4_BITS);
    }
    return false;
  }

  int size() {
    return ipv4Addresses.length + ipv6HighAddresses.length;
  }

  private static void addParsedNetwork(IPv4Builder ipv4, IPv6Builder ipv6, ParsedNetwork parsed) {
    normalizeMappedNetwork(parsed);
    if (parsed.version == IPV4) {
      ipv4.add(parsed.ipv4Address, parsed.prefix);
    } else {
      ipv6.add(parsed.ipv6HighAddress, parsed.ipv6LowAddress, parsed.prefix);
    }
  }

  private static void normalizeMappedNetwork(ParsedNetwork parsed) {
    if (parsed.version == IPV6
        && parsed.prefix >= 96
        && isIPv4Mapped(parsed.ipv6HighAddress, parsed.ipv6LowAddress)) {
      parsed.version = IPV4;
      parsed.ipv4Address = (int) parsed.ipv6LowAddress;
      parsed.prefix -= 96;
    }
  }

  private static boolean isIPv4Mapped(long high, long low) {
    return high == 0 && (low >>> 32) == 0x0000ffffL;
  }

  private boolean containsIPv4(int address, int prefix) {
    int index = upperBoundIPv4(address, prefix) - 1;
    if (index < 0) {
      return false;
    }
    return containsIPv4(
        ipv4Addresses[index], Byte.toUnsignedInt(ipv4Prefixes[index]), address, prefix);
  }

  private int upperBoundIPv4(int address, int prefix) {
    int left = 0;
    int right = ipv4Addresses.length;
    while (left < right) {
      int middle = (left + right) >>> 1;
      if (compareIPv4(
              ipv4Addresses[middle], Byte.toUnsignedInt(ipv4Prefixes[middle]), address, prefix)
          <= 0) {
        left = middle + 1;
      } else {
        right = middle;
      }
    }
    return left;
  }

  private boolean containsIPv6(long high, long low, int prefix) {
    int index = upperBoundIPv6(high, low, prefix) - 1;
    if (index < 0) {
      return false;
    }
    return containsIPv6(
        ipv6HighAddresses[index],
        ipv6LowAddresses[index],
        Byte.toUnsignedInt(ipv6Prefixes[index]),
        high,
        low,
        prefix);
  }

  private int upperBoundIPv6(long high, long low, int prefix) {
    int left = 0;
    int right = ipv6HighAddresses.length;
    while (left < right) {
      int middle = (left + right) >>> 1;
      if (compareIPv6(
              ipv6HighAddresses[middle],
              ipv6LowAddresses[middle],
              Byte.toUnsignedInt(ipv6Prefixes[middle]),
              high,
              low,
              prefix)
          <= 0) {
        left = middle + 1;
      } else {
        right = middle;
      }
    }
    return left;
  }

  private static int compareIPv4(
      int leftAddress, int leftPrefix, int rightAddress, int rightPrefix) {
    int addressComparison = Integer.compareUnsigned(leftAddress, rightAddress);
    return addressComparison != 0 ? addressComparison : Integer.compare(leftPrefix, rightPrefix);
  }

  private static int compareIPv6(
      long leftHigh, long leftLow, int leftPrefix, long rightHigh, long rightLow, int rightPrefix) {
    int highComparison = Long.compareUnsigned(leftHigh, rightHigh);
    if (highComparison != 0) {
      return highComparison;
    }
    int lowComparison = Long.compareUnsigned(leftLow, rightLow);
    return lowComparison != 0 ? lowComparison : Integer.compare(leftPrefix, rightPrefix);
  }

  private static boolean containsIPv4(
      int networkAddress, int networkPrefix, int otherAddress, int otherPrefix) {
    if (networkPrefix == 0) {
      return true;
    }
    if (otherPrefix == 0 || networkPrefix > otherPrefix) {
      return false;
    }
    int mask = -1 << (IPV4_BITS - networkPrefix);
    return (networkAddress & mask) == (otherAddress & mask);
  }

  private static boolean containsIPv6(
      long networkHigh,
      long networkLow,
      int networkPrefix,
      long otherHigh,
      long otherLow,
      int otherPrefix) {
    if (networkPrefix == 0) {
      return true;
    }
    if (otherPrefix == 0 || networkPrefix > otherPrefix) {
      return false;
    }
    if (networkPrefix <= Long.SIZE) {
      long mask = networkPrefix == Long.SIZE ? -1L : -1L << (Long.SIZE - networkPrefix);
      return (networkHigh & mask) == (otherHigh & mask);
    }
    int lowPrefix = networkPrefix - Long.SIZE;
    long mask = lowPrefix == Long.SIZE ? -1L : -1L << (Long.SIZE - lowPrefix);
    return networkHigh == otherHigh && (networkLow & mask) == (otherLow & mask);
  }

  private static int maskIPv4(int address, int prefix) {
    return prefix == 0 ? 0 : address & (-1 << (IPV4_BITS - prefix));
  }

  private static long maskIPv6High(long high, int prefix) {
    if (prefix == 0) {
      return 0;
    }
    if (prefix >= Long.SIZE) {
      return high;
    }
    return high & (-1L << (Long.SIZE - prefix));
  }

  private static long maskIPv6Low(long low, int prefix) {
    if (prefix <= Long.SIZE) {
      return 0;
    }
    int lowPrefix = prefix - Long.SIZE;
    return lowPrefix == Long.SIZE ? low : low & (-1L << (Long.SIZE - lowPrefix));
  }

  private record IPv4Networks(int[] addresses, byte[] prefixes) {}

  private record IPv6Networks(long[] highAddresses, long[] lowAddresses, byte[] prefixes) {}

  private static final class IPv4Builder {
    private long[] networks = new long[16];
    private int size;

    void add(int address, int prefix) {
      if (size == networks.length) {
        networks = Arrays.copyOf(networks, size * 2);
      }
      networks[size++] = encode(address, prefix);
    }

    IPv4Networks build() {
      if (size == 0) {
        return new IPv4Networks(new int[0], new byte[0]);
      }
      Arrays.sort(networks, 0, size);
      int summarizedSize = summarize();
      int[] addresses = new int[summarizedSize];
      byte[] prefixes = new byte[summarizedSize];
      for (int index = 0; index < summarizedSize; index++) {
        addresses[index] = decodeAddress(networks[index]);
        prefixes[index] = (byte) decodePrefix(networks[index]);
      }
      return new IPv4Networks(addresses, prefixes);
    }

    private int summarize() {
      int outputSize = 0;
      for (int index = 0; index < size; index++) {
        int address = decodeAddress(networks[index]);
        int prefix = decodePrefix(networks[index]);
        if (outputSize > 0) {
          int previousAddress = decodeAddress(networks[outputSize - 1]);
          int previousPrefix = decodePrefix(networks[outputSize - 1]);
          if (containsIPv4(previousAddress, previousPrefix, address, prefix)) {
            continue;
          }
        }

        networks[outputSize++] = networks[index];
        while (outputSize >= 2) {
          int firstAddress = decodeAddress(networks[outputSize - 2]);
          int firstPrefix = decodePrefix(networks[outputSize - 2]);
          int secondAddress = decodeAddress(networks[outputSize - 1]);
          int secondPrefix = decodePrefix(networks[outputSize - 1]);
          if (firstPrefix != secondPrefix
              || firstPrefix == 0
              || firstAddress != maskIPv4(firstAddress, firstPrefix - 1)
              || Integer.toUnsignedLong(firstAddress) + (1L << (IPV4_BITS - firstPrefix))
                  != Integer.toUnsignedLong(secondAddress)) {
            break;
          }
          networks[outputSize - 2] = encode(firstAddress, firstPrefix - 1);
          outputSize--;
        }
      }
      return outputSize;
    }

    private static long encode(int address, int prefix) {
      return ((long) (address ^ Integer.MIN_VALUE) << 8) | prefix;
    }

    private static int decodeAddress(long network) {
      return ((int) (network >> 8)) ^ Integer.MIN_VALUE;
    }

    private static int decodePrefix(long network) {
      return (int) (network & 0xff);
    }
  }

  private static final class IPv6Builder {
    private long[] highAddresses = new long[16];
    private long[] lowAddresses = new long[16];
    private byte[] prefixes = new byte[16];
    private int size;

    void add(long high, long low, int prefix) {
      if (size == highAddresses.length) {
        int capacity = size * 2;
        highAddresses = Arrays.copyOf(highAddresses, capacity);
        lowAddresses = Arrays.copyOf(lowAddresses, capacity);
        prefixes = Arrays.copyOf(prefixes, capacity);
      }
      highAddresses[size] = high;
      lowAddresses[size] = low;
      prefixes[size] = (byte) prefix;
      size++;
    }

    IPv6Networks build() {
      if (size == 0) {
        return new IPv6Networks(new long[0], new long[0], new byte[0]);
      }
      sort(0, size - 1);
      int summarizedSize = summarize();
      return new IPv6Networks(
          Arrays.copyOf(highAddresses, summarizedSize),
          Arrays.copyOf(lowAddresses, summarizedSize),
          Arrays.copyOf(prefixes, summarizedSize));
    }

    private int summarize() {
      int outputSize = 0;
      for (int index = 0; index < size; index++) {
        long high = highAddresses[index];
        long low = lowAddresses[index];
        int prefix = Byte.toUnsignedInt(prefixes[index]);
        if (outputSize > 0
            && containsIPv6(
                highAddresses[outputSize - 1],
                lowAddresses[outputSize - 1],
                Byte.toUnsignedInt(prefixes[outputSize - 1]),
                high,
                low,
                prefix)) {
          continue;
        }

        highAddresses[outputSize] = high;
        lowAddresses[outputSize] = low;
        prefixes[outputSize] = (byte) prefix;
        outputSize++;
        while (outputSize >= 2) {
          int firstIndex = outputSize - 2;
          int secondIndex = outputSize - 1;
          int firstPrefix = Byte.toUnsignedInt(prefixes[firstIndex]);
          int secondPrefix = Byte.toUnsignedInt(prefixes[secondIndex]);
          long firstHigh = highAddresses[firstIndex];
          long firstLow = lowAddresses[firstIndex];
          if (firstPrefix != secondPrefix
              || firstPrefix == 0
              || firstHigh != maskIPv6High(firstHigh, firstPrefix - 1)
              || firstLow != maskIPv6Low(firstLow, firstPrefix - 1)
              || !isAdjacent(
                  firstHigh,
                  firstLow,
                  firstPrefix,
                  highAddresses[secondIndex],
                  lowAddresses[secondIndex])) {
            break;
          }
          prefixes[firstIndex] = (byte) (firstPrefix - 1);
          highAddresses[firstIndex] = maskIPv6High(firstHigh, firstPrefix - 1);
          lowAddresses[firstIndex] = maskIPv6Low(firstLow, firstPrefix - 1);
          outputSize--;
        }
      }
      return outputSize;
    }

    private static boolean isAdjacent(
        long firstHigh, long firstLow, int prefix, long secondHigh, long secondLow) {
      int bit = IPV6_BITS - prefix;
      if (bit >= Long.SIZE) {
        long expectedHigh = firstHigh | (1L << (bit - Long.SIZE));
        return expectedHigh == secondHigh && firstLow == secondLow;
      }
      long expectedLow = firstLow | (1L << bit);
      return firstHigh == secondHigh && expectedLow == secondLow;
    }

    private void sort(int left, int right) {
      while (left < right) {
        int first = left;
        int last = right;
        int pivotIndex = (left + right) >>> 1;
        long pivotHigh = highAddresses[pivotIndex];
        long pivotLow = lowAddresses[pivotIndex];
        int pivotPrefix = Byte.toUnsignedInt(prefixes[pivotIndex]);
        while (first <= last) {
          while (compareAt(first, pivotHigh, pivotLow, pivotPrefix) < 0) {
            first++;
          }
          while (compareAt(last, pivotHigh, pivotLow, pivotPrefix) > 0) {
            last--;
          }
          if (first <= last) {
            swap(first, last);
            first++;
            last--;
          }
        }
        if (last - left < right - first) {
          if (left < last) {
            sort(left, last);
          }
          left = first;
        } else {
          if (first < right) {
            sort(first, right);
          }
          right = last;
        }
      }
    }

    private int compareAt(int index, long high, long low, int prefix) {
      return compareIPv6(
          highAddresses[index],
          lowAddresses[index],
          Byte.toUnsignedInt(prefixes[index]),
          high,
          low,
          prefix);
    }

    private void swap(int first, int second) {
      if (first == second) {
        return;
      }
      long high = highAddresses[first];
      highAddresses[first] = highAddresses[second];
      highAddresses[second] = high;
      long low = lowAddresses[first];
      lowAddresses[first] = lowAddresses[second];
      lowAddresses[second] = low;
      byte prefix = prefixes[first];
      prefixes[first] = prefixes[second];
      prefixes[second] = prefix;
    }
  }

  private static final class ParsedNetwork {
    private int version;
    private int prefix;
    private int ipv4Address;
    private long ipv6HighAddress;
    private long ipv6LowAddress;

    private void reset() {
      version = 0;
      prefix = 0;
      ipv4Address = 0;
      ipv6HighAddress = 0;
      ipv6LowAddress = 0;
    }
  }

  private static final class Parser {
    private Parser() {}

    private static boolean parseStrictIPv4Address(String value, ParsedNetwork output) {
      output.reset();
      int length = value.length();
      int position = 0;
      int address = 0;
      for (int part = 0; part < 4; part++) {
        int start = position;
        int number = 0;
        while (position < length) {
          char character = value.charAt(position);
          if (character < '0' || character > '9') {
            break;
          }
          number = number * 10 + character - '0';
          if (number > 255) {
            return false;
          }
          position++;
        }
        if (position == start) {
          return false;
        }
        address = (address << 8) | number;
        if (part < 3) {
          if (position >= length || value.charAt(position) != '.') {
            return false;
          }
          position++;
        }
      }
      if (position != length) {
        return false;
      }
      output.version = IPV4;
      output.prefix = IPV4_BITS;
      output.ipv4Address = address;
      return true;
    }

    private static boolean parseBaseNetwork(String value, ParsedNetwork output) {
      output.reset();
      if (value == null) {
        return false;
      }
      int start = trimStart(value, 0, value.length());
      int end = trimEnd(value, start, value.length());
      if (start == end) {
        return false;
      }

      int slash = -1;
      for (int index = start; index < end; index++) {
        if (value.charAt(index) == '/') {
          if (slash >= 0) {
            return false;
          }
          slash = index;
        }
      }

      Boolean ipv4 = looksLikeIPv4(value, start, end);
      if (ipv4 == null) {
        return false;
      }
      int maxPrefix = ipv4 ? IPV4_BITS : IPV6_BITS;
      int prefix = slash < 0 ? maxPrefix : parsePrefix(value, slash + 1, end, maxPrefix);
      if (prefix < 0) {
        return false;
      }
      int addressEnd = slash < 0 ? end : slash;
      if (ipv4) {
        long address = parseIPv4(value, start, addressEnd);
        if (address < 0) {
          return parseLegacyIPv4(value, start, addressEnd, prefix, output);
        }
        output.version = IPV4;
        output.prefix = prefix;
        output.ipv4Address = maskIPv4((int) address, prefix);
        return true;
      }
      if (!parseIPv6(value, start, addressEnd, output)) {
        return false;
      }
      output.version = IPV6;
      output.prefix = prefix;
      output.ipv6HighAddress = maskIPv6High(output.ipv6HighAddress, prefix);
      output.ipv6LowAddress = maskIPv6Low(output.ipv6LowAddress, prefix);
      return true;
    }

    private static Boolean looksLikeIPv4(String value, int start, int end) {
      for (int index = start; index < end; index++) {
        char character = value.charAt(index);
        if (character == '.') {
          return true;
        }
        if (character == ':') {
          return false;
        }
      }
      return null;
    }

    private static int parsePrefix(String value, int start, int end, int maximum) {
      int number = 0;
      int digits = 0;
      for (int index = start; index < end; index++) {
        char character = value.charAt(index);
        if (character < '0' || character > '9') {
          break;
        }
        number = number * 10 + character - '0';
        digits++;
        if (number > maximum) {
          return -1;
        }
      }
      return digits == 0 ? -1 : number;
    }

    private static long parseIPv4(String value, int start, int end) {
      int position = start;
      long address = 0;
      for (int part = 0; part < 4; part++) {
        int partEnd = end;
        if (part < 3) {
          partEnd = indexOf(value, '.', position, end);
          if (partEnd < 0) {
            return -1;
          }
        } else if (indexOf(value, '.', position, end) >= 0) {
          return -1;
        }
        int number = parseDecimalPrefix(value, position, partEnd);
        if (number < 0 || number > 255) {
          return -1;
        }
        address = (address << 8) | number;
        position = partEnd + 1;
      }
      return address;
    }

    private static boolean parseLegacyIPv4(
        String value, int start, int end, int requestedPrefix, ParsedNetwork output) {
      int partCount = 1;
      boolean hasWildcard = false;
      for (int index = start; index < end; index++) {
        char character = value.charAt(index);
        if (character == '.') {
          partCount++;
        } else if (character == '*') {
          hasWildcard = true;
        }
      }

      if (hasWildcard) {
        if (partCount != 4) {
          return false;
        }
        int position = start;
        int address = 0;
        int wildcardPrefix = IPV4_BITS;
        boolean wildcardSeen = false;
        for (int part = 0; part < 4; part++) {
          int partEnd = part < 3 ? indexOf(value, '.', position, end) : end;
          if (partEnd < 0) {
            return false;
          }
          if (partEnd - position == 1 && value.charAt(position) == '*') {
            if (!wildcardSeen) {
              wildcardPrefix = part * Byte.SIZE;
              wildcardSeen = true;
            }
            address <<= Byte.SIZE;
          } else {
            if (wildcardSeen) {
              return false;
            }
            long parsed = parseUnsignedDecimal(value, position, partEnd, 255);
            if (parsed < 0) {
              return false;
            }
            address = (address << Byte.SIZE) | (int) parsed;
          }
          position = partEnd + 1;
        }
        output.version = IPV4;
        output.prefix = Math.min(requestedPrefix, wildcardPrefix);
        output.ipv4Address = maskIPv4(address, output.prefix);
        return true;
      }

      if (partCount != 2 && partCount != 3) {
        return false;
      }
      int firstSeparator = indexOf(value, '.', start, end);
      long first = parseUnsignedDecimal(value, start, firstSeparator, 255);
      if (first < 0) {
        return false;
      }

      long address;
      if (partCount == 2) {
        long second = parseUnsignedDecimal(value, firstSeparator + 1, end, 0x00ff_ffffL);
        if (second < 0) {
          return false;
        }
        address = (first << 24) | second;
      } else {
        int secondSeparator = indexOf(value, '.', firstSeparator + 1, end);
        long second = parseUnsignedDecimal(value, firstSeparator + 1, secondSeparator, 255);
        long third = parseUnsignedDecimal(value, secondSeparator + 1, end, 0xffff);
        if (second < 0 || third < 0) {
          return false;
        }
        address = (first << 24) | (second << 16) | third;
      }
      output.version = IPV4;
      output.prefix = requestedPrefix;
      output.ipv4Address = maskIPv4((int) address, requestedPrefix);
      return true;
    }

    private static long parseUnsignedDecimal(String value, int start, int end, long maximum) {
      if (start >= end) {
        return -1;
      }
      long number = 0;
      for (int index = start; index < end; index++) {
        char character = value.charAt(index);
        if (character < '0' || character > '9') {
          return -1;
        }
        number = number * 10 + character - '0';
        if (number > maximum) {
          return -1;
        }
      }
      return number;
    }

    private static int parseDecimalPrefix(String value, int start, int end) {
      int position = trimStart(value, start, end);
      boolean negative = false;
      if (position < end && (value.charAt(position) == '+' || value.charAt(position) == '-')) {
        negative = value.charAt(position) == '-';
        position++;
      }
      int digitStart = position;
      int number = 0;
      while (position < end) {
        char character = value.charAt(position);
        if (character < '0' || character > '9') {
          break;
        }
        number = number * 10 + character - '0';
        if (number > 255) {
          return -1;
        }
        position++;
      }
      if (position == digitStart || (negative && number != 0)) {
        return -1;
      }
      return number;
    }

    private static boolean parseIPv6(String value, int start, int end, ParsedNetwork output) {
      if (start < end && value.charAt(start) == '[') {
        int closingBracket = lastIndexOf(value, ']', start + 1, end);
        if (closingBracket >= 0) {
          start++;
          end = closingBracket;
        }
      }
      if (start == end) {
        return false;
      }
      int zoneSeparator = indexOf(value, '%', start, end);
      if (zoneSeparator >= 0) {
        if (zoneSeparator == end - 1) {
          return false;
        }
        end = zoneSeparator;
      }
      if (end - start == 2 && value.charAt(start) == ':' && value.charAt(start + 1) == ':') {
        return true;
      }

      int compression = indexOfDoubleColon(value, start, end);
      if (compression >= 0 && indexOfDoubleColon(value, compression + 2, end) >= 0) {
        return false;
      }
      int leftEnd = compression < 0 ? end : compression;
      int leftByteIndex = parseIPv6LeftHalf(value, start, leftEnd, output);
      if (leftByteIndex < 0) {
        return false;
      }
      return compression < 0
          || parseIPv6RightHalf(value, compression + 2, end, leftByteIndex, output);
    }

    private static int parseIPv6LeftHalf(String value, int start, int end, ParsedNetwork output) {
      int byteIndex = 0;
      if (start == end) {
        return byteIndex;
      }
      int position = start;
      while (position <= end) {
        int partEnd = indexOf(value, ':', position, end);
        if (partEnd < 0) {
          partEnd = end;
        }
        if (byteIndex >= IPV6) {
          return -1;
        }
        if (hasFourIPv4Parts(value, position, partEnd)) {
          long ipv4 = parseEmbeddedIPv4(value, position, partEnd);
          if (ipv4 < 0 || byteIndex + 4 > IPV6) {
            return -1;
          }
          for (int shift = 24; shift >= 0; shift -= 8) {
            setIPv6Byte(output, byteIndex++, (int) (ipv4 >>> shift) & 0xff);
          }
        } else {
          int hextet = parseHextet(value, position, partEnd);
          if (hextet < 0 || byteIndex + 2 > IPV6) {
            return -1;
          }
          setIPv6Byte(output, byteIndex++, hextet >>> 8);
          setIPv6Byte(output, byteIndex++, hextet & 0xff);
        }
        if (partEnd == end) {
          break;
        }
        position = partEnd + 1;
      }
      return byteIndex;
    }

    private static boolean parseIPv6RightHalf(
        String value, int start, int end, int leftByteIndex, ParsedNetwork output) {
      if (start == end) {
        return true;
      }
      int rightByteIndex = IPV6 - 1;
      int position = end;
      boolean rightmost = true;
      while (position >= start) {
        int partStart = lastIndexOf(value, ':', start, position);
        partStart = partStart < 0 ? start : partStart + 1;
        if (trimStart(value, partStart, position) == trimEnd(value, partStart, position)
            || leftByteIndex > rightByteIndex) {
          return false;
        }
        if (hasFourIPv4Parts(value, partStart, position)) {
          long ipv4 = parseEmbeddedIPv4(value, partStart, position);
          if (ipv4 < 0 || rightByteIndex - 3 < 0) {
            return false;
          }
          for (int shift = 0; shift <= 24; shift += 8) {
            setIPv6Byte(output, rightByteIndex--, (int) (ipv4 >>> shift) & 0xff);
          }
        } else {
          int partEnd = position;
          if (rightmost) {
            partEnd = removePortInfo(value, partStart, partEnd);
            partStart = trimStart(value, partStart, partEnd);
          }
          int hextet = parseHextet(value, partStart, partEnd);
          if (hextet < 0 || rightByteIndex - 1 < 0) {
            return false;
          }
          setIPv6Byte(output, rightByteIndex--, hextet & 0xff);
          setIPv6Byte(output, rightByteIndex--, hextet >>> 8);
        }
        rightmost = false;
        if (partStart == start) {
          break;
        }
        position = partStart - 1;
      }
      return true;
    }

    private static long parseEmbeddedIPv4(String value, int start, int end) {
      int position = start;
      long address = 0;
      for (int part = 0; part < 4; part++) {
        int partEnd = part < 3 ? indexOf(value, '.', position, end) : end;
        if (partEnd < 0 || (part == 3 && indexOf(value, '.', position, end) >= 0)) {
          return -1;
        }
        int number = parseNumber(value, position, partEnd);
        if (number < 0 || number > 255) {
          return -1;
        }
        address = (address << 8) | number;
        position = partEnd + 1;
      }
      return address;
    }

    private static int parseNumber(String value, int start, int end) {
      int position = trimStart(value, start, end);
      int trimmedEnd = trimEnd(value, position, end);
      if (position == trimmedEnd) {
        return 0;
      }
      if (position + 2 < trimmedEnd && value.charAt(position) == '0') {
        int radix = switch (value.charAt(position + 1)) {
          case 'b', 'B' -> 2;
          case 'o', 'O' -> 8;
          case 'x', 'X' -> 16;
          default -> 0;
        };
        if (radix != 0) {
          return parseRadixNumber(value, position + 2, trimmedEnd, radix);
        }
      }

      boolean negative = false;
      if (value.charAt(position) == '+' || value.charAt(position) == '-') {
        negative = value.charAt(position) == '-';
        position++;
      }
      double number = 0;
      int digits = 0;
      while (position < trimmedEnd && isDecimalDigit(value.charAt(position))) {
        number = number * 10 + value.charAt(position++) - '0';
        digits++;
      }
      if (position < trimmedEnd && value.charAt(position) == '.') {
        position++;
        double decimalPlace = 0.1;
        while (position < trimmedEnd && isDecimalDigit(value.charAt(position))) {
          number += (value.charAt(position++) - '0') * decimalPlace;
          decimalPlace *= 0.1;
          digits++;
        }
      }
      if (digits == 0) {
        return -1;
      }
      if (position < trimmedEnd
          && (value.charAt(position) == 'e' || value.charAt(position) == 'E')) {
        position++;
        boolean negativeExponent = false;
        if (position < trimmedEnd
            && (value.charAt(position) == '+' || value.charAt(position) == '-')) {
          negativeExponent = value.charAt(position) == '-';
          position++;
        }
        int exponent = 0;
        int exponentDigits = 0;
        while (position < trimmedEnd && isDecimalDigit(value.charAt(position))) {
          exponent = Math.min(1000, exponent * 10 + value.charAt(position++) - '0');
          exponentDigits++;
        }
        if (exponentDigits == 0) {
          return -1;
        }
        number *= Math.pow(10, negativeExponent ? -exponent : exponent);
      }
      if (position != trimmedEnd) {
        return -1;
      }
      if (negative) {
        number = -number;
      }
      return number >= 0 && number <= 255 && number == Math.rint(number) ? (int) number : -1;
    }

    private static int parseRadixNumber(String value, int start, int end, int radix) {
      int number = 0;
      for (int position = start; position < end; position++) {
        int digit = Character.digit(value.charAt(position), radix);
        if (digit < 0) {
          return -1;
        }
        number = number * radix + digit;
        if (number > 255) {
          return -1;
        }
      }
      return number;
    }

    private static boolean isDecimalDigit(char character) {
      return character >= '0' && character <= '9';
    }

    private static boolean hasFourIPv4Parts(String value, int start, int end) {
      int dots = 0;
      for (int index = start; index < end; index++) {
        if (value.charAt(index) == '.') {
          dots++;
        }
      }
      return dots == 3;
    }

    private static int parseHextet(String value, int start, int end) {
      int trimmedStart = trimStart(value, start, end);
      int trimmedEnd = trimEnd(value, trimmedStart, end);
      int trimmedLength = trimmedEnd - trimmedStart;
      if (trimmedLength < 1 || trimmedLength > 4) {
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
        parsed = (parsed << 4) | digit;
      }
      return parsed;
    }

    private static int removePortInfo(String value, int start, int end) {
      for (int index = start; index < end; index++) {
        char character = value.charAt(index);
        if (character == '#' || character == 'p' || character == '.') {
          return trimEnd(value, start, index);
        }
      }
      return end;
    }

    private static void setIPv6Byte(ParsedNetwork output, int index, int value) {
      if (index < Long.BYTES) {
        output.ipv6HighAddress |= (long) value << ((Long.BYTES - 1 - index) * Byte.SIZE);
      } else {
        output.ipv6LowAddress |= (long) value << ((IPV6 - 1 - index) * Byte.SIZE);
      }
    }

    private static int indexOf(String value, char target, int start, int end) {
      for (int index = start; index < end; index++) {
        if (value.charAt(index) == target) {
          return index;
        }
      }
      return -1;
    }

    private static int lastIndexOf(String value, char target, int start, int end) {
      for (int index = end - 1; index >= start; index--) {
        if (value.charAt(index) == target) {
          return index;
        }
      }
      return -1;
    }

    private static int indexOfDoubleColon(String value, int start, int end) {
      for (int index = start; index + 1 < end; index++) {
        if (value.charAt(index) == ':' && value.charAt(index + 1) == ':') {
          return index;
        }
      }
      return -1;
    }

    private static int trimStart(String value, int start, int end) {
      while (start < end && isWhitespace(value.charAt(start))) {
        start++;
      }
      return start;
    }

    private static int trimEnd(String value, int start, int end) {
      while (end > start && isWhitespace(value.charAt(end - 1))) {
        end--;
      }
      return end;
    }

    private static boolean isWhitespace(char character) {
      return Character.isWhitespace(character) || Character.isSpaceChar(character);
    }
  }
}
