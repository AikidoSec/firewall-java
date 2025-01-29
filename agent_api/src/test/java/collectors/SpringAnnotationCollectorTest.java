package collectors;

import dev.aikido.agent_api.collectors.SpringAnnotationCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.SpringContextObject;
import dev.aikido.agent_api.context.SpringMVCContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpringAnnotationCollectorTest {

    private SpringContextObject mockContext;
    private static final Annotation requestBodyAnnotation = new RequestBody() {
        @Override
        public boolean required() {
            return false;
        }

        public Class<? extends Annotation> annotationType() {
            return RequestBody.class;
        }
    };
    private static final Annotation requestParamAnnotation = new RequestParam() {
        @Override
        public String value() {
            return "";
        }

        @Override
        public String name() {
            return "";
        }

        @Override
        public boolean required() {
            return false;
        }

        @Override
        public String defaultValue() {
            return "";
        }

        public Class<? extends Annotation> annotationType() {
            return RequestParam.class;
        }
    };
    private static final Annotation pathVariableAnnotation = new PathVariable() {
        @Override
        public String value() {
            return "";
        }

        @Override
        public String name() {
            return "";
        }

        @Override
        public boolean required() {
            return false;
        }

        public Class<? extends Annotation> annotationType() {
            return PathVariable.class;
        }
    };


    @BeforeEach
    public void setUp() {
        mockContext = new SpringMVCContextObject(
            "GET", new StringBuffer("http://localhost/test"), "192.168.1.1", Map.of(), new HashMap<>(), new HashMap<>()
        );
        Context.set(mockContext);
    }

    @Test
    public void testReportWithRequestBody() throws Exception {
        Parameter parameter = createMockParameter(requestBodyAnnotation, "Requestbody");
        Object value = "test body";

        SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value});

        assertEquals(value, Context.get().getBody());
    }

    @Test
    public void testReportWithRequestParam() throws Exception {
        Parameter parameter = createMockParameter(requestParamAnnotation, "param");
        Object value = "test param";

        SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value});

        assertEquals(Map.of("param", "test param"), mockContext.getBody());
    }

    @Test
    public void testReportWithRequestParamMultiple() throws Exception {
        Parameter parameter1 = createMockParameter(requestParamAnnotation, "param1");
        Parameter parameter2 = createMockParameter(requestParamAnnotation, "param2");

        SpringAnnotationCollector.report(new Parameter[]{parameter1, parameter2}, new Object[]{"value 1", "value 2"});

        assertEquals(Map.of("param1", "value 1", "param2", "value 2"), mockContext.getBody());
    }

    @Test
    public void testReportWithPathVariable() throws Exception {
        Parameter parameter = createMockParameter(pathVariableAnnotation, "myPathVar");
        String value = "testPath";

        SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value});
        SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value});
        SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value});


        assertEquals(Map.of("myPathVar", "testPath"), mockContext.getParams());
    }

    @Test
    public void testReportWithMapPathVariable() throws Exception {
        Parameter parameter = createMockParameter(pathVariableAnnotation, "pathVar");
        Map<String, String> value = Map.of("key1", "value1", "key2", "value2");

        SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value});

        Parameter parameter2 = createMockParameter(pathVariableAnnotation, "key3");
        SpringAnnotationCollector.report(new Parameter[]{parameter2}, new Object[]{"value3"});

        assertEquals(
            Map.of("key1", "value1", "key2", "value2", "key3", "value3"),
            mockContext.getParams());
    }

    @Test
    public void testReportWithOptionalPathVariable() throws Exception {
        Parameter parameter = createMockParameter(pathVariableAnnotation, "pathVar");
        Optional<String> value = Optional.of("optionalValue");

        SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value});

        assertEquals(Map.of("pathVar", "optionalValue"), mockContext.getParams());
    }

    @Test
    public void testReportWithDifferentLengthParametersAndValues() {
        Parameter parameter = createMockParameter(requestBodyAnnotation, "paramName");
        Object value = "test body";

        Exception exception = assertThrows(Exception.class, () -> {
            SpringAnnotationCollector.report(new Parameter[]{parameter}, new Object[]{value, "extraValue"});
        });

        assertEquals("Length of parameters and values should match!", exception.getMessage());
    }

    private Parameter createMockParameter(Annotation annotation, String name) {
        return Mockito.mock(Parameter.class, invocation -> {
            if (invocation.getMethod().getName().equals("getDeclaredAnnotations")) {
                return new Annotation[]{annotation};
            } else if (invocation.getMethod().getName().equals("getName")) {
                return name;
            }
            return null;
        });
    }
}
