package dev.aikido.AikidoAgent.wrappers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aikido.AikidoAgent.context.ContextObject;
import dev.aikido.AikidoAgent.context.SpringContextObject;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;

import jakarta.servlet.http.HttpServletRequest;

public class SpringFrameworkWrapper extends Wrapper {
    public static AsmVisitorWrapper get() {
        return Advice.to(SpringFrameworkAdvice.class)
                .on(ElementMatchers.named("processRequest"));
    }

    private static class SpringFrameworkAdvice {
        @Advice.OnMethodEnter
        public static void intercept(@Advice.Argument(0) HttpServletRequest request) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            ContextObject contextObject = new SpringContextObject(request);

            String method = contextObject.getMethod();
            String url = contextObject.getUrl();
            System.out.printf("Url: %s with Method: %s \n", url, method);

            String json = gson.toJson(contextObject);
            System.out.println("Serialized JSON:");
            System.out.println(json);

        }
    }
}
