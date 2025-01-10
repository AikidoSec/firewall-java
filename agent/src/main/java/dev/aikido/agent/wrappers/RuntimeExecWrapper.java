package dev.aikido.agent.wrappers;
import dev.aikido.agent_api.collectors.CommandCollector;
import dev.aikido.agent_api.vulnerabilities.AikidoException;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Executable;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
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
        // Since we have to wrap a native Java Class stuff gets more complicated
        // The classpath is not the same anymore, and we can't import our modules directly.
        // To bypass this issue we load collectors from a .jar file.
        @Advice.OnMethodEnter
        public static void before(
                @Advice.This(typing = DYNAMIC, optional = true) Object target,
                @Advice.Origin Executable method,
                @Advice.Argument(0) Object argument
        ) throws Throwable {
            try {
                if (!(argument instanceof String)) {
                    return;
                }
                CommandCollector.report(argument);
            } catch (AikidoException e) {
                throw e;
            } catch(Throwable e) {}
        }
    }
}
