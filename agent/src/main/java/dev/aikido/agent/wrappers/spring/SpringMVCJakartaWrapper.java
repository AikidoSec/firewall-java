package dev.aikido.agent.wrappers.spring;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.SpringMVCContextObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import jakarta.servlet.http.HttpServletRequest;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class SpringMVCJakartaWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(SpringMVCJakartaWrapper.class);

    @Override
    public String getName() {
        // We wrap the function doFilterInternal which gets called with
        // HttpServletRequest request, HttpServletResponse response
        // And is part of org.springframework.web.filter.RequestContextFilter
        // See: https://github.com/spring-projects/spring-framework/blob/4749d810db0261ce16ae5f32da6d375bb8087430/spring-web/src/main/java/org/springframework/web/filter/RequestContextFilter.java#L92
        return SpringMVCAdvice.class.getName();
    }
    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.nameContainsIgnoreCase("doFilterInternal")
            .and(takesArgument(0, nameContains("jakarta")))
            .and(takesArgument(1, nameContains("jakarta")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("org.springframework.web.filter.RequestContextFilter");
    }

    public static class SpringMVCAdvice {
        // Wrapper to skip if it's inside this wrapper (i.e. our own response : )
        public record SkipOnWrapper(HttpServletResponse response) {};
        /**
         * @return the first value of Object is used as a boolean, if it's true code will
         * not execute (skip execution). The second value is the servlet request.
         */
        @Advice.OnMethodEnter(skipOn = SkipOnWrapper.class, suppress = Throwable.class)
        public static Object interceptOnEnter(
                @Advice.Origin Executable method,
                @Advice.Argument(value = 0, typing = DYNAMIC, optional = true) HttpServletRequest request,
                @Advice.Argument(value = 1, typing = DYNAMIC, optional = true) HttpServletResponse response) throws Throwable {
            if (request == null) {
                return response;
            }
            // extract headers :
            HashMap<String, Enumeration<String>> headersMap = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValue = request.getHeaders(headerName);
                headersMap.put(headerName, headerValue);
            }
            // extract cookies :
            HashMap<String, List<String>> cookiesMap = new HashMap<>();
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    // If no entry exists, create a new empty entry
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

            // Write a new response:
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
            if (response != null && response instanceof HttpServletResponse httpServletResponse) {
                WebResponseCollector.report(httpServletResponse.getStatus()); // Report status code.
            }
        }
    }
}
