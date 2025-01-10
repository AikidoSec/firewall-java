package dev.aikido.agent;

import dev.aikido.agent.helpers.Logger;
import dev.aikido.agent.wrappers.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

import static dev.aikido.agent.ByteBuddyInitializer.createAgentBuilder;
import static dev.aikido.agent.DaemonStarter.startDaemon;
import static dev.aikido.agent.Wrappers.WRAPPERS;

public class Agent {
    private static final Logger logger = Logger.getLogger();
    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("Aikido Java Agent loaded.");
        setAikidoSysProperties();
        try {
            inst.appendToBootstrapClassLoaderSearch(
                    new JarFile(new File(System.getProperty("AIK_agent_dir") + "/agent_api.jar"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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