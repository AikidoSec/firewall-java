package dev.aikido.agent_api.helpers.logging;

import dev.aikido.agent_api.helpers.env.BooleanEnv;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Logger {
    private LogLevel logLevel;
    private final Class<?> logClass;
    private static final int MAX_ARGUMENT_LENGTH = 300;
    private static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.INFO;

    public Logger(Class<?> logClass) {
        this.logLevel = DEFAULT_LOG_LEVEL;
        this.logClass = logClass;

        // We first check "AIKIDO_LOG_LEVEL", because "AIKIDO_DEBUG" takes precedent.
        String logLevelString = System.getenv("AIKIDO_LOG_LEVEL");
        if (logLevelString != null) {
            try {
                this.logLevel = LogLevel.valueOf(logLevelString.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                this.error("Unknown log level `%s`", logLevelString);
            }
        }
        // "AIKIDO_DEBUG"
        BooleanEnv aikidoDebug = new BooleanEnv("AIKIDO_DEBUG", false);
        if (aikidoDebug.getValue()) {
            this.logLevel = LogLevel.TRACE;
        }
    }
    public Logger(Class<?> logClass, LogLevel logLevel) {
        this.logLevel = logLevel;
        this.logClass = logClass;
    }

    public void log(LogLevel level, Object message, Object... args) {
        try {
            if (level.getLevel() >= logLevel.getLevel()) {
                // Get time stamp :
                LocalDateTime timestamp = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTimestamp = timestamp.format(formatter);

                // Get name of the current thread :
                String threadName = Thread.currentThread().getName();

                // Create prefix :
                String prefix = "%s [%s] %s %s: ".formatted(formattedTimestamp, threadName, level, logClass.getName());

                // Print message :
                if (message instanceof String messageStr) {
                    List<Object> parsedArgs = parseArguments(args);
                    System.out.println(prefix + String.format(messageStr, parsedArgs.toArray()));
                } else {
                    System.out.println(prefix + message);
                }
            }
        }
        catch (Throwable e) {
            System.err.println("[AIKIDO LOGGER FAILURE] " + e.getMessage());
        }
    }
    public List<Object> parseArguments(Object[] args) {
        List<Object> parsedArgs = new ArrayList<>();
        for (Object arg: args) {
            if(arg instanceof Object[] argArray) {
                parsedArgs.add(Arrays.toString(argArray));
            } else if (arg instanceof Collection<?> collectionArg) {
                String str = "[";
                for (Object el : collectionArg) {
                    str += el + ",";
                }
                char[] stringChars = str.toCharArray();
                stringChars[str.length()-1] = ']'; // Replace last , with ']'
                parsedArgs.add(String.valueOf(stringChars));
            } else {
                parsedArgs.add(arg);
            }
        }
        for(int i = 0; i < parsedArgs.size(); i++) {
            if (parsedArgs.get(i) instanceof String argString) {
                // replace newline and carriage return to avoid log injection :
                argString = argString.replaceAll("[\\r\\n]+", "");
                if (argString.length() > MAX_ARGUMENT_LENGTH) {
                    argString = argString.substring(0, MAX_ARGUMENT_LENGTH);
                }
                parsedArgs.set(i, argString);
            }
        }
        return parsedArgs;
    }

    public void trace(Object message, Object... args) {
        log(LogLevel.TRACE, message, args);
    }
    public void debug(Object message, Object... args) {
        log(LogLevel.DEBUG, message, args);
    }
    public void info(Object message, Object... args) {
        log(LogLevel.INFO, message, args);
    }
    public void warn(Object message, Object... args) {
        log(LogLevel.WARN, message, args);
    }
    public void error(Object message, Object... args) {
        log(LogLevel.ERROR, message, args);
    }
    public void fatal(Object message, Object... args) {
        log(LogLevel.FATAL, message, args);
    }
    public boolean logsTraceLogs() {
        return LogLevel.TRACE.getLevel() >= logLevel.getLevel();
    }
}
