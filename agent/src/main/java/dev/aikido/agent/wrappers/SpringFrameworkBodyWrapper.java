package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.RequestBodyCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/* We wrap the controller functions annotated with an @RequestMapping
 * We check the input for @RequestBody and @RequestParam
 * @RequestPart currently not supported.
 */
public class SpringFrameworkBodyWrapper implements Wrapper {
    public static final Logger log = LogManager.getLogger(SpringFrameworkBodyWrapper.class);

    @Override
    public String getName() {
        return SpringFrameworkBodyWrapperAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/package-summary.html
        return isAnnotatedWith(
                nameContainsIgnoreCase("org.springframework.web.bind.annotation")
                    .and(nameContainsIgnoreCase("DeleteMapping"))
                    .or(nameContainsIgnoreCase("GetMapping"))
                    .or(nameContainsIgnoreCase("PatchMapping"))
                    .or(nameContainsIgnoreCase("PostMapping"))
                    .or(nameContainsIgnoreCase("PutMapping"))
                    .or(nameContainsIgnoreCase("RequestMapping"))
        );
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return hasSuperType(declaresMethod(getMatcher()));
    }

    private static class SpringFrameworkBodyWrapperAdvice {
        /* We intercept the call to the controller, arguments given to it contain user input
         * We check if it's annotated by @RequestBody, ... and add it as a key to our body.
         */
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
                @Advice.Origin Executable method,
                @Advice.AllArguments(readOnly = false, typing = DYNAMIC) Object[] args
        ) {
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String key = checkAnnotationsArray(parameter.getDeclaredAnnotations(), parameter);
                if (key != null) {
                    Object value = args[i];
                    RequestBodyCollector.report(key, value);
                }
            }
        }
    }

    public static String checkAnnotationsArray(Annotation[] annotations, Parameter parameter) {
        for (Annotation annotation: annotations) {
            String annotStr = annotation.toString();
            if (annotStr.contains("org.springframework.web.bind.annotation.RequestBody") ||
                    annotStr.contains("org.springframework.web.bind.annotation.RequestParam")) {
                if (parameter.getName() != null) {
                    return parameter.getName(); // Return the name of the parameter
                }
                // Return the simplified name of the class that contains the data :
                return parameter.getType().getSimpleName();
            }
        }

        return null;
    }
}
