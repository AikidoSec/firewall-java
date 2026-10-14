package dev.aikido.agent.wrappers.spring;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.SpringMVCContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

// Fallback context creator for Spring MVC: wraps FrameworkServlet#processRequest (runs for every
// DispatcherServlet request) and creates the context when RequestContextFilter is absent from the chain.
public class SpringMVCDispatcherWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(SpringMVCDispatcherWrapper.class);

    @Override
    public String getName() {
        return SpringMVCDispatcherAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.nameContainsIgnoreCase("processRequest")
            .and(takesArgument(0, nameContains("jakarta")))
            .and(takesArgument(1, nameContains("jakarta")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("org.springframework.web.servlet.FrameworkServlet");
    }

    public static class SpringMVCDispatcherAdvice {
        public record SkipOnWrapper(HttpServletResponse response) {}

        @Advice.OnMethodEnter(skipOn = SkipOnWrapper.class, suppress = Throwable.class)
        public static Object interceptOnEnter(
                @Advice.Origin Executable method,
                @Advice.Argument(value = 0, typing = DYNAMIC, optional = true) HttpServletRequest request,
                @Advice.Argument(value = 1, typing = DYNAMIC, optional = true) HttpServletResponse response) throws Throwable {
            if (request == null) {
                return null;
            }
            // Per-request marker (not Context.get()) since pooled threads keep a stale context between requests.
            if (request.getAttribute("dev.aikido.zen.springContextCreated") != null) {
                return null;
            }
            HashMap<String, Enumeration<String>> headersMap = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValue = request.getHeaders(headerName);
                headersMap.put(headerName, headerValue);
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

            ContextObject contextObject = new SpringMVCContextObject(
                    request.getMethod(), request.getRequestURL(), request.getRemoteAddr(),
                    request.getParameterMap(), cookiesMap, headersMap, request.getQueryString()
            );

            request.setAttribute("dev.aikido.zen.springContextCreated", Boolean.TRUE);
            WebRequestCollector.Res res = WebRequestCollector.report(contextObject);
            if (res != null && response != null) {
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
