package dev.aikido.agent;

import dev.aikido.agent.wrappers.jdbc.MysqlCJWrapper;
import dev.aikido.agent.wrappers.jdbc.PostgresWrapper;
import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent.wrappers.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
                ElementMatchers.nameContainsIgnoreCase("org.postgresql.jdbc.PgConnection")
                .or(ElementMatchers.nameContainsIgnoreCase("org.springframework.web.filter.RequestContextFilter"))
                .or(ElementMatchers.nameContainsIgnoreCase("org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.io.File"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.net.HttpURLConnection"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.net.InetAddress"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.lang"))
                .or(ElementMatchers.nameContainsIgnoreCase("com.mysql.cj.jdbc.ConnectionImp"))

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
            new SpringFrameworkBodyWrapper(),
            new FileWrapper(),
            new HttpURLConnectionWrapper(),
            new InetAddressWrapper(),
            new RuntimeExecWrapper(),
            new MysqlCJWrapper()
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