package dev.aikido.agent_api.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class EnhancedStackTrace {
    private final List<Item> enhancedStackTrace = new ArrayList<>();

    public EnhancedStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : trace) {
            if (shouldIgnore(ste)) {
                continue;
            }
            String filename = ste.getFileName();
            int lineNumber = ste.getLineNumber();
            String functionCall = ste.getClassName() + "$" + ste.getMethodName();
            boolean isNative = ste.isNativeMethod();

            enhancedStackTrace.add(new Item(filename, lineNumber, functionCall, isNative));
        }
    }

    private static boolean shouldIgnore(StackTraceElement el) {
        if (el.getClassName().startsWith("dev.aikido.agent")) {
            return true;
        }
        return el.getClassName().startsWith("dev.aikido.agent_api");
    }

    public List<Item> getEnhancedStackTrace() {
        return this.enhancedStackTrace;
    }

    public String getStackTrace() {
        return enhancedStackTrace.stream()
            .map(element -> String.format("at %s(%s:%d)", element.functionCall, element.filename, element.lineNumber))
            .collect(Collectors.joining("\n"))
            .strip();
    }

    public record Item(String filename, int lineNumber, String functionCall, boolean isNative) {
    }
}
