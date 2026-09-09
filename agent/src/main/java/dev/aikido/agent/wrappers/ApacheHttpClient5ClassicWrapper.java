package dev.aikido.agent.wrappers;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.hc.client5.http.classic.ExecChain;
import org.apache.hc.core5.http.ClassicHttpRequest;

public class ApacheHttpClient5ClassicWrapper implements Wrapper {
    @Override
    public String getName() {
        return ConnectExecAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return named("execute")
                .and(takesArguments(3))
                .and(takesArgument(0, named("org.apache.hc.core5.http.ClassicHttpRequest")))
                .and(takesArgument(1, named("org.apache.hc.client5.http.classic.ExecChain$Scope")))
                .and(takesArgument(2, named("org.apache.hc.client5.http.classic.ExecChain")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("org.apache.hc.client5.http.impl.classic.ConnectExec");
    }

    public static class ConnectExecAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) ClassicHttpRequest request, @Advice.Argument(1) ExecChain.Scope scope) {
            ApacheHttpClient5UrlReporter.report(request, scope.route, scope.clientContext);
        }
    }
}
