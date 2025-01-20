package dev.aikido.agent;

import dev.aikido.agent.wrappers.*;
import dev.aikido.agent_api.helpers.env.BooleanEnv;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.instrument.Instrumentation;

import static dev.aikido.agent.ByteBuddyInitializer.createAgentBuilder;
import static dev.aikido.agent.DaemonStarter.startDaemon;
import static dev.aikido.agent.Wrappers.WRAPPERS;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);
    public static void premain(String agentArgs, Instrumentation inst) {
        // Check for 'AIKIDO_DISABLE' :
        if (new BooleanEnv("AIKIDO_DISABLE", /*default value*/ false).getValue()) {
            return; // AIKIDO_DISABLE is true, so we will not be wrapping anything.
        }
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