package dev.aikido.AikidoAgent.wrappers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aikido.AikidoAgent.context.ContextObject;
import dev.aikido.AikidoAgent.context.SpringContextObject;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;

import jakarta.servlet.http.HttpServletRequest;

public class SpringFrameworkWrapper extends Wrapper {
    public static AsmVisitorWrapper get() {
        // We wrap the function processRequest which gets called with
        // HttpServletRequest request, HttpServletResponse response
        // And is part of org.springframework.web.servlet.FrameworkServlet
        // See : https://github.com/spring-projects/spring-framework/blob/eb4bf1c0a65db32a161abd6fc89c69623dd80418/spring-webmvc/src/main/java/org/springframework/web/servlet/FrameworkServlet.java#L996
        return Advice.to(SpringFrameworkAdvice.class)
                .on(ElementMatchers.named("processRequest"));
    }

    private static class SpringFrameworkAdvice {
        @Advice.OnMethodEnter
        public static HttpServletResponse interceptOnEnter(
                @Advice.Argument(0) HttpServletRequest request,
                @Advice.Argument(1) HttpServletResponse response) {
            ContextObject contextObject = new SpringContextObject(request);

            System.out.printf("Url: %s with Method: %s \n", contextObject.getUrl(), contextObject.getMethod());
            System.out.println(contextObject.toJson());
            return response;
        }

        @Advice.OnMethodExit
        public static void interceptOnExit(@Advice.Enter HttpServletResponse response) {
            int statusCode = response.getStatus();
            System.out.println("HTTP Status Code: " + statusCode);
        }
    }
}
