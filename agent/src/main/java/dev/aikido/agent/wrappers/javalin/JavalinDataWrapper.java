package dev.aikido.agent.wrappers.javalin;

import dev.aikido.agent.wrappers.Wrapper;
import dev.aikido.agent_api.context.Context;
import dev.aikido.agent_api.context.JavalinContextObject;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Executable;

import static net.bytebuddy.matcher.ElementMatchers.*;

/** JavalinDataWrapper
 * Wraps multiple functions for both body, form & path variable data.
 * https://github.com/javalin/javalin/blob/8b1dc1a55c28618df7f9f044aad4949d30a8cca8/javalin/src/main/java/io/javalin/http/Context.kt#L158
 */
public class JavalinDataWrapper implements Wrapper {
    @Override
    public String getName() {
        return JavalinDataAdvice.class.getName();
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMatcher() {
        return isDeclaredBy(getTypeMatcher()).and(namedOneOf(
                // parse-able bodies :
                "bodyAsClass",
                // Form parameters :
                "formParam", "formParamAsClass", "formParams", "formParamMap",
                // Path parameters :
                "pathParamMap", "pathParam", "pathParamAsClass"
        ));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return hasSuperType(nameContains("io.javalin.http.Context"));
    }

    public class JavalinDataAdvice {
        @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class)
        public static void after(
                @Advice.This io.javalin.http.Context ctx,
                @Advice.Return Object data,
                @Advice.Origin Executable method
        ) {
            String methodName = method.getName();
            if (Context.get() instanceof JavalinContextObject context) {
                if(methodName == "bodyAsClass") {
                    // Body data via bodyAsClass, this overrides everything and is our preferred object :
                    context.setBody(data);
                }

                if (methodName == "formParamMap") {
                    // Form data, this can only override body if body does not yet exist :
                    if (context.getBody() == null) {
                        context.setBody(data);
                    }
                } else if(methodName.startsWith("form")) {
                    // form functions that might not have used formParamMap, so we execute it here :
                    ctx.formParamMap(); // This will fall through to the if-clause above.
                }

                if (methodName == "pathParamMap") {
                    // We will now store the path parameters :
                    context.setParams(data);
                } else if(methodName.startsWith("path")) {
                    // path functions that might not have used pathParamMap, so we execute pathParamMap here :
                    ctx.pathParamMap(); // This will fall through to the if-clause above.
                }

                // Store changes :
                Context.set(context);
            }
        }
    }
}
