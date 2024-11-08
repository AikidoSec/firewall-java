package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.RequestBodyCollector;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class SpringFrameworkBodyWrapper implements Wrapper {
    @Override
    public String getName() {
        // We wrap the function readWithMessageConverters which itself returns an Object
        // It is part of org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver
        // See : https://github.com/spring-projects/spring-framework/blob/a75f22e5482b1e728f78aa67c8d4136a76f73084/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/AbstractMessageConverterMethodArgumentResolver.java#L148
        return SpringFrameworkBodyWrapperAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.nameContainsIgnoreCase("readWithMessageConverters");
    }

    private static class SpringFrameworkBodyWrapperAdvice {
        @Advice.OnMethodExit
        public static void interceptOnExit(@Advice.Return Object body) {
            RequestBodyCollector.report(body);
        }
    }
}
