package dev.aikido.agent;

import dev.aikido.agent.wrappers.*;
import dev.aikido.agent_api.Config;
import dev.aikido.agent_api.helpers.env.BlockingEnv;
import dev.aikido.agent_api.helpers.env.BooleanEnv;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.storage.ServiceConfigStore;
import dev.aikido.agent_api.storage.RuntimePackagesStore;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

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
        int javaVersion = Runtime.version().feature();
        if (javaVersion < 17) {
            logger.error("Zen by Aikido requires Java 17 or newer. Current version: %d. The agent will not be loaded.", javaVersion);
            return;
        }
        if (javaVersion > 25) {
            logger.error("Zen by Aikido does not support Java %d (max supported version: 25). The agent will not be loaded.", javaVersion);
            return;
        }
        logger.info("Zen by Aikido v%s starting.", Config.pkgVersion);
        setAikidoSysProperties();
        installPackageObserver(inst);

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

        // We want to load in the blocking value here, it's important that this happens regardless of the token.
        ServiceConfigStore.updateBlocking(new BlockingEnv().getValue());

        startDaemon(agentArgs);
    }

    private static void installPackageObserver(Instrumentation inst) {
        inst.addTransformer(new PackageObserver(), false);
        for (Class<?> loadedClass : inst.getAllLoadedClasses()) {
            observeLoadedClass(loadedClass);
        }
    }

    private static void observeLoadedClass(Class<?> loadedClass) {
        String className = loadedClass.getName().replace('.', '/');
        if (isAikidoClass(className)) {
            return;
        }
        try {
            RuntimePackagesStore.observeClass(className, loadedClass.getClassLoader(), loadedClass.getProtectionDomain());
        } catch (Throwable ignored) {
        }
    }

    private static boolean isAikidoClass(String className) {
        return className == null || className.startsWith("dev/aikido/");
    }

    private static final class PackageObserver implements ClassFileTransformer {
        @Override
        public byte[] transform(
                ClassLoader loader,
                String className,
                Class<?> classBeingRedefined,
                ProtectionDomain protectionDomain,
                byte[] classfileBuffer
        ) {
            if (!isAikidoClass(className)) {
                RuntimePackagesStore.observeClass(className, loader, protectionDomain);
            }
            return null;
        }
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
