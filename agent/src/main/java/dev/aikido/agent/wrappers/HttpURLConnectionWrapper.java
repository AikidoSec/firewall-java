package dev.aikido.agent.wrappers;

import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.net.*;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class HttpURLConnectionWrapper implements Wrapper {
    public String getName() {
        // Wrap Constructor of HttpURLConnection
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html
        return ConstructorAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(URL.class).and(named("openConnection"));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return is(URL.class);
    }

    public static class ConstructorAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void before(
                @Advice.This(typing = DYNAMIC) URL url
        ) throws Throwable {
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.URL_COLLECTOR_REPORT, url);
        }
    }
}
