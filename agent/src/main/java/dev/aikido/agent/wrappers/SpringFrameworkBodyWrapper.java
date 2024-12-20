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
                for (Annotation annotation: parameter.getDeclaredAnnotations()) {
                    String annotStr = annotation.toString();
                    if (annotStr.contains("org.springframework.web.bind.annotation.RequestBody")) {
                        // RequestBody includes all data so we report everything as one block:
                        // Also important for API Discovery that we get the exact overview
                        RequestBodyCollector.report(args[i]);
                        return; // You can safely return here without missing more data
                    }
                    if (annotStr.contains("org.springframework.web.bind.annotation.RequestParam")) {
                        String identifier = parameter.getName();
                        RequestBodyCollector.report(identifier, args[i]);
                        break; // You can safely exit for-loop, but we still want to scan other arguments.
                    }
                }
            }
        }
    }
}
