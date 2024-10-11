package dev.aikido.AikidoAgent.wrappers;

import dev.aikido.AikidoAgent.Agent;
import dev.aikido.AikidoAgent.collectors.SQLCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;

public class PostgresWrapper {

    public static AsmVisitorWrapper get() {
        return Advice.to(PostgresAdvice.class)
                .on(ElementMatchers.isConstructor());
    }

    private static class PostgresAdvice {
        @Advice.OnMethodEnter
        public static void intercept(@Advice.Origin Method method) {
            String pkgName = method.getDeclaringClass().getPackageName();
            String methodName = method.getName();
            String className = method.getDeclaringClass().getSimpleName();
            System.out.println("Package : " + pkgName + " Method "+ methodName + " of class " + className + " called.");
            SQLCollector.report("test", "postgresql");
        }
    }
}
