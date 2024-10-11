package dev.aikido.AikidoAgent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Aikido Java Agent loaded.");
        new AgentBuilder.Default()
            .type(ElementMatchers.any())
            .transform(new AikidoTransformer())
            .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
            .installOn(inst);
    }
    private static class AikidoTransformer implements Transformer {
        @Override
        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
            return builder
                    .method(ElementMatchers.any())
                    .intercept(Advice.to(LoggingAdvice.class))
        }

        private static class LoggingAdvice {
            @Advice.OnMethodEnter
            public static void intercept(@Advice.AllArguments Object[] allArguments,
                                         @Advice.Origin Method method) {
                //Logger logger = LogManager.getLogger();
                String pkgName = method.getDeclaringClass().getPackageName();
                if(pkgName.startsWith("org.postgresql")) {
                    System.out.println("Package : " + pkgName);
                    System.out.println("Method "+ method.getName() + " of class " + method.getDeclaringClass().getSimpleName() + " called.");
                }

                /*
                for (Object argument : allArguments) {
                    logger.info("Method {}, parameter type {}, value={}",
                            method.getName(), argument.getClass().getSimpleName(),
                            argument.toString());
                }
                */
            }
        }
    }
}