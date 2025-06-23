package dev.aikido.agent;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.VisibilityBridgeStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.matcher.ElementMatchers;

public final class ByteBuddyInitializer {
    private static final Logger logger = LogManager.getLogger(ByteBuddyInitializer.class);
    public static AgentBuilder createAgentBuilder() {
        // byte buddy debug mode should be linked to the trace logs, since we rarely want to inspect them.
        boolean debugMode = logger.logsTraceLogs();
        return createAgentBuilder(debugMode);
    }

    public static AgentBuilder createAgentBuilder(boolean debugMode) {
        logger.debug("Creating new ByteBuddy agent, with debugMode: %s", debugMode);
        AgentBuilder agentBuilder = new AgentBuilder.Default(
                // default method graph compiler inspects the class hierarchy, we don't need it, so
                // we use a simpler and faster strategy instead
                new ByteBuddy()
                        .with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE)
                        .with(VisibilityBridgeStrategy.Default.NEVER)
                        .with(InstrumentedType.Factory.Default.FROZEN)
        );

        //  Disables all implicit changes on a class file that Byte Buddy would apply for certain instrumentation's.
        agentBuilder = agentBuilder.disableClassFormatChanges();

        // Disabling this would impair our ability to wrap Java's own classes :
        agentBuilder = agentBuilder.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        if (debugMode) {
                agentBuilder = agentBuilder
                    .with(AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly())
                    .with(AgentBuilder.InstallationListener.StreamWriting.toSystemError());
        }

        // Ignore Byte Buddy and Aikido's internal code:
        agentBuilder = agentBuilder.ignore(
                ElementMatchers.nameContains("bytebuddy")
                .or(ElementMatchers.nameContains("dev.aikido.agent"))
        );

        agentBuilder = agentBuilder.with(AgentBuilder.TypeStrategy.Default.DECORATE);

        logger.trace("Finished creating new ByteBuddy agent: %s", agentBuilder);
        return agentBuilder;
    }
}
