package dev.aikido.agent_api.collectors;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.MicronautContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Handles Micronaut controller parameters: @Body, @QueryValue, @PathVariable, @Part.
 * Mirrors SpringAnnotationCollector; runs on the controller thread after the context is set.
 */
public final class MicronautAnnotationCollector {
    private MicronautAnnotationCollector() {}
    private static final Logger logger = LogManager.getLogger(MicronautAnnotationCollector.class);

    private static final String BODY = "io.micronaut.http.annotation.Body";
    private static final String QUERY_VALUE = "io.micronaut.http.annotation.QueryValue";
    private static final String PATH_VARIABLE = "io.micronaut.http.annotation.PathVariable";
    private static final String PART = "io.micronaut.http.annotation.Part";
    // @RequestBean aggregates @QueryValue/@PathVariable/... fields into one bean :
    private static final String REQUEST_BEAN = "io.micronaut.http.annotation.RequestBean";

    public static void report(Parameter[] parameters, Object[] values) {
        if (parameters == null || values == null) {
            return;
        }
        if (!(Context.get() instanceof MicronautContextObject context)) {
            return;
        }
        int len = Math.min(parameters.length, values.length);
        for (int i = 0; i < len; i++) {
            report(context, parameters[i], values[i]);
        }
        Context.set(context);
    }

    private static void report(MicronautContextObject context, Parameter parameter, Object value) {
        if (value == null) {
            return;
        }
        for (Annotation annotation : parameter.getDeclaredAnnotations()) {
            String name = annotation.annotationType().getName();
            if (name.equals(BODY)) {
                context.setBody(value); // full body, overrides everything
                return;
            } else if (name.equals(QUERY_VALUE) || name.equals(PART) || name.equals(REQUEST_BEAN)) {
                // The whole value is stored; StringExtractor recurses into bean fields.
                context.setBodyElement(parameter.getName(), value);
                return;
            } else if (name.equals(PATH_VARIABLE)) {
                context.setParameter(parameter.getName(), value.toString());
                return;
            }
        }
    }
}
