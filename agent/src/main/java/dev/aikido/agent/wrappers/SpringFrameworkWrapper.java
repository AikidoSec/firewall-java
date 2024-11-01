package dev.aikido.agent.wrappers;

import com.google.gson.Gson;
import dev.aikido.agent_api.background.utilities.IPCDefaultClient;
import dev.aikido.agent_api.context.Context;
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

import static dev.aikido.agent_api.helpers.url.IsUsefulRoute.isUsefulRoute;

public class SpringFrameworkWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(SpringFrameworkWrapper.class);

    @Override
    public String getName() {
        // We wrap the function processRequest which gets called with
        // HttpServletRequest request, HttpServletResponse response
        // And is part of org.springframework.web.servlet.FrameworkServlet
        // See : https://github.com/spring-projects/spring-framework/blob/eb4bf1c0a65db32a161abd6fc89c69623dd80418/spring-webmvc/src/main/java/org/springframework/web/servlet/FrameworkServlet.java#L996
        return SpringFrameworkAdvice.class.getName();
    }
    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.nameContainsIgnoreCase("processRequest");
    }

    private static class SpringFrameworkAdvice {
        @Advice.OnMethodEnter
        public static HttpServletResponse interceptOnEnter(
                @Advice.Origin Executable method,
                @Advice.Argument(0) Object request,
                @Advice.Argument(1) Object response) {
            try {
                Context.reset();
                ContextObject contextObject = new SpringContextObject((HttpServletRequest) request);
                Context.set(contextObject);
                return (HttpServletResponse) response;
            } catch (Throwable e) {
                logger.debug(e);
            }
            return null;
        }

        @Advice.OnMethodExit
        public static void interceptOnExit(@Advice.Enter HttpServletResponse response) {
            if (response == null) {
                return;
            }
            int statusCode = response.getStatus();
            ContextObject context = Context.get();
            boolean currentRouteUseful = isUsefulRoute(statusCode, context.getRoute(), context.getMethod());
            if (currentRouteUseful) {
                Gson gson = new Gson();
                String data = "INIT_ROUTE$" + gson.toJson(context.getRouteMetadata());
                new IPCDefaultClient().sendData(data, false /* does not receive a response*/);
            }
        }
    }
}
