package helpers;

import dev.aikido.agent_api.helpers.patterns.LooksLikeJWT;
import dev.aikido.agent_api.vulnerabilities.DangerousBodyException;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LooksLikeJWTTest {

    @Test
    void testReturnsFalseForEmptyString() {
        assertEquals(new LooksLikeJWT.Result(false, null), LooksLikeJWT.tryDecodeAsJwt(""));
    }

    @Test
    void testReturnsFalseForInvalidJwt() {
        assertEquals(new LooksLikeJWT.Result(false, null), LooksLikeJWT.tryDecodeAsJwt("invalid"));
        assertEquals(new LooksLikeJWT.Result(false, null), LooksLikeJWT.tryDecodeAsJwt("invalid.invalid"));
        assertEquals(new LooksLikeJWT.Result(false, null), LooksLikeJWT.tryDecodeAsJwt("invalid.invalid.invalid"));
        assertEquals(new LooksLikeJWT.Result(false, null), LooksLikeJWT.tryDecodeAsJwt("invalid.invalid.invalid.invalid"));
    }

    @Test
    void testReturnsPayloadForInvalidJwt() {
        assertEquals(new LooksLikeJWT.Result(false, null), LooksLikeJWT.tryDecodeAsJwt("/;ping%20localhost;.e30=."));
        assertEquals(new LooksLikeJWT.Result(false,  null), LooksLikeJWT.tryDecodeAsJwt("/;ping%20localhost;.W10=."));
    }

    @Test
    void testReturnsDecodedJwtForValidJwt() {
        String validJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidXNlcm5hbWUiOnsiJG5lIjpudWxsfSwiaWF0IjoxNTE2MjM5MDIyfQ._jhGJw9WzB6gHKPSozTFHDo9NOHs3CNOlvJ8rWy6VrQ";
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("sub", "1234567890");
        Map<String, Object> usernameMap = new HashMap<>();
        usernameMap.put("$ne", null); // This is allowed in a HashMap
        expectedPayload.put("username", usernameMap);
        expectedPayload.put("iat", 1.516239022E9);
        assertEquals(new LooksLikeJWT.Result(true, expectedPayload), LooksLikeJWT.tryDecodeAsJwt(validJwt));
    }

    @Test
    void testReturnsDecodedJwtForValidJwtWithBearerPrefix() {
        String validJwtWithBearer = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidXNlcm5hbWUiOnsiJG5lIjpudWxsfSwiaWF0IjoxNTE2MjM5MDIyfQ._jhGJw9WzB6gHKPSozTFHDo9NOHs3CNOlvJ8rWy6VrQ";
        Map<String, Object> expectedPayload = new HashMap<>();
        expectedPayload.put("sub", "1234567890");
        Map<String, Object> usernameMap = new HashMap<>();
        usernameMap.put("$ne", null); // This is allowed in a HashMap
        expectedPayload.put("username", usernameMap);
        expectedPayload.put("iat", 1.516239022E9);
        assertEquals(new LooksLikeJWT.Result(true, expectedPayload), LooksLikeJWT.tryDecodeAsJwt(validJwtWithBearer));
    }

    private static String buildJwt(String payloadJson) {
        String payloadB64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes());
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + payloadB64 + ".sig";
    }

    @Test
    void testThrowsDangerousBodyExceptionForOversizedPayload() {
        StringBuilder sb = new StringBuilder("{\"k\":\"");
        for (int i = 0; i < LooksLikeJWT.MAX_JWT_PAYLOAD_BYTES + 10; i++) {
            sb.append('a');
        }
        sb.append("\"}");
        String jwt = buildJwt(sb.toString());
        assertThrows(DangerousBodyException.class, () -> LooksLikeJWT.tryDecodeAsJwt(jwt));
    }

    @Test
    void testThrowsDangerousBodyExceptionForDeeplyNestedPayload() {
        int depth = 7000;
        StringBuilder open = new StringBuilder();
        StringBuilder close = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            open.append("{\"a\":");
            close.append("}");
        }
        String payload = open.toString() + "1" + close.toString();
        String jwt = buildJwt(payload);
        assertThrows(DangerousBodyException.class, () -> LooksLikeJWT.tryDecodeAsJwt(jwt));
    }
}
