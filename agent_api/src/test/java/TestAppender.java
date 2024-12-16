import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.List;

public class TestAppender extends AbstractAppender {
    private final List<String> logMessages = new ArrayList<>();

    protected TestAppender(String name) {
        super(name, null, PatternLayout.createDefaultLayout(), true);
    }

    @Override
    public void append(LogEvent event) {
        logMessages.add(event.getMessage().getFormattedMessage());
    }

    public List<String> getLogMessages() {
        return logMessages;
    }

    public void clear() {
        logMessages.clear();
    }
}
