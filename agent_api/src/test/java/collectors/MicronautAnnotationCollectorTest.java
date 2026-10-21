package collectors;

import dev.aikido.agent_api.collectors.MicronautAnnotationCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.MicronautContextObject;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.RequestBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MicronautAnnotationCollectorTest {

    private MicronautContextObject context;

    @BeforeEach
    public void setUp() {
        context = new MicronautContextObject(
                "GET", "http://localhost/test", "192.168.1.1",
                new HashMap<>(), new HashMap<>(), new HashMap<>());
        Context.set(context);
    }

    static class Fixture {
        void body(@Body Object body) {}
        void queryValue(@QueryValue String q) {}
        void part(@Part Object p) {}
        void pathVariable(@PathVariable String p) {}
        void pathVariableNonString(@PathVariable Integer p) {}
        void requestBean(@RequestBean Object bean) {}
        void header(@Header String h) {}
    }

    private Parameter param(String method, Class<?> type) throws NoSuchMethodException {
        return Fixture.class.getDeclaredMethod(method, type).getParameters()[0];
    }

    @Test
    public void reportsBodyAsFullBody() throws Exception {
        Object value = Map.of("name", "value");
        MicronautAnnotationCollector.report(new Parameter[]{param("body", Object.class)}, new Object[]{value});
        assertEquals(value, context.getBody());
    }

    @Test
    public void reportsQueryValueAsBodyElement() throws Exception {
        MicronautAnnotationCollector.report(new Parameter[]{param("queryValue", String.class)}, new Object[]{"q-value"});
        assertEquals("q-value", ((Map<?, ?>) context.getBody()).values().iterator().next());
    }

    @Test
    public void reportsPartAsBodyElement() throws Exception {
        MicronautAnnotationCollector.report(new Parameter[]{param("part", Object.class)}, new Object[]{"file-content"});
        assertEquals("file-content", ((Map<?, ?>) context.getBody()).values().iterator().next());
    }

    @Test
    public void reportsRequestBeanAsBodyElement() throws Exception {
        MicronautAnnotationCollector.report(new Parameter[]{param("requestBean", Object.class)}, new Object[]{"bean"});
        assertEquals("bean", ((Map<?, ?>) context.getBody()).values().iterator().next());
    }

    @Test
    public void reportsPathVariableAsParam() throws Exception {
        MicronautAnnotationCollector.report(new Parameter[]{param("pathVariable", String.class)}, new Object[]{"p-value"});
        assertEquals("p-value", ((Map<?, ?>) context.getParams()).values().iterator().next());
    }

    @Test
    public void reportsNonStringPathVariableViaToString() throws Exception {
        MicronautAnnotationCollector.report(new Parameter[]{param("pathVariableNonString", Integer.class)}, new Object[]{42});
        assertEquals("42", ((Map<?, ?>) context.getParams()).values().iterator().next());
    }

    @Test
    public void ignoresUnrecognizedAnnotation() throws Exception {
        MicronautAnnotationCollector.report(new Parameter[]{param("header", String.class)}, new Object[]{"h-value"});
        assertTrue(((Map<?, ?>) context.getBody()).isEmpty());
        assertTrue(((Map<?, ?>) context.getParams()).isEmpty());
    }

    @Test
    public void ignoresNullValues() throws Exception {
        MicronautAnnotationCollector.report(new Parameter[]{param("body", Object.class)}, new Object[]{null});
        assertEquals(new HashMap<>(), context.getBody());
    }

    @Test
    public void ignoresNullArguments() {
        MicronautAnnotationCollector.report(null, null);
        assertEquals(new HashMap<>(), context.getBody());
    }

    @Test
    public void ignoresWhenContextIsNotMicronaut() throws Exception {
        Context.reset();
        MicronautAnnotationCollector.report(new Parameter[]{param("body", Object.class)}, new Object[]{"x"});
    }

    @Test
    public void usesMinLengthWhenParamsAndValuesMismatch() throws Exception {
        Parameter[] params = {param("queryValue", String.class)};
        MicronautAnnotationCollector.report(params, new Object[]{"only", "extra"});
        assertEquals("only", ((Map<?, ?>) context.getBody()).values().iterator().next());
    }
}
