package dev.aikido.agent.wrappers;

import dev.aikido.agent.helpers.Logger;
import dev.aikido.agent_api.collectors.URLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

public class OkHttpWrapper implements Wrapper {
    public static final Logger logger = Logger.getLogger();

    public String getName() {
        // Wrap newCall function which makes a HTTP Request
        // https://square.github.io/okhttp/5.x/okhttp/okhttp3/-ok-http-client/new-call.html
        return OkHttpAdvice.class.getName();
    }
    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.nameContains("okhttp3.OkHttpClient");
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("OkHttpClient"))
                .and(ElementMatchers.nameContainsIgnoreCase("newCall"));
    }
    public class OkHttpAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void before(
                @Advice.Argument(0) Object request
        ) {
            // This Object is an okhttp3.Request object, we will use reflection to access the URL:
            // We want to (safely) access request.url.toUrl()
            if (request == null) {
                return;
            }
            try {
                Class<?> requestClass = request.getClass();

                // Fetch urlObject :
                Field urlField = requestClass.getDeclaredField("url");
                urlField.setAccessible(true);
                Object urlObject = urlField.get(request);

                if (urlObject == null || !urlObject.getClass().getName().equals("okhttp3.HttpUrl")) {
                    return;
                }
                // Fetch URL object :
                Method toUrlMethod = urlObject.getClass().getMethod("url");
                URL url = (URL) toUrlMethod.invoke(urlObject);

                // Report the URL
                URLCollector.report(url);
            } catch (Throwable e) {
                logger.trace(e);
            }
        }
    }
}
