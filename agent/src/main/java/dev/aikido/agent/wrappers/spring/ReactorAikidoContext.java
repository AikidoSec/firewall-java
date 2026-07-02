package dev.aikido.agent.wrappers.spring;

import dev.aikido.agent_api.collectors.URLCollector;
import dev.aikido.agent_api.context.ContextObject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;

/**
 * Carries the Aikido ContextObject through Reactor's own Context so it survives scheduler hops
 * (e.g. .publishOn()) between the incoming WebFlux request and any WebClient calls made while
 * handling it - unlike Context.get()'s ThreadLocal, which only sees the current OS thread.
 *
 * Everything here is Object-typed and goes through reflection, rooted at the classloader of a
 * live Mono instance passed in. reactor-core is compileOnly for this module: a *separate* class
 * (like this one, as opposed to an @Advice method whose parameter types ByteBuddy resolves
 * specially against the woven target's own classloader) that declares Mono, Context or
 * ContextView as a concrete parameter/return type throws NoClassDefFoundError at class
 * verification time on the agent's own classloader, which has no visibility into the target
 * application's classpath. Must be public: the woven target class (in a completely different
 * package) needs to call into it directly.
 */
public final class ReactorAikidoContext {
    private static final String KEY = "dev.aikido.agent.wrappers.spring.ReactorAikidoContextKey";

    private ReactorAikidoContext() {}

    // `mono` is a Mono<Void>, returned Object is that same Mono<Void> wrapped with .contextWrite().
    public static Object write(Object mono, ContextObject context) {
        try {
            ClassLoader cl = mono.getClass().getClassLoader();
            Class<?> contextClass = Class.forName("reactor.util.context.Context", false, cl);
            Class<?> contextViewClass = Class.forName("reactor.util.context.ContextView", false, cl);
            Object newContext = contextClass.getMethod("of", Object.class, Object.class)
                    .invoke(null, KEY, context);
            Method contextWrite = mono.getClass().getMethod("contextWrite", contextViewClass);
            return contextWrite.invoke(mono, newContext);
        } catch (Throwable t) {
            return mono;
        }
    }

    // `original` is a Mono<T>. Registers `url` once `original` is actually subscribed to, using
    // whatever ContextObject write() captured upstream in the same reactive chain (null if
    // none). Returns a Mono<T> equivalent to `original` (or `original` itself if anything here
    // fails - registration is best-effort, must never break the actual request).
    public static Object deferRegisterUrl(Object original, URL url) {
        try {
            ClassLoader cl = original.getClass().getClassLoader();
            Class<?> functionClass = Class.forName("java.util.function.Function", false, cl);
            Method deferContextual = original.getClass().getMethod("deferContextual", functionClass);
            InvocationHandler handler = new RegisterUrlHandler(original, url);
            Object proxy = Proxy.newProxyInstance(cl, new Class<?>[]{functionClass}, handler);
            return deferContextual.invoke(null, proxy);
        } catch (Throwable t) {
            return original;
        }
    }

    // Not a lambda: constructed from advice code that ByteBuddy inlines into the *target*
    // class's bytecode, so a lambda here would become a private synthetic method that the
    // target class can't call back into (IllegalAccessError). A plain named class implementing
    // InvocationHandler - whose own methods only ever see java.lang.Object - avoids that.
    private static final class RegisterUrlHandler implements InvocationHandler {
        private final Object original;
        private final URL url;

        RegisterUrlHandler(Object original, URL url) {
            this.original = original;
            this.url = url;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (!"apply".equals(method.getName()) || args == null || args.length == 0) {
                return original;
            }
            Object ctxView = args[0];
            ContextObject context = null;
            try {
                Method getOrDefault = ctxView.getClass().getMethod("getOrDefault", Object.class, Object.class);
                context = (ContextObject) getOrDefault.invoke(ctxView, KEY, null);
            } catch (Throwable ignored) {
            }
            URLCollector.report(url, context);
            return original;
        }
    }
}
