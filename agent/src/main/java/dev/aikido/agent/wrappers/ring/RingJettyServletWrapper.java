package dev.aikido.agent.wrappers.ring;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.RingContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Populates request context for Ring apps served through ring-jetty-adapter (Jetty 12/EE9),
 * which dispatches requests through a subclass of ServletHandler overriding doHandle(...).
 * Body capture lives in RingRequestBodyWrapper instead: Clojure's proxy-generated doHandle
 * boxes its arguments for delegation, so reassigning the request argument here never reaches
 * Ring's own handler.
 */
public class RingJettyServletWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(RingJettyServletWrapper.class);

    @Override
    public String getName() {
        return RingJettyAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return named("doHandle").and(takesArguments(4));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return hasSuperType(named("org.eclipse.jetty.ee9.servlet.ServletHandler"));
    }

    public static class RingJettyAdvice {
        public record SkipOnWrapper(HttpServletResponse response) {}

        @Advice.OnMethodEnter(skipOn = SkipOnWrapper.class, suppress = Throwable.class)
        public static Object interceptOnEnter(
                @Advice.Origin Executable method,
                @Advice.Argument(value = 2, typing = DYNAMIC, optional = true) HttpServletRequest request,
                @Advice.Argument(value = 3, typing = DYNAMIC, optional = true) HttpServletResponse response) throws Throwable {
            if (request == null) {
                return response;
            }

            HashMap<String, Enumeration<String>> headersMap = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headersMap.put(headerName, request.getHeaders(headerName));
            }

            HashMap<String, List<String>> cookiesMap = new HashMap<>();
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (!cookiesMap.containsKey(cookie.getName())) {
                        cookiesMap.put(cookie.getName(), new ArrayList<>());
                    }
                    cookiesMap.get(cookie.getName()).add(cookie.getValue());
                }
            }

            ContextObject contextObject = new RingContextObject(
                    request.getMethod(), request.getRequestURL(), request.getRemoteAddr(),
                    request.getParameterMap(), cookiesMap, headersMap, request.getQueryString()
            );

            WebRequestCollector.Res res = WebRequestCollector.report(contextObject);
            if (res != null) {
                logger.trace("Writing a new response");
                response.setStatus(res.status());
                response.setContentType("text/plain");
                response.getWriter().write(res.msg());
                return new SkipOnWrapper(response);
            }
            return response;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void interceptOnExit(@Advice.Enter Object response) {
            if (response instanceof HttpServletResponse httpServletResponse) {
                WebResponseCollector.report(httpServletResponse.getStatus());
            }
        }
    }
}
