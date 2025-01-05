package collectors;

import dev.aikido.agent_api.background.Endpoint;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.SpringContextObject;
import dev.aikido.agent_api.storage.routes.Routes;
import dev.aikido.agent_api.thread_cache.ThreadCache;
import dev.aikido.agent_api.thread_cache.ThreadCacheObject;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static utils.EmtpyThreadCacheObject.getEmptyThreadCacheObject;

class WebRequestCollectorTest {

    private ContextObject contextObject;
    private ThreadCacheObject threadCacheObject;

    @BeforeEach
    void setUp() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        contextObject = new SpringContextObject(request);
        threadCacheObject = mock(ThreadCacheObject.class);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_contextSet() {
        // Mock ThreadCache
        threadCacheObject = getEmptyThreadCacheObject();
        ThreadCache.set(threadCacheObject);

        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
        assertEquals(Context.get(), contextObject);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_ipNotAllowed() {
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(new Endpoint(
                "GET", "/test", 100, 100,
                List.of("192.168.0.1"), false, false, false
        )), Set.of(), Set.of(), new Routes(), Optional.empty());
        ThreadCache.set(threadCacheObject);


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNotNull(response);
        assertEquals("Your IP address is not allowed to access this resource. (Your IP: 192.168.1.1)", response.msg());
        assertEquals(403, response.status());
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_endpointAndIpAllowed() {
        // Mock ThreadCache
        threadCacheObject = new ThreadCacheObject(List.of(new Endpoint(
                "GET", "/test", 100, 100,
                List.of("192.168.1.1"), false, false, false
        )), Set.of(), Set.of(), new Routes(), Optional.empty());
        ThreadCache.set(threadCacheObject);


        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        assertNull(response);
    }

    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "test-token")
    @Test
    void testReport_noThreadCacheObject() {
        // Mock ThreadCache to return null
        ThreadCache.reset();

        // Act
        WebRequestCollector.Res response = WebRequestCollector.report(contextObject);

        // Assert
        assertNull(response);
    }
}
