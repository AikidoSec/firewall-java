package helpers;

import dev.aikido.agent_api.helpers.patterns.LooksLikeASecret;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LooksLikeASecretTest {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIALS = "!#$%^&*|;:<>";

    private String secretFromCharset(int length, String charset) {
        Random random = new Random();
        StringBuilder secret = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            secret.append(charset.charAt(random.nextInt(charset.length())));
        }
        return secret.toString();
    }

    @Test
    public void testEmptyString() {
        assertFalse(LooksLikeASecret.looksLikeASecret(""));
    }

    @Test
    public void testShortStrings() {
        List<String> shortStrings = Arrays.asList(
            "c", "NR", "7t3", "4qEK", "KJr6s", "KXiW4a", "Fupm2Vi",
            "jiGmyGfg", "SJPLzVQ8t", "OmNf04j6mU"
        );
        for (String s : shortStrings) {
            assertFalse(LooksLikeASecret.looksLikeASecret(s));
        }
    }

    @Test
    public void testLongStrings() {
        assertTrue(LooksLikeASecret.looksLikeASecret("rsVEExrR2sVDONyeWwND"));
        assertTrue(LooksLikeASecret.looksLikeASecret(":2fbg;:qf$BRBc<2AG8&"));
    }

    @Test
    public void testVeryLongStrings() {
        assertTrue(LooksLikeASecret.looksLikeASecret(
            "efDJHhzvkytpXoMkFUgag6shWJktYZ5QUrUCTfecFELpdvaoAT3tekI4ZhpzbqLt"
        ));
        assertTrue(LooksLikeASecret.looksLikeASecret(
            "XqSwF6ySwMdTomIdmgFWcMVXWf5L0oVvO5sIjaCPI7EjiPvRZhZGWx3A6mLl1HXPOHdUeabsjhngW06JiLhAchFwgtUaAYXLolZn75WsJVKHxEM1mEXhlmZepLCGwRAM"
        ));
    }

    @Test
    public void testContainsWhiteSpace() {
        assertFalse(LooksLikeASecret.looksLikeASecret("rsVEExrR2sVDONyeWwND "));
    }

    @Test
    public void testLessThan2Charsets() {
        assertFalse(LooksLikeASecret.looksLikeASecret(secretFromCharset(10, LOWERCASE)));
        assertFalse(LooksLikeASecret.looksLikeASecret(secretFromCharset(10, UPPERCASE)));
        assertFalse(LooksLikeASecret.looksLikeASecret(secretFromCharset(10, NUMBERS)));
        assertFalse(LooksLikeASecret.looksLikeASecret(secretFromCharset(10, SPECIALS)));
    }

    @Test
    public void testCommonUrlTerms() {
        List<String> urlTerms = Arrays.asList(
            "development", "programming", "applications", "implementation",
            "environment", "technologies", "documentation", "demonstration",
            "configuration", "administrator", "visualization", "international",
            "collaboration", "opportunities", "functionality", "customization",
            "specifications", "optimization", "contributions", "accessibility",
            "subscription", "subscriptions", "infrastructure", "architecture",
            "authentication", "sustainability", "notifications", "announcements",
            "recommendations", "communication", "compatibility", "enhancement",
            "integration", "performance", "improvements", "introduction",
            "capabilities", "communities", "credentials", "integration",
            "permissions", "validation", "serialization", "deserialization",
            "rate-limiting", "throttling", "load-balancer", "microservices",
            "endpoints", "data-transfer", "encryption", "authorization",
            "bearer-token", "multipart", "urlencoded", "api-docs", "postman",
            "json-schema", "serialization", "deserialization", "rate-limiting",
            "throttling", "load-balancer", "api-gateway", "microservices",
            "endpoints", "data-transfer", "encryption", "signature",
            "poppins-bold-webfont.woff2", "karla-bold-webfont.woff2", "startEmailBasedLogin",
            "jenkinsFile", "ConnectionStrings.config", "coach", "login",
            "payment_methods", "activity_logs", "feedback_responses",
            "balance_transactions", "customer_sessions", "payment_intents",
            "billing_portal", "subscription_items", "namedLayouts",
            "PlatformAction", "quickActions", "queryLocator", "relevantItems", "parameterizedSearch"
        );
        for (String term : urlTerms) {
            assertFalse(LooksLikeASecret.looksLikeASecret(term));
        }
    }

    @Test
    public void testKnownWordSeparators() {
        assertFalse(LooksLikeASecret.looksLikeASecret("this-is-a-secret-1"));
    }

    @Test
    public void testNumberIsNotASecret() {
        assertFalse(LooksLikeASecret.looksLikeASecret("1234567890"));
        assertFalse(LooksLikeASecret.looksLikeASecret("1234567890" + "1234567890"));
    }

    @Test
    public void testKnownSecrets() {
        List<String> secrets = Arrays.asList(
            "yqHYTS<agpi^aa1",
            "hIofuWBifkJI5iVsSNKKKDpBfmMqJJwuXMxau6AS8WZaHVLDAMeJXo3BwsFyrIIm",
            "AG7DrGi3pDDIUU1PrEsj",
            "CnJ4DunhYfv2db6T1FRfciRBHtlNKOYrjoz",
            "Gic*EfMq:^MQ|ZcmX:yW1",
            "AG7DrGi3pDDIUU1PrEsj"
        );

        for (String secret : secrets) {
            assertTrue(LooksLikeASecret.looksLikeASecret(secret));
        }
    }
}
