package dev.aikido.agent.wrappers;

import dev.aikido.agent.contexts.NettyContext;
import dev.aikido.agent_api.collectors.WebRequestCollector;
import dev.aikido.agent_api.context.ContextObject;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import reactor.netty.http.server.HttpServerRequest;

import java.util.*;
import java.util.stream.Collectors;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class NettyWrapper implements Wrapper {
    public String getName() {
        return MyGenericAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(getTypeMatcher()).and(isConstructor());
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return hasSuperType(nameContains("reactor.netty.http.server.HttpServerRequest").and(isInterface()))
                .and((nameContains("HttpServerOperations")));
    }
    public class MyGenericAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void after(
                @Advice.This(typing = DYNAMIC, optional = true) HttpServerRequest target
        ) {
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
    }
}
