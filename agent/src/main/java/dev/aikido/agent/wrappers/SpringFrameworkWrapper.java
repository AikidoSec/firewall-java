package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.SpringContextObject;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Executable;

public class SpringFrameworkWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(SpringFrameworkWrapper.class);

    @Override
    public String getName() {
        // We wrap the function doFilterInternal which gets called with
        // HttpServletRequest request, HttpServletResponse response
        // And is part of org.springframework.web.filter.RequestContextFilter
        // See: https://github.com/spring-projects/spring-framework/blob/4749d810db0261ce16ae5f32da6d375bb8087430/spring-web/src/main/java/org/springframework/web/filter/RequestContextFilter.java#L92
        return SpringFrameworkAdvice.class.getName();
    }
    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.nameContainsIgnoreCase("doFilterInternal");
    }

    private static class SpringFrameworkAdvice {
        // Wrapper to skip if it's inside this wrapper (i.e. our own response : )
        public record SkipOnWrapper(HttpServletResponse response) {};
        /**
         * @return the first value of Object is used as a boolean, if it's true code will
         * not execute (skip execution). The second value is the servlet request.
         */
        @Advice.OnMethodEnter(skipOn = SkipOnWrapper.class)
        public static Object interceptOnEnter(
                @Advice.Origin Executable method,
                @Advice.Argument(0) Object request,
                @Advice.Argument(1) Object response) {
            try {
                ContextObject contextObject = new SpringContextObject((HttpServletRequest) request);

                // Write a new response:
                WebRequestCollector.Res res = WebRequestCollector.report(contextObject);
                if (res != null) {
                    logger.debug("Writing a new response");
                    HttpServletResponse newResponse = (HttpServletResponse) response;
                    newResponse.setStatus(res.status());
                    newResponse.setContentType("text/plain");
                    newResponse.getWriter().write(res.msg());
                    return new SkipOnWrapper(newResponse);
                }
                return response;
            } catch (Throwable e) {
                logger.debug(e);
            }
            return null;
        }

        @Advice.OnMethodExit
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
