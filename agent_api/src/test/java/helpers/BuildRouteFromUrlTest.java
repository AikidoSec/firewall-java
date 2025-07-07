package helpers;

import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static dev.aikido.agent_api.helpers.url.BuildRouteFromUrl.buildRouteFromUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BuildRouteFromUrlTest {

    @Test
    void testInvalidUrls() {
        assertNull(buildRouteFromUrl(""));
        assertNull(buildRouteFromUrl("http"));
    }

    @Test
    void testRootUrls() {
        assertEquals("/", buildRouteFromUrl("/"));
        assertEquals("/", buildRouteFromUrl("http://localhost/"));
    }

    @Test
    void testReplaceNumbers() {
        assertEquals("/posts/:number", buildRouteFromUrl("/posts/3"));
        assertEquals("/posts/:number", buildRouteFromUrl("http://localhost/posts/3"));
        assertEquals("/posts/:number", buildRouteFromUrl("http://localhost/posts/3/"));
        assertEquals("/posts/:number/comments/:number",
                buildRouteFromUrl("http://localhost/posts/3/comments/10"));
        assertEquals("/blog/:number/:number/great-article",
                buildRouteFromUrl("/blog/2023/05/great-article"));
    }

    @Test
    void testReplaceDates() {
        assertEquals("/posts/:date", buildRouteFromUrl("/posts/2023-05-01"));
        assertEquals("/posts/:date", buildRouteFromUrl("/posts/2023-05-01/"));
        assertEquals("/posts/:date/comments/:date",
                buildRouteFromUrl("/posts/2023-05-01/comments/2023-05-01"));
        assertEquals("/posts/:date", buildRouteFromUrl("/posts/01-05-2023"));
    }

    @Test
    void testIgnoreCommaNumbers() {
        assertEquals("/posts/3,000", buildRouteFromUrl("/posts/3,000"));
    }

    @Test
    void testIgnoreApiVersionNumbers() {
        assertEquals("/v1/posts/:number", buildRouteFromUrl("/v1/posts/3"));
    }

    @Test
    void testReplaceUuids() {
        String[] uuids = {
                "d9428888-122b-11e1-b85c-61cd3cbb3210",
                "000003e8-2363-21ef-b200-325096b39f47",
                "a981a0c2-68b1-35dc-bcfc-296e52ab01ec",
                "109156be-c4fb-41ea-b1b4-efe1671c5836",
                "90123e1c-7512-523e-bb28-76fab9f2f73d",
                "1ef21d2f-1207-6660-8c4f-419efbd44d48",
                "017f22e2-79b0-7cc3-98c4-dc0c0c07398f",
                "0d8f23a0-697f-83ae-802e-48f3756dd581"
        };
        for (String uuid : uuids) {
            assertEquals("/posts/:uuid", buildRouteFromUrl("/posts/" + uuid));
        }
    }

    @Test
    void testIgnoreInvalidUuids() {
        assertEquals("/posts/00000000-0000-1000-6000-000000000000",
                buildRouteFromUrl("/posts/00000000-0000-1000-6000-000000000000"));
    }

    @Test
    void testIgnoreStrings() {
        assertEquals("/posts/abc", buildRouteFromUrl("/posts/abc"));
}

    @Test
    void testReplaceEmailAddresses() {
        assertEquals("/login/:email", buildRouteFromUrl("/login/john.doe@acme.com"));
        assertEquals("/login/:email", buildRouteFromUrl("/login/john.doe+alias@acme.com"));
    }

    @Test
    void testReplaceIpAddresses() {
        assertEquals("/block/:ip", buildRouteFromUrl("/block/1.2.3.4"));
        assertEquals("/block/:ip",
                buildRouteFromUrl("/block/2001:2:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertEquals("/block/:ip", buildRouteFromUrl("/block/100::"));
        assertEquals("/block/:ip", buildRouteFromUrl("/block/fec0::"));
        assertEquals("/block/:ip", buildRouteFromUrl("/block/227.202.96.196"));
    }

    private String generateHash(String algorithm) {
        String data = "test";
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    @Test
    public void testReplaceHashes() {
        assertEquals("/files/:hash", buildRouteFromUrl("/files/" + generateHash("MD5")));
        assertEquals("/files/:hash", buildRouteFromUrl("/files/" + generateHash("SHA-1")));
        assertEquals("/files/:hash", buildRouteFromUrl("/files/" + generateHash("SHA-256")));
        assertEquals("/files/:hash", buildRouteFromUrl("/files/" + generateHash("SHA-512")));
    }

    @Test
    public void testReplaceSecrets() {
        assertEquals("/confirm/:secret", buildRouteFromUrl("/confirm/CnJ4DunhYfv2db6T1FRfciRBHtlNKOYrjoz"));
    }

    @Test
    public void testReplacesBsonObjectIds() {
        assertEquals("/posts/:objectId", buildRouteFromUrl("/posts/66ec29159d00113616fc7184"));
    }

    @Test
    public void testReplacesUlidStrings() {
        assertEquals("/posts/:ulid", buildRouteFromUrl("/posts/01ARZ3NDEKTSV4RRFFQ69G5FAV"));
        assertEquals("/posts/:ulid", buildRouteFromUrl("/posts/01arz3ndektsv4rrffq69g5fav"));
    }
}
