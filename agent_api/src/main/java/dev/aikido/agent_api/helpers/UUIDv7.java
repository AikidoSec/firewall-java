package dev.aikido.agent_api.helpers;

import java.security.SecureRandom;
import java.util.UUID;

// The JDK versions we support can't generate a UUID v7
public final class UUIDv7 {
    private UUIDv7() {}

    // RFC 9562 layout: 48-bit Unix timestamp in ms, version 7, 74 random bits and the variant
    public static UUID generate() {
        SecureRandom random = new SecureRandom();
        long mostSigBits = (System.currentTimeMillis() << 16) | 0x7000L | (random.nextLong() & 0x0FFFL);
        long leastSigBits = (random.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(mostSigBits, leastSigBits);
    }
}
