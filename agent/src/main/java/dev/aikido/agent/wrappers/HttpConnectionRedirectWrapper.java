package dev.aikido.agent.wrappers;

import dev.aikido.agent_api.collectors.RedirectCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.*;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class HttpConnectionRedirectWrapper implements Wrapper {
    public String getName() {
        // Wrap followRedirect0 function which follows redirects for HttpUrlConnection
        // Corretto :
        // https://github.com/corretto/corretto-21/blob/375920bee36488cd39b842e3041d071fd3d087ec/src/java.base/share/classes/sun/net/www/protocol/http/HttpURLConnection.java#L2842
        // OpenJDK :
        //  https://github.com/openjdk/jdk/blob/21f0ed50a224f19d083ef8e3b7b02b8f3dd31cac/src/java.base/share/classes/sun/net/www/protocol/http/HttpURLConnection.java#L2464
        return FollowRedirect0Advice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.nameContainsIgnoreCase("followRedirect0");
    }
    public static class FollowRedirect0Advice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file, specified with the AIKIDO_DIRECTORY env variable
        @Advice.OnMethodEnter
        public static void before(
                @Advice.This(typing = DYNAMIC, optional = true) HttpURLConnection target,
                @Advice.Argument(2) URL destUrl
        ) throws AikidoException {
            URL origin = target.getURL();
            if (origin == null || destUrl == null) {
                return;
            }
            try {
                RedirectCollector.report(origin, destUrl);
            } catch(Throwable e) {
                if(e instanceof AikidoException) {
                    throw e; // Do throw an Aikido vulnerability
                }
                // Ignore non-aikido throwables.
            }
        }
    }
}
