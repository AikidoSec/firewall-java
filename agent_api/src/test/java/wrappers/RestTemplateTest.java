package wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import utils.EmptySampleContextObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

public class RestTemplateTest {

    @AfterEach
    void cleanup() {
        Context.set(null);
        ThreadCache.set(null);
    }

    @BeforeEach
    void clearThreadCache() {
        cleanup();
    }

    private void setContextAndLifecycle(String url) {
        Context.set(new EmptySampleContextObject(url));
        ThreadCache.set(getEmptyThreadCacheObject());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFLocalhostValid() throws Exception {
        setContextAndLifecycle("http://localhost:5000");

        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000/api/test");
        });
        assertEquals(
                "Aikido Zen has blocked a server-side request forgery",
                exception1.getMessage());

        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost:5000");
        });
        assertEquals(
                "Aikido Zen has blocked a server-side request forgery",
                exception2.getMessage());

        RuntimeException exception3 = assertThrows(RuntimeException.class, () -> {
            fetchResponse("https://localhost:5000/api/test");
        });
        assertEquals(
                "Aikido Zen has blocked a server-side request forgery",
                exception3.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFWithoutPort() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
        assertEquals("Aikido Zen has blocked a server-side request forgery", exception.getMessage());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "invalid-token-2")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    @Test
    public void testSSRFWithoutPortAndWithoutContext() throws Exception {
        setContextAndLifecycle("http://localhost:80");
        Context.set(null);
        assertThrows(ResourceAccessException.class, () -> {
            fetchResponse("http://localhost/api/test");
        });
    }

    private void fetchResponse(String urlString) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(urlString, String.class);

    }
}
