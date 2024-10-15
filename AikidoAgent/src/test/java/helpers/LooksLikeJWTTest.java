package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.aikido.AikidoAgent.helpers.patterns.LooksLikeJWT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        expectedPayload.put("iat", 1516239022);
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
        expectedPayload.put("iat", 1516239022);
        assertEquals(new LooksLikeJWT.Result(true, expectedPayload), LooksLikeJWT.tryDecodeAsJwt(validJwtWithBearer));
    }
}
