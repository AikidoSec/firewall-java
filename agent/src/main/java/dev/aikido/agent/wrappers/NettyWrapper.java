package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.context.NettyContext;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.collectors.WebResponseCollector;
import dev.aikido.agent_api.context.ContextObject;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import reactor.netty.Connection;
import reactor.netty.ConnectionObserver;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerState;


import java.lang.reflect.Executable;
import java.util.*;
import java.util.stream.Collectors;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * We need to get the Request/Response object before any filters get called, So we wrap HttpServer
 * Users can add their own handlers using .route(...) or .handle(...) on HttpServer. .route(...) uses .handle(...)
 * under-the-hood and .handle(...) creates a HttpServerHandle : https://github.com/reactor/reactor-netty/blob/bb60f6c25e4a305aa9ec2ed81f866e0dd7ae7552/reactor-netty-http/src/main/java/reactor/netty/http/server/HttpServer.java#L1186
 *
 */
public class NettyWrapper implements Wrapper {
    public String getName() {
        return MyGenericAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(getTypeMatcher()).and(named("onStateChange"));
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return isDeclaredBy(nameContains("reactor.netty.http.server.HttpServer"))
                .and(nameContains("HttpServerHandle"));
    }
    public class MyGenericAdvice {
        @Advice.OnMethodEnter//(suppress = Throwable.class)
        public static void before(
                @Advice.Origin Executable method,
                @Advice.Argument(0) Connection connection,
                @Advice.Argument(1) ConnectionObserver.State state
                ) {
            if (state == HttpServerState.REQUEST_RECEIVED) {
                if (connection instanceof HttpServerRequest target) {
                    // Extract headers & query parameters :
                    List<Map.Entry<String, String>> headerEntries = target.requestHeaders().entries();
                    Map<String, List<String>> query = new QueryStringDecoder(target.uri()).parameters();

                    // Extract cookies :
                    HashMap<String, List<String>> cookieMap = new HashMap<>();
                    for (Map.Entry<CharSequence, List<Cookie>> entry : target.allCookies().entrySet()) {
                        List<String> values = entry.getValue().stream().map(Cookie::value).collect(Collectors.toList());
                        cookieMap.put(entry.getKey().toString(), values);
                    }

                    // Create context object :
                    ContextObject context = new NettyContext(
                            target.method().toString(), target.uri(), target.remoteAddress(),  cookieMap, query, headerEntries
                    );
                    WebRequestCollector.report(context);
                }
            } else if(state == HttpServerState.DISCONNECTING) {
                if (connection instanceof HttpServerResponse response) {
                    WebResponseCollector.report(response.status().code());
                }
            }
        }
    }
}
