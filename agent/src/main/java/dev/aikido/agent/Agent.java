package dev.aikido.agent;

import dev.aikido.agent.wrappers.jdbc.MSSQLWrapper;
import dev.aikido.agent.wrappers.jdbc.MariaDBWrapper;
import dev.aikido.agent.wrappers.jdbc.MysqlCJWrapper;
import dev.aikido.agent.wrappers.jdbc.PostgresWrapper;
import dev.aikido.agent_api.background.BackgroundProcess;
import dev.aikido.agent_api.helpers.env.Token;
import dev.aikido.agent.wrappers.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Agent {
    private static final Logger logger = LogManager.getLogger(Agent.class);
    public static void premain(String agentArgs, Instrumentation inst) {
        logger.info("Aikido Java Agent loaded.");
        setAikidoSysProperties();
        // Bytecode instrumentation :
        new AgentBuilder.Default()
            //  Disables all implicit changes on a class file that Byte Buddy would apply for certain instrumentation's.
            .disableClassFormatChanges()
            // After careful consideration we decided not to retransform pre-existing classes, most are either from other agents or from the JDK
            // Using retransformation causes compatibility issues and since our agent is not dynamically loaded, is unnecessary.
            .with(AgentBuilder.RedefinitionStrategy.DISABLED)
            .ignore(ElementMatchers.none())
            .type(
                ElementMatchers.nameContainsIgnoreCase("org.postgresql.jdbc.PgConnection")
                .or(ElementMatchers.nameContainsIgnoreCase("org.springframework.web.filter.RequestContextFilter"))
                .or(ElementMatchers.nameContainsIgnoreCase("org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver"))
                .or(ElementMatchers.nameContainsIgnoreCase("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.io.File"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.net.HttpURLConnection"))
                .or(ElementMatchers.nameContainsIgnoreCase("sun.net.www.protocol.http.HttpURLConnection"))
                .or(ElementMatchers.nameContainsIgnoreCase("jdk.internal.net.http.HttpRequestImpl"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.net.InetAddress"))
                .or(ElementMatchers.nameContainsIgnoreCase("java.lang"))
                .or(ElementMatchers.nameContainsIgnoreCase("com.mysql.cj.jdbc.ConnectionImp"))
                .or(ElementMatchers.nameContainsIgnoreCase("com.microsoft.sqlserver.jdbc.SQLServerConnection"))
                .or(ElementMatchers.nameContainsIgnoreCase("org.mariadb.jdbc.Connection"))
            )
            .transform(AikidoTransformer.get())
            .with(AgentBuilder.TypeStrategy.Default.DECORATE)
            .installOn(inst);
        logger.info("Instrumentation installed.");
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
            new SpringFrameworkInvokeWrapper(),
            new MysqlCJWrapper(),
            new MSSQLWrapper(),
            new MariaDBWrapper(),
            new HttpClientWrapper(),
            new HttpConnectionRedirectWrapper()
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
    private static void setAikidoSysProperties() {
        String pathToAgentJar = Agent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String pathToAikidoDirectory = new File(pathToAgentJar).getParent();
        String jarPath = "file:" + pathToAikidoDirectory + "/agent_api.jar";
        System.setProperty("AIK_agent_dir", pathToAikidoDirectory);
        System.setProperty("AIK_agent_api_jar", jarPath);
    }
}