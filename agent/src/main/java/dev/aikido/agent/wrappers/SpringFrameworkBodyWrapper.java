package dev.aikido.agent.wrappers;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

import static dev.aikido.agent.helpers.ClassLoader.fetchMethod;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;

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

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver");
    }

    private static class SpringFrameworkBodyWrapperAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void interceptOnExit(@Advice.Return Object body) throws Exception {
            Method reportBodyMethod = fetchMethod("dev.aikido.agent_api.collectors.RequestBodyCollector", "report");
            reportBodyMethod.invoke(null, body);
        }
    }
}
