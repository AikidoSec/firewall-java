package dev.aikido.agent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.VisibilityBridgeStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.matcher.ElementMatchers;

public final class ByteBuddyInitializer {
    public static AgentBuilder createAgentBuilder() {
        return new AgentBuilder.Default(
                // default method graph compiler inspects the class hierarchy, we don't need it, so
                // we use a simpler and faster strategy instead
                new ByteBuddy()
                        .with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE)
                        .with(VisibilityBridgeStrategy.Default.NEVER)
                        .with(InstrumentedType.Factory.Default.FROZEN)
        )
                //  Disables all implicit changes on a class file that Byte Buddy would apply for certain instrumentation's.
                .disableClassFormatChanges()

                // Disabling this would impair our ability to wrap Java's own classes :
                // Issues with this retransformation for JaCoCo investigate :
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)

                .with(AgentBuilder.DescriptionStrategy.Default.POOL_ONLY)

                .ignore(ElementMatchers.none())

                .with(AgentBuilder.TypeStrategy.Default.DECORATE);
    }
}
