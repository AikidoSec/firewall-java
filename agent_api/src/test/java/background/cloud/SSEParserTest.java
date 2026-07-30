package background.cloud;

import dev.aikido.agent_api.background.cloud.SSEParser;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SSEParserTest {
    private SSEParser parserFor(String raw) {
        return new SSEParser(new BufferedReader(new StringReader(raw)));
    }

    @Test
    public void testParsesEventAndData() throws IOException {
        SSEParser parser = parserFor("event: config-updated\ndata: {\"configUpdatedAt\":1}\n\n");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertTrue(event.isPresent());
        assertEquals("config-updated", event.get().event());
        assertEquals("{\"configUpdatedAt\":1}", event.get().data());
    }

    @Test
    public void testJoinsMultiLineData() throws IOException {
        SSEParser parser = parserFor("event: config-updated\ndata: line1\ndata: line2\n\n");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertTrue(event.isPresent());
        assertEquals("line1\nline2", event.get().data());
    }

    @Test
    public void testIgnoresCommentLines() throws IOException {
        SSEParser parser = parserFor(": ping\nevent: config-updated\ndata: hello\n\n");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertTrue(event.isPresent());
        assertEquals("hello", event.get().data());
    }

    @Test
    public void testPureCommentDoesNotDispatch() throws IOException {
        SSEParser parser = parserFor(": ping\n\nevent: config-updated\ndata: hello\n\n");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertTrue(event.isPresent());
        assertEquals("hello", event.get().data());
    }

    @Test
    public void testIncompleteEventAtStreamEndIsDiscarded() throws IOException {
        SSEParser parser = parserFor("event: config-updated\ndata: hello");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertTrue(event.isEmpty());
    }

    @Test
    public void testMultipleEventsInSequence() throws IOException {
        SSEParser parser = parserFor("data: first\n\ndata: second\n\n");

        Optional<SSEParser.Event> first = parser.nextEvent();
        Optional<SSEParser.Event> second = parser.nextEvent();
        Optional<SSEParser.Event> third = parser.nextEvent();

        assertEquals("first", first.get().data());
        assertEquals("second", second.get().data());
        assertTrue(third.isEmpty());
    }

    @Test
    public void testValueWithoutLeadingSpaceIsPreserved() throws IOException {
        SSEParser parser = parserFor("data:hello\n\n");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertEquals("hello", event.get().data());
    }

    @Test
    public void testUnknownFieldIsIgnored() throws IOException {
        SSEParser parser = parserFor("id: 42\ndata: hello\n\n");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertTrue(event.isPresent());
        assertEquals("hello", event.get().data());
    }

    @Test
    public void testEmptyStreamReturnsEmpty() throws IOException {
        SSEParser parser = parserFor("");

        Optional<SSEParser.Event> event = parser.nextEvent();

        assertTrue(event.isEmpty());
    }
}
