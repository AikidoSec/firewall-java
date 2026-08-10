package dev.aikido.agent.wrappers.ring;

import com.google.gson.Gson;
import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Captures the request body for Jetty EE9's HttpServletRequest and attaches it (best-effort
 * JSON parsed) to the ContextObject RingJettyServletWrapper already stored for this thread.
 * Ring calls getInputStream() once per request, so draining and replacing it here is safe.
 */
public class RingRequestBodyWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(RingRequestBodyWrapper.class);

    @Override
    public String getName() {
        return RingRequestBodyAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return named("getInputStream").and(takesArguments(0));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContains("org.eclipse.jetty.ee9.nested")
                .and(hasSuperType(named("jakarta.servlet.http.HttpServletRequest")));
    }

    // Must be public: Advice bytecode is inlined into the instrumented class itself, which
    // can't access a private nested class from a different top-level class (IllegalAccessError).
    public static class BufferedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream backing;

        public BufferedServletInputStream(byte[] body) {
            this.backing = new ByteArrayInputStream(body);
        }

        @Override
        public boolean isFinished() {
            return backing.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() {
            return backing.read();
        }
    }

    public static class RingRequestBodyAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void interceptOnExit(
                @Advice.Return(readOnly = false, typing = DYNAMIC) ServletInputStream returnValue) throws Throwable {
            if (returnValue == null) {
                return;
            }
            byte[] bodyBytes = returnValue.readAllBytes();

            ContextObject ctx = Context.get();
            if (ctx != null && bodyBytes.length > 0) {
                try {
                    Object parsedBody = new Gson().fromJson(new String(bodyBytes, StandardCharsets.UTF_8), Object.class);
                    ctx.setBody(parsedBody);
                } catch (Throwable t) {
                    logger.debug("RingRequestBodyWrapper failed to parse JSON body: %s", t.getMessage());
                }
            }

            returnValue = new BufferedServletInputStream(bodyBytes);
        }
    }
}
