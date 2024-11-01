package dev.aikido.agent.wrappers;
import dev.aikido.agent_api.collectors.FileCollector;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.reflect.Executable;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

public class FileInputStreamWrapper implements Wrapper {
    public String getName() {
        // Wrap FileInputStream constructor.
        // https://github.com/pgjdbc/pgjdbc/blob/fcc13e70e6b6bb64b848df4b4ba6b3566b5e95a3/pgjdbc/src/main/java/org/postgresql/core/NativeQuery.java#L34C10-L34C21
        return FileInputStreamAdvice.class.getName();
    }
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return ElementMatchers.isDeclaredBy(ElementMatchers.named("java.io.File")).and(ElementMatchers.isConstructor());
    }
    public static class FileInputStreamAdvice {
        @Advice.OnMethodEnter
        public static void before(
                @Advice.This(typing = DYNAMIC, optional = true) Object target,
                @Advice.Origin Executable method,
                @Advice.AllArguments(readOnly = false, typing = DYNAMIC) Object[] args
        ) {
            try {
                FileCollector.report("Hello!");
                System.out.println(args[0]);

                System.out.println("[C] >> " + method);
            } catch(Throwable e) {
                System.out.println(e);
            }
        }

        @Advice.OnMethodExit
        public static void after(
                @Advice.This(typing = DYNAMIC, optional = true) Object target,
                @Advice.Origin Executable method,
                @Advice.AllArguments(readOnly = false, typing = DYNAMIC) Object[] args
        ) {
            System.out.println("[C] << " + method);
        }
    }
}
