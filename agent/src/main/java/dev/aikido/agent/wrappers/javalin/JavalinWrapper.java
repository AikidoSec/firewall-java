package dev.aikido.agent.wrappers.javalin;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.JavalinContextObject;
import io.javalin.http.servlet.JavalinServletContext;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.*;

import java.util.*;

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
        public static void before(
                @Advice.Argument(value = 0, typing = DYNAMIC, optional = true) JavalinServletContext ctx
        ) {
            if (Context.get() != null) {
                return; // Do not extract if context already exists.
            }

            // cookieMap() returns the last value if the key is the same for the cookie, however cookie(str key) returns
            // the first one (in accordance with IETF rules), We merge both values to ensure full coverage :
            HashMap<String, List<String>> cookies = new HashMap<>();
            for(Map.Entry<String, String> cookie: ctx.cookieMap().entrySet()) {
                List<String> cookieValues = new ArrayList<>();
                cookieValues.add(cookie.getValue()); // Add the value from ctx.cookieMap()
                if (!ctx.cookie(cookie.getKey()).equals(cookie.getValue())) {
                    cookieValues.add(ctx.cookie(cookie.getKey())); // Add the value from ctx.cookie(str key)
                }
                cookies.put(cookie.getKey(), cookieValues); // Store
            }

            // headerMap() returns the value for the last sent header if the key is the same, this is also how
            // ctx.header(str key) operates so we don't have to change anything.
            Map<String, String> headers = ctx.headerMap();

            // Create a context object for Javalin
            ContextObject context = new JavalinContextObject(
                    ctx.method().name(), ctx.url(), ctx.ip(),
                    ctx.queryParamMap(), cookies, headers
            );
            WebRequestCollector.Res response = WebRequestCollector.report(context);

            // Write a response if necessary :
            if (response != null) {
                ctx.result(response.msg());
                ctx.status(response.status());
                ctx.skipRemainingHandlers();
            }
        }
    }

}
