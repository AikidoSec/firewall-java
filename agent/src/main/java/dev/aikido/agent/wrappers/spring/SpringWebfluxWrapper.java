package dev.aikido.agent.wrappers.spring;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.ContextObject;
import dev.aikido.agent_api.context.SpringWebfluxContextObject;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Wraps handle() function on HttpWebHandlerAdapter for Spring Webflux.
 * Creates context object, writes a response (e.g. ip blocking), and reports status code.
 * [github link](https://github.com/spring-projects/spring-framework/blob/7405e2069098400a01ee1e84ce72c45c6498b28d/spring-web/src/main/java/org/springframework/web/server/adapter/HttpWebHandlerAdapter.java#L269)
 */
public class SpringWebfluxWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(SpringWebfluxWrapper.class);

    @Override
    public String getName() {
        return SpringWebfluxAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {

        return isDeclaredBy(getTypeMatcher()).and(named("handle"))
                .and(takesArgument(0, nameContains("ServerHttpRequest")))
                .and(takesArgument(1, nameContains("ServerHttpResponse")));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return nameContainsIgnoreCase("org.springframework.web.server.adapter.HttpWebHandlerAdapter");
    }

    public record SkipOnWrapper(Mono<Void> mono) {
    }

    public static class SpringWebfluxAdvice {
        @Advice.OnMethodEnter(skipOn = SkipOnWrapper.class, suppress = Throwable.class)
        public static Object onEnter(
                @Advice.Origin Executable method,
                @Advice.Argument(value = 0, typing = DYNAMIC, optional = true) ServerHttpRequest req,
                @Advice.Argument(value = 1, typing = DYNAMIC, optional = true) ServerHttpResponse res
        ) {
            if (req == null) {
                return null;
            }
            // Extract headers & query parameters :
            Set<Map.Entry<String, List<String>>> headerEntries = req.getHeaders().entrySet();
            Map<String, List<String>> query = req.getQueryParams();

            // Extract cookies :
            HashMap<String, List<String>> cookieMap = new HashMap<>();
            for (Map.Entry<String, List<HttpCookie>> entry : req.getCookies().entrySet()) {
                List<String> values = entry.getValue().stream().map(HttpCookie::getValue).collect(Collectors.toList());
                cookieMap.put(entry.getKey(), values);
            }

            // Create context object :
            ContextObject context = new SpringWebfluxContextObject(
                    req.getMethod().toString(), req.getURI().toString(),
                    Objects.requireNonNull(req.getRemoteAddress()),
                    cookieMap, query, req.getHeaders().toSingleValueMap()
            );

            // If a response is present, write the response :
            WebRequestCollector.Res zenResponse = WebRequestCollector.report(context);
            if (zenResponse != null && res != null) {
                // Write message :
                DataBufferFactory dataBufferFactory = res.bufferFactory();
                DataBuffer dataBuffer = dataBufferFactory.wrap(zenResponse.msg().getBytes(StandardCharsets.UTF_8));

                res.setRawStatusCode(zenResponse.status()); // Set status code
                return new SkipOnWrapper(res.writeWith(Mono.just(dataBuffer)));
            }

            return res; // Return to analyze status code in OnMethodExit.
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Enter Object enterResult,
                @Advice.Return(readOnly = false) Mono<Void> returnValue
        ) {
            if (enterResult instanceof SkipOnWrapper wrapper && wrapper.mono() != null) {
                returnValue = wrapper.mono();
            } else if (enterResult instanceof ServerHttpResponse res) {
                // Report status code of response :
                Integer statusCode = res.getRawStatusCode();
                if (statusCode != null) {
                    WebResponseCollector.report(statusCode);
                }
            }
        }
    }

}
