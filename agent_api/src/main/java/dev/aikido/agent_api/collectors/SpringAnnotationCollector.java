package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.SpringContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

public final class SpringAnnotationCollector {
    private SpringAnnotationCollector() {}
    public static final Logger logger = LogManager.getLogger(SpringAnnotationCollector.class);
    private static String REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
    private static String REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
    private static String REQUEST_PART = "org.springframework.web.bind.annotation.RequestPart";
    private static String PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";

    /** report(...)
     * Handles Springs parameters, can include @PathVariable, @RequestBody, ...
     */
    public static void report(Parameter[] parameters, Object[] values) throws Exception {
        if (parameters.length != values.length) {
            // This exception gets caught by Byte Buddy.
            throw new Exception("Length of parameters and values should match!");
        }
        for (int i = 0; i < parameters.length; i++) {
            report(parameters[i], values[i]);
        }
    }

    public static void report(Parameter parameter, Object value) {
        SpringContextObject context = (SpringContextObject) Context.get();
        if (context == null) {
            logger.error(
                "Received Spring Annotations, but no context set." +
                "This is likely because of an incompatibility with your current Spring setup.");
            return;
        }

        for (Annotation annotation: parameter.getDeclaredAnnotations()) {
            String annotStr = annotation.annotationType().getName();
            if (annotStr.contains(REQUEST_BODY)) {
                // RequestBody includes all data so we report everything as one block:
                // Also important for API Discovery that we get the exact overview
                context.setBody(value);
                break;
            } else if (annotStr.equals(REQUEST_PARAM) || annotStr.equals(REQUEST_PART)) {
                // RequestPart and RequestParam both contain partial data.
                String identifier = parameter.getName();
                context.setBodyElement(identifier, value);
                break;
            } else if(annotStr.equals(PATH_VARIABLE)) {
                String identifier = parameter.getName();
                if (value instanceof Map<?, ?> paramsMap) {
                    for (Map.Entry<?, ?> entry: paramsMap.entrySet()) {
                        if (entry.getKey() instanceof String key && entry.getValue() instanceof String valueStr) {
                            context.setParameter(key, valueStr);
                        }
                    }
                } else if (value instanceof String valueStr) {
                    context.setParameter(identifier, valueStr);
                } else if (value instanceof Optional<?> valueOpt) {
                    if(valueOpt.isPresent()) {
                        if (valueOpt.get() instanceof String valueStr) {
                            context.setParameter(identifier, valueStr);
                        }
                    }
                }
                break;
            }
        }
        Context.set(context); // Store context.
    }
}
