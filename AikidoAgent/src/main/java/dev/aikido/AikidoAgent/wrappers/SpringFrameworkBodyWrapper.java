package dev.aikido.AikidoAgent.wrappers;

import dev.aikido.AikidoAgent.context.Context;
import dev.aikido.AikidoAgent.context.ContextObject;
import dev.aikido.AikidoAgent.context.SpringContextObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static dev.aikido.AikidoAgent.helpers.url.IsUsefulRoute.isUsefulRoute;

public class SpringFrameworkBodyWrapper extends Wrapper {
    public static AsmVisitorWrapper get() {
        // We wrap the function readWithMessageConverters which itself returns an Object
        // It is part of org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver
        // See : https://github.com/spring-projects/spring-framework/blob/a75f22e5482b1e728f78aa67c8d4136a76f73084/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodArgumentResolver.java#L148
        return Advice.to(SpringFrameworkAdvice.class)
                .on(ElementMatchers.named("readWithMessageConverters"));
    }

    private static class SpringFrameworkAdvice {
        @Advice.OnMethodExit
        public static void interceptOnExit(@Advice.Return Object body) {
            ContextObject contextObj = Context.get();
            contextObj.setBody(body);
            Context.set(contextObj);
        }
    }
}
