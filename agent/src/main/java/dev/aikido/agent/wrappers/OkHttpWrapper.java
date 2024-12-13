package dev.aikido.agent.wrappers;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import static dev.aikido.agent.helpers.ClassLoader.fetchMethod;

public class OkHttpWrapper implements Wrapper {
    public static final Logger logger = LogManager.getLogger(OkHttpWrapper.class);

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
                Method reportUrlCollector = fetchMethod("dev.aikido.agent_api.collectors.URLCollector", "report");
                reportUrlCollector.invoke(null, url);
            } catch (Throwable e) {
                logger.trace(e);
            }
        }
    }
}
