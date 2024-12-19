package dev.aikido.agent.wrappers.javalin;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.JavalinContextObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;

import static javax.management.Query.or;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * We wrap the functions that convert raw body data into an object :
 * On io.javalin.http.servlet.JavalinServletContext
 * -> body()
 * -> bodyAsClass(Type t)
 * -> bodyStreamAsClass(Type t)
 * -> formParamMap(...) [Used by the other form functions]
 * See: https://github.com/javalin/javalin/blob/4219073de8e4873c05f7c1f709014a7e148e4162/javalin/src/main/java/io/javalin/http/Context.kt#L138
 */
public class JavalinBodyWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(JavalinBodyWrapper.class);

    @Override
    public String getName() {
        return JavalinBodyAdvice.class.getName();
    }
    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(nameContainsIgnoreCase("javalin").and(nameContainsIgnoreCase("Context")))
                .and(named("bodyAsClass").and(takesArgument(0, Type.class)))
                .or(named("bodyStreamAsClass").and(takesArgument(0, Type.class)))
                .or(named("body").and(takesNoArguments()))
                .or(named("formParamMap").and(takesNoArguments()));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("io.javalin.http.Context").or(named("io.javalin.http.servlet.JavalinServletContext"));
    }

    private static class JavalinBodyAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void after(
                @Advice.Origin Executable method,
                @Advice.Return(readOnly = false, typing = DYNAMIC) Object returnValue
        ) {
            ContextObject context = Context.get();

            if (returnValue instanceof String) {
                // body() function, string takes lowest priority, if body is set ignore
                if (context.getBody() == null) {
                    context.setBody(returnValue);
                }
            } else {
                // Can be body(Stream)AsClass or formParamMap, we can override :
                context.setBody(returnValue);
            }

            // Store context :
            Context.set(context);
        }
    }
}
