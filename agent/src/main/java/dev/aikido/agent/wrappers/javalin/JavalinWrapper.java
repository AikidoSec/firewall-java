package dev.aikido.agent.wrappers.javalin;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.JavalinContextObject;
import io.javalin.http.servlet.JavalinServletContext;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.*;

import java.lang.reflect.Executable;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/** JavalinWrapper
 * We're wrapping io.javalin.router.ParsedEndpoint's handle(JavalinServletContext, ...) function.
 * See here: https://github.com/javalin/javalin/blob/8b1dc1a55c28618df7f9f044aad4949d30a8cca8/javalin/src/main/java/io/javalin/router/ParsedEndpoint.kt#L14
 */
public class JavalinWrapper implements Wrapper {
    public String getName() {
        return JavalinAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(getTypeMatcher()).and(named("handle"));
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("io.javalin.router.ParsedEndpoint");
    }
    public class JavalinAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static JavalinServletContext before(
                @Advice.This(typing = DYNAMIC, optional = true) Object target,
                @Advice.Origin Executable method,
                @Advice.Argument(value = 0, typing = DYNAMIC, optional = true) JavalinServletContext ctx
                ) {
            if (Context.get() != null) {
                return ctx; // Do not extract if context already exists.
            }
            // Create a context object :
            ContextObject context = new JavalinContextObject(
                    ctx.method().name(), ctx.url(), ctx.ip(),
                    ctx.queryParamMap(), ctx.cookieMap(), ctx.headerMap()
            );
            WebRequestCollector.Res response = WebRequestCollector.report(context);

            // Write a response if necessary :
            if (response != null) {
                ctx.result(response.msg());
                ctx.status(response.status());
                ctx.skipRemainingHandlers();
            }

            return ctx;
        }
        @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
        public static void after(
                @Advice.Enter(typing = DYNAMIC) JavalinServletContext ctx
        ) {
            WebResponseCollector.report(ctx.statusCode());
        }
    }

}
