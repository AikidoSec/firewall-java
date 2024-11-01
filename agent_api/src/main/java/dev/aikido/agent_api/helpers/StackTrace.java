package dev.aikido.agent_api.helpers;

public class StackTrace {
    public static String getCurrentStackTrace() {
        StringBuilder stringBuilder = new StringBuilder();
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : trace) {
            if(ste.getModuleName() != null && ste.getModuleName().startsWith("dev.aikido")) {
                continue; // Ignore Aikido internal stacktrace
            }
            stringBuilder.append(stackTraceElementToString(ste));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString().strip();
    }
    private static String stackTraceElementToString(StackTraceElement element) {
        return "at " + element.getClassName() + "." + element.getMethodName() +
                "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
    }
}
