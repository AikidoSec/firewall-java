package dev.aikido.agent;

import dev.aikido.agent.wrappers.*;
import dev.aikido.agent_api.Config;
import dev.aikido.agent_api.helpers.env.BooleanEnv;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

import static dev.aikido.agent.ByteBuddyInitializer.createAgentBuilder;
import static dev.aikido.agent.DaemonStarter.startDaemon;
import static dev.aikido.agent.Wrappers.WRAPPERS;
import static dev.aikido.agent_api.vulnerabilities.sql_injection.RustSQLInterface.loadLibrary;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        // Check for 'AIKIDO_DISABLE' :
        if (new BooleanEnv("AIKIDO_DISABLE", /*default value*/ false).getValue()) {
            return; // AIKIDO_DISABLE is true, so we will not be wrapping anything.
        }
        logger.info("Zen by Aikido v%s starting.", Config.pkgVersion);
        try {
            setAikidoSysProperties(inst);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        // Modify bootstrap class path (includes the core Java classes), so we can have some shared
        // code for core java classes when wrapping.
        try {
            inst.appendToBootstrapClassLoaderSearch(new JarFile(getPathToAikidoDirectory() + "/agent_bootstrap.jar"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        // Test loading of zen binaries :
        loadLibrary();


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
    private static String getPathToAikidoDirectory() {
        String pathToAgentJar = Agent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        return new File(pathToAgentJar).getParent();
    }
    private static void setAikidoSysProperties(Instrumentation inst) throws IOException {

        String jarPath = "file:" + getPathToAikidoDirectory() + "/agent_api.jar";
        System.setProperty("AIK_agent_dir", getPathToAikidoDirectory());
        System.setProperty("AIK_agent_api_jar", jarPath);
    }
}
