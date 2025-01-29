package dev.aikido.agent_api.helpers.patterns;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

public final class LooksLikeJWT {
    private LooksLikeJWT() {}

    public static Result tryDecodeAsJwt(String jwt) {
        // Check if the JWT contains the required parts
        if (jwt == null || !jwt.contains(".")) {
            return new Result(false, null);
        }

        String[] parts = jwt.split("\\.");

        // Ensure there are exactly 3 parts
        if (parts.length != 3) {
            return new Result(false, null);
        }

        try {
            // Decode the middle part (payload) of the JWT
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            Gson gson = new Gson();
            Map<String, Object> jwtPayload = gson.fromJson(payload, new TypeToken<Map<String, Object>>() {}.getType());

            return new Result(true, jwtPayload);
        } catch (Exception ignored) {
            return new Result(false, null);
        }
    }

    // Helper class to hold the result
    public record Result(boolean success, Map<String, Object> payload) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Result)) return false;
            Result result = (Result) o;
            return success == result.success && Objects.equals(payload, result.payload);
        }

        @Override
        public String toString() {
            return "Result{" + "success=" + success + ", payload=" + payload + '}';
        }
    }
}
