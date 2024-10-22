package dev.aikido.AikidoAgent;

import dev.aikido.AikidoAgent.background.BackgroundProcess;
import dev.aikido.AikidoAgent.helpers.env.Token;
import dev.aikido.AikidoAgent.wrappers.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.LatentMatcher;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);
    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("Aikido Java Agent loaded.");
        // Bytecode instrumentation :
        new AgentBuilder.Default()
            //  Disables all implicit changes on a class file that Byte Buddy would apply for certain instrumentation's.
            .disableClassFormatChanges()
            // Applies a retransformation to all classes that are already loaded and that would have been transformed if the
            // built agent was registered before they were loaded.
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .ignore(ElementMatchers.none())
            .type(
                ElementMatchers.nameContainsIgnoreCase("org.postgresql.core")
                .or(ElementMatchers.nameContainsIgnoreCase("org.springframework.web.servlet"))
            )
            .transform(AikidoTransformer.get())
            .with(AgentBuilder.TypeStrategy.Default.DECORATE)
            .installOn(inst);

        // Background process :
        BackgroundProcess backgroundProcess = new BackgroundProcess("main-background-process", Token.fromEnv());
        backgroundProcess.setDaemon(true);
        backgroundProcess.start();
    }
    private static final List<Wrapper> wrappers = Arrays.asList(
            new PostgresWrapper(),
            new SpringFrameworkWrapper(),
            new SpringFrameworkBodyWrapper()
    );
    private static class AikidoTransformer {
        public static AgentBuilder.Transformer get() {
            var adviceAgentBuilder = new AgentBuilder.Transformer.ForAdvice()
                    .include(Agent.class.getClassLoader());
            for(Wrapper wrapper: wrappers) {
                // Add wrapper as advice :
                adviceAgentBuilder = adviceAgentBuilder.advice(wrapper.getMatcher(), wrapper.getName());
            }
            return adviceAgentBuilder;
        }
    }
}