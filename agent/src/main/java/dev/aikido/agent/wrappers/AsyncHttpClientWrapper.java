package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.asynchttpclient.Request;

public class AsyncHttpClientWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(AsyncHttpClientWrapper.class);

    @Override
    public String getName() {
        return ExecuteAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.named("execute")
                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.asynchttpclient.Request")))
                .and(ElementMatchers.takesArgument(1, ElementMatchers.named("org.asynchttpclient.AsyncHandler")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.named("org.asynchttpclient.DefaultAsyncHttpClient");
    }

    public static class ExecuteAdvice {
        @Advice.OnMethodEnter
        public static void before(@Advice.Argument(0) Request request) throws Throwable {
            try {
                if (request == null || request.getUri() == null) {
                    return;
                }

                URLCollector.report(request.getUri().toJavaNetURI().toURL());
            } catch (AikidoException e) {
                throw e;
            } catch (Throwable e) {
                logger.debug(e);
            }
        }
    }
}
