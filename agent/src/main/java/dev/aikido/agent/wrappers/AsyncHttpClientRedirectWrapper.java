package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.asynchttpclient.Request;
import org.asynchttpclient.netty.NettyResponseFuture;

import java.net.URL;

public class AsyncHttpClientRedirectWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(AsyncHttpClientRedirectWrapper.class);

    @Override
    public String getName() {
        return SendRequestAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.named("sendRequest")
                .and(ElementMatchers.isPublic())
                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.asynchttpclient.Request")))
                .and(ElementMatchers.takesArgument(2, ElementMatchers.named("org.asynchttpclient.netty.NettyResponseFuture")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.named("org.asynchttpclient.netty.request.NettyRequestSender");
    }

    public static class SendRequestAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) Request request,
                @Advice.Argument(2) NettyResponseFuture<?> future
        ) throws Throwable {
            try {
                if (request == null || request.getUri() == null || future == null) {
                    return;
                }

                Request currentRequest = future.getCurrentRequest();
                if (currentRequest == null || currentRequest.getUri() == null) {
                    return;
                }

                URL origin = currentRequest.getUri().toJavaNetURI().toURL();
                URL destination = request.getUri().toJavaNetURI().toURL();
                if (!origin.equals(destination)) {
                    RedirectCollector.report(origin, destination);
                }
            } catch (AikidoException e) {
                throw e;
            } catch (Throwable e) {
                logger.debug(e);
            }
        }
    }
}
