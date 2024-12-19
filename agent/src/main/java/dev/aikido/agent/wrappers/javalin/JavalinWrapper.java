package dev.aikido.agent.wrappers.javalin;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
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

import static net.bytebuddy.matcher.ElementMatchers.*;

public class JavalinWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(JavalinWrapper.class);

    @Override
    public String getName() {
        // We wrap the function handle which gets called with
        // HttpServletRequest request, HttpServletResponse response
        // And is part of io.javalin.http.servlet.JavalinServlet
        // See: https://github.com/javalin/javalin/blob/4219073de8e4873c05f7c1f709014a7e148e4162/javalin/src/main/java/io/javalin/http/servlet/JavalinServlet.kt#L33
        return JavalinAdvice.class.getName();
    }
    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        // io.javalin.http.servlet.JavalinServlet.handle
        return ElementMatchers.named("handle").and(isDeclaredBy(nameEndsWith("JavalinServlet")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameEndsWith("io.javalin.http.servlet.JavalinServlet");
    }

    private static class JavalinAdvice {
        // Wrapper to skip if it's inside this wrapper (i.e. our own response : )
        public record SkipOnWrapper(HttpServletResponse response) {};
        /**
         * @return the first value of Object is used as a boolean, if it's true code will
         * not execute (skip execution). The second value is the servlet request.
         */
        @Advice.OnMethodEnter(suppress = Throwable.class, skipOn = SkipOnWrapper.class)
        public static Object interceptOnEnter(
                @Advice.Argument(0) HttpServletRequest request,
                @Advice.Argument(1) HttpServletResponse response) throws IOException {
            ContextObject contextObject = new JavalinContextObject(request);
            WebRequestCollector.Res res = WebRequestCollector.report(contextObject);
            // Write a new response:
            if (res != null) {
                logger.debug("Writing a new response");
                HttpServletResponse newResponse = response;
                newResponse.setStatus(res.status());
                newResponse.setContentType("text/plain");
                newResponse.getWriter().write(res.msg());
                return new SkipOnWrapper(newResponse);
            }
            return response;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void interceptOnExit(@Advice.Enter Object response) {
            if (response == null) {
                return;
            }
            if (response instanceof HttpServletResponse httpServletResponse) {
                WebResponseCollector.report(httpServletResponse.getStatus()); // Report status code.
            }
        }
    }
}
