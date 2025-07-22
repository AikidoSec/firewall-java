package dev.aikido.agent.wrappers;
import dev.aikido.agent_bootstrap.AikidoBootstrapClass;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.is;

public class RuntimeExecWrapper implements Wrapper {
    public String getName() {
        // Wrap File constructor.
        // https://docs.oracle.com/javase/8/docs/api/java/io/File.html
        return CommandExecAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(Runtime.class)
                .and(ElementMatchers.nameContainsIgnoreCase("exec"));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return is(Runtime.class);
    }

    public static class CommandExecAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.Argument(0) Object argument
        ) throws Throwable {
            AikidoBootstrapClass.invoke(AikidoBootstrapClass.COMMAND_COLLECTOR_REPORT, argument);
        }
    }
}
