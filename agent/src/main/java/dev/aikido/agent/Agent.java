package dev.aikido.agent;

import dev.aikido.agent.wrappers.jdbc.MSSQLWrapper;
import dev.aikido.agent.wrappers.jdbc.MariaDBWrapper;
import dev.aikido.agent.wrappers.jdbc.MysqlCJWrapper;
import dev.aikido.agent.wrappers.jdbc.PostgresWrapper;
import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent.wrappers.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.aikido.agent.ByteBuddyInitializer.createAgentBuilder;
import static dev.aikido.agent.DaemonStarter.startDaemon;
import static dev.aikido.agent.Wrappers.WRAPPERS;
import static dev.aikido.agent.helpers.AgentArgumentParser.parseAgentArgs;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);
    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("Aikido Java Agent loaded.");
        setAikidoSysProperties();

        ElementMatcher.Junction wrapperTypeDescriptors = ElementMatchers.none();
        for(Wrapper wrapper: WRAPPERS) {
            wrapperTypeDescriptors = wrapperTypeDescriptors.or(wrapper.getTypeMatcher());
        }

        // Bytecode instrumentation :
        createAgentBuilder()
            .type(wrapperTypeDescriptors)
            .transform(AikidoTransformer.get())
            .installOn(inst);

        logger.info("Instrumentation installed.");
        
        startDaemon(agentArgs);
    }
    private static class AikidoTransformer {
        public static AgentBuilder.Transformer get() {
            var adviceAgentBuilder = new AgentBuilder.Transformer.ForAdvice()
                    .include(Agent.class.getClassLoader());
            for(Wrapper wrapper: WRAPPERS) {
                // Add wrapper as advice :
                adviceAgentBuilder = adviceAgentBuilder.advice(wrapper.getMatcher(), wrapper.getName());
            }
            return adviceAgentBuilder;
        }
    }
    private static void setAikidoSysProperties() {
        String pathToAgentJar = Agent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String pathToAikidoDirectory = new File(pathToAgentJar).getParent();
        String jarPath = "file:" + pathToAikidoDirectory + "/agent_api.jar";
        System.setProperty("AIK_agent_dir", pathToAikidoDirectory);
        System.setProperty("AIK_agent_api_jar", jarPath);
    }
}