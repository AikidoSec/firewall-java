package dev.aikido.AikidoAgent;

import dev.aikido.AikidoAgent.wrappers.PostgresWrapper;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Aikido Java Agent loaded.");
        new AgentBuilder.Default()
            .ignore(ElementMatchers.none())
            .type(
                ElementMatchers.nameContainsIgnoreCase("org.postgresql.core")
                .or(ElementMatchers.nameContainsIgnoreCase("springboot"))
            )
            .transform(new AikidoTransformer())
            .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
            .installOn(inst);
    }
    private static class AikidoTransformer implements AgentBuilder.Transformer {
        @Override
        public DynamicType.Builder<?> transform(
                DynamicType.Builder<?> builder,
                TypeDescription typeDescription,
                ClassLoader classLoader,
                JavaModule javaModule,
                ProtectionDomain protectionDomain) {
            // Builder type : https://javadoc.io/static/net.bytebuddy/byte-buddy/1.15.4/net/bytebuddy/dynamic/DynamicType.Builder.html
            System.out.println(typeDescription.toString());
            if (Objects.equals(typeDescription.toString(), "class org.postgresql.core.NativeQuery")) {
                System.out.println("NativeQuery");
                return builder
                    .visit(PostgresWrapper.get());
            }
            return builder;
        }
    }
}