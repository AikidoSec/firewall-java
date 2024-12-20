package collectors;

import dev.aikido.agent_api.collectors.RequestBodyCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.SpringContextObject;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class RequestBodyCollectorTest {
    private SpringContextObject springContextObject1;
    @BeforeEach
    public void setup() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/test"));
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        springContextObject1 = new SpringContextObject(request);
    }

    @Test
    public void testWithSpringContext() {
        Context.set(springContextObject1);

        RequestBodyCollector.report("Hello World");
        assertEquals("Hello World", Context.get().getBody());

        RequestBodyCollector.report("data1", "data2");
        // Make sure that full body gets prioritized:
        assertEquals("Hello World", Context.get().getBody());
    }

    @Test
    public void testWithSpringContext2() {
        Context.set(springContextObject1);

        RequestBodyCollector.report("data1", Map.of("1", "2"));
        RequestBodyCollector.report("data2", Map.of("3", "4"));

        // Make sure that full body gets prioritized:
        Map<?, ?> currentBody = (Map<?, ?>) Context.get().getBody();
        assertEquals(2, currentBody.size());

        Map<String, String> data1 = (Map<String, String>) currentBody.get("data1");
        assertEquals("2", data1.get("1"));

        Map<String, String> data2 = (Map<String, String>) currentBody.get("data2");
        assertEquals("4", data2.get("3"));
    }
}
