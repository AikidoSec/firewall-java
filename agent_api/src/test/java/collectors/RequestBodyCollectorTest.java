package collectors;

import dev.aikido.agent_api.collectors.SpringAnnotationCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.SpringMVCContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestBodyCollectorTest {
    private SpringMVCContextObject contextObject;
    @BeforeEach
    public void setup() {
        contextObject = new SpringMVCContextObject(
                "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>()
        );
    }

    @Test
    public void testWithSpringContext() {
        Context.set(contextObject);

        SpringAnnotationCollector.report("Hello World");
        assertEquals("Hello World", Context.get().getBody());

        SpringAnnotationCollector.report("data1", "data2");
        // Make sure that full body gets prioritized:
        assertEquals("Hello World", Context.get().getBody());
    }

    @Test
    public void testWithSpringContext2() {
        Context.set(contextObject);

        SpringAnnotationCollector.report("data1", Map.of("1", "2"));
        SpringAnnotationCollector.report("data2", Map.of("3", "4"));

        // Make sure that full body gets prioritized:
        Map<?, ?> currentBody = (Map<?, ?>) Context.get().getBody();
        assertEquals(2, currentBody.size());

        Map<String, String> data1 = (Map<String, String>) currentBody.get("data1");
        assertEquals("2", data1.get("1"));

        Map<String, String> data2 = (Map<String, String>) currentBody.get("data2");
        assertEquals("4", data2.get("3"));
    }
}
