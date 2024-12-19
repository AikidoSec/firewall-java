package dev.aikido.agent.wrappers.javalin;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.JavalinContextObject;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * We wrap the functions that fetch path parameters i.e. GET /pet/{id}
 * On io.javalin.http.servlet.JavalinServletContext
 * -> pathParam(String key) : key-value pair
 * -> pathParamMap() : All data
 * See: https://github.com/javalin/javalin/blob/4219073de8e4873c05f7c1f709014a7e148e4162/javalin/src/main/java/io/javalin/http/servlet/JavalinServletContext.kt#L146
 */
public class JavalinPathParamWrapper implements Wrapper {
    @Override
    public String getName() {
        return JavalinPathParamAdvice.class.getName();
    }
    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(nameContainsIgnoreCase("JavalinServletContext"))
                .and(named("pathParam").and(takesArgument(0, String.class)))
                .or(named("pathParamMap").and(takesNoArguments()));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("io.javalin.http.servlet.JavalinServletContext");
    }

    private static class JavalinPathParamAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void after(
                @Advice.Argument(value = 0, optional = true) String key,
                @Advice.Return(readOnly = false, typing = DYNAMIC) Object returnValue
        ) {
            JavalinContextObject context = (JavalinContextObject) Context.get();
            if (returnValue instanceof String pathParameter && key != null) {
                // pathParam() function
                context.setParam(key, pathParameter);
            } else if(returnValue instanceof Map pathParameters) {
                // pathParamMap() function
                context.setParams(pathParameters);
            }
            Context.set(context);
        }
    }
}
