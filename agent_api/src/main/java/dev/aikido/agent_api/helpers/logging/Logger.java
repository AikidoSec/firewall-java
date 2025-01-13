package dev.aikido.agent_api.helpers.logging;

    import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Logger {
    private final LogLevel logLevel;
    private final Class<?> logClass;

    public Logger(Class<?> logClass) {
        String logLevelString = System.getenv("AIKIDO_LOG_LEVEL");
        if (logLevelString != null) {
            this.logLevel = LogLevel.valueOf(logLevelString.toUpperCase());
        } else {
            this.logLevel = LogLevel.INFO; // Default loglevel
        }
        this.logClass = logClass;
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
            e.printStackTrace();
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
}