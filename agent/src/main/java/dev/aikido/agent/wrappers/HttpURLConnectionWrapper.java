package dev.aikido.agent.wrappers;
import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;
import java.net.*;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class HttpURLConnectionWrapper implements Wrapper {
    public String getName() {
        // Wrap Constructor of HttpURLConnection
        // https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html
        return ConstructorAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.nameContainsIgnoreCase("java.net.HttpURLConnection"))
                .and(ElementMatchers.isConstructor());
    }
    public static class ConstructorAdvice {
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file
        @Advice.OnMethodEnter
        public static void before(
                @Advice.This(typing = DYNAMIC, optional = true) HttpURLConnection target,
                @Advice.Origin Executable method
        ) throws AikidoException {
            try {
                URLCollector.report(target.getURL());
            } catch(Throwable e) {
                if(e instanceof AikidoException) {
                    throw e; // Do throw an Aikido vulnerability
                }
                // Ignore non-aikido throwables.
            }
        }
    }
}
