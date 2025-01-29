package dev.aikido.agent.wrappers.javalin;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.context.Context;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class JavalinContextClearWrapper implements Wrapper {

    @Override
    public String getName() {
        return JavalinContextClearAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(getTypeMatcher()).and(named("service"));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("io.javalin.jetty.JavalinJettyServlet");
    }

    public static class JavalinContextClearAdvice {
        @Advice.OnMethodEnter
        public static void before() {
            Context.reset();
        }
    }
}
