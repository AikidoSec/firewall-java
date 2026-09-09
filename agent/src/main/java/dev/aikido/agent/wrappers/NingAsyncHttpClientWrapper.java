package dev.aikido.agent.wrappers;

import com.ning.http.client.Request;
import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.URL;

public class NingAsyncHttpClientWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(NingAsyncHttpClientWrapper.class);

    @Override
    public String getName() {
        return ExecuteAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.named("execute")
                .and(ElementMatchers.isPublic())
                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("com.ning.http.client.Request")))
                .and(ElementMatchers.takesArgument(1, ElementMatchers.named("com.ning.http.client.AsyncHandler")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.hasSuperType(ElementMatchers.named("com.ning.http.client.AsyncHttpProvider"));
    }

    public static class ExecuteAdvice {
        @Advice.OnMethodEnter
        public static void before(@Advice.Argument(0) Request request) throws Throwable {
            try {
                if (request == null || request.getUrl() == null) {
                    return;
                }

                URLCollector.report(new URL(request.getUrl()));
            } catch (AikidoException e) {
                throw e;
            } catch (Throwable e) {
                logger.debug(e);
            }
        }
    }
}
