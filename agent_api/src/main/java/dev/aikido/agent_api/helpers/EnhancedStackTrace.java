package dev.aikido.agent_api.helpers;

import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class EnhancedStackTrace {
    private static final Logger logger = LogManager.getLogger(EnhancedStackTrace.class);
    private final List<Item> enhancedStackTrace = new ArrayList<>();

    public EnhancedStackTrace() {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : trace) {
            if (shouldIgnore(ste)) {
                continue;
            }
            String fileName = ste.getFileName();
            String filePath = tryGetFullPath(ste).orElse("");
            int lineNumber = ste.getLineNumber();
            String functionCall = ste.getClassName() + "$" + ste.getMethodName();
            boolean isNative = isFromJavaLibrary(ste);

            enhancedStackTrace.add(new Item(fileName, filePath, lineNumber, functionCall, isNative));
        }
    }

    private static boolean shouldIgnore(StackTraceElement el) {
        if (el.getClassName().startsWith("dev.aikido.agent")) {
            return true;
        }
        return el.getClassName().startsWith("dev.aikido.agent_api");
    }

    private static boolean isFromJavaLibrary(StackTraceElement el) {
        if (el.getClassName().startsWith("java.")) {
            return true;
        }
        return el.getClassName().startsWith("jdk.");
    }

    private static Optional<String> tryGetFullPath(StackTraceElement el) {
        if (el.getFileName() == null) {
            return Optional.empty();
        }
        String fileName = el.getFileName();
        try {
            String cn = el.getClassName().replace('.', '/') + ".class";
            URL rawPathUrl = EnhancedStackTrace.class.getClassLoader().getResource(cn);

            // make sure the URL exists and is a jar: url
            if (rawPathUrl == null || !rawPathUrl.getProtocol().equals("jar")) {
                return Optional.empty();
            }
            // e.g. /app/build/libs/my_build.jar!/com/example/app/components/Class.java
            String[] rawPathUrlSeparated = rawPathUrl.toString().split("!");
            if (rawPathUrlSeparated.length != 2) {
                return Optional.empty();
            }

            // Safely extract the file extension
            String fileExtension = "java"; // default = java
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex != -1 && lastDotIndex != 0) {
                fileExtension = fileName.substring(lastDotIndex + 1);
            }

            // Construct the full path
            String fullPath = rawPathUrlSeparated[1].replace(".class", "." + fileExtension);

            // Remove inner classes
            fullPath = fullPath.replaceAll("\\$[^.]*", "");

            // verify validity
            if (fullPath.endsWith(fileName)) {
                return Optional.of(fullPath);
            }
        } catch (Throwable ignored) {
        }
        return Optional.empty();
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

    public record Item(String filename, String filepath, int lineNumber, String functionCall, boolean isNative) {
    }
}
