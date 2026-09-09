package dev.aikido.agent.wrappers;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.core5.http.HttpRequest;

public class ApacheHttpClient5AsyncWrapper implements Wrapper {
    @Override
    public String getName() {
        return AsyncConnectExecAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return named("execute")
                .and(takesArguments(5))
                .and(takesArgument(0, named("org.apache.hc.core5.http.HttpRequest")))
                .and(takesArgument(2, named("org.apache.hc.client5.http.async.AsyncExecChain$Scope")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("org.apache.hc.client5.http.impl.async.AsyncConnectExec");
    }

    public static class AsyncConnectExecAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) HttpRequest request, @Advice.Argument(2) AsyncExecChain.Scope scope) {
            ApacheHttpClient5UrlReporter.report(request, scope.route, scope.clientContext);
        }
    }
}
