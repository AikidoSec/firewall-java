package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.SpringContextObject;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * SpringFrameworkInvokeWrapper is a wrapper for the invokeHandlerMethod function
 * This functions gets called after the path variables are set (/post/:id)
 * So we fetch the path variables and store them inside the context.
 */
public class SpringFrameworkInvokeWrapper implements Wrapper {
    @Override
    public String getName() {
        // We wrap the function invokeHandlerMethod
        // It is part of package org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
        // See : https://github.com/spring-projects/spring-framework/blob/8d4a8cbbe5a412a985483b52c835ea1c6118530c/spring-webmvc/src/main/java/org/springframework/web/servlet/mvc/method/annotation/RequestMappingHandlerAdapter.java#L936
        return SpringFrameworkInvokeWrapperAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("RequestMappingHandlerAdapter"))
                .and(ElementMatchers.named("invokeHandlerMethod"));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.nameContains("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter");
    }

    private static class SpringFrameworkInvokeWrapperAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(@Advice.Argument(0) HttpServletRequest httpServletRequest  ) {
            if (httpServletRequest == null) {
                return;
            }
            Object pathVariables = httpServletRequest.getAttribute("org.springframework.web.servlet.HandlerMapping.uriTemplateVariables");
            if (pathVariables != null) {
                if (Context.get() instanceof SpringContextObject springContextObject) {
                    // Set path variables in context object :
                    springContextObject.setParams(pathVariables);
                    Context.set(springContextObject);
                }
            }
        }
    }
}
