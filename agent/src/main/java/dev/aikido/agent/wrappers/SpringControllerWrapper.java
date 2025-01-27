package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.SpringAnnotationCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;


import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/* We wrap the controller functions annotated with an @RequestMapping
 * We check the input for @RequestBody and @RequestParam
 * @RequestPart currently not supported.
 */
public class SpringControllerWrapper implements Wrapper {
    @Override
    public String getName() {
        return SpringAnnotationWrapperAdvice.class.getName();
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

    private static class SpringAnnotationWrapperAdvice {
        /* We intercept the call to the controller, arguments given to it contain user input
         * We check if it's annotated by @RequestBody, @PathVariable, ... and add it as a key to our body.
         */
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
                @Advice.Origin Executable method,
                @Advice.AllArguments(readOnly = false, typing = DYNAMIC) Object[] args
        ) throws Exception {
            Parameter[] parameters = method.getParameters();
            SpringAnnotationCollector.report(parameters, args);
        }
    }
}
