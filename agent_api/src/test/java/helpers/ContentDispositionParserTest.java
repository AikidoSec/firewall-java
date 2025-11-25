package helpers;

import dev.aikido.agent_api.helpers.ContentDispositionParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContentDispositionParserTest {

    @Test
    void testExtractFilenameFromHeader_NullInput() {
        assertNull(ContentDispositionParser.extractFilenameFromHeader(null));
    }

    @Test
    void testExtractFilenameFromHeader_EmptyInput() {
        assertNull(ContentDispositionParser.extractFilenameFromHeader(""));
    }

    @Test
    void testExtractFilenameFromHeader_StandardQuotedFilename() {
        String header = "attachment; filename=\"example.txt\"";
        assertEquals("example.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_StandardUnquotedFilename() {
        String header = "attachment; filename=example.txt";
        assertEquals("example.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_FilenameWithSpaces() {
        String header = "attachment; filename=\"my file.txt\"";
        assertEquals("my file.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_FilenameWithSpecialChars() {
        String header = "attachment; filename=\"my-file_123.txt\"";
        assertEquals("my-file_123.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_FilenameWithPath() {
        String header = "attachment; filename=\"/path/to/example.txt\"";
        assertEquals("/path/to/example.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_MultipleAttributes() {
        String header = "attachment; filename=\"example.txt\"; size=1024";
        assertEquals("example.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_MalformedHeader() {
        String header = "attachment; filename=; size=1024";
        assertNull(ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_NoFilenameAttribute() {
        String header = "attachment; size=1024";
        assertNull(ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_OnlyFilenameAttribute() {
        String header = "filename=\"example.txt\"";
        assertEquals("example.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_UnquotedFilenameWithSemicolon() {
        String header = "attachment; filename=example.txt; size=1024";
        assertEquals("example.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_QuotedFilenameWithSemicolon() {
        String header = "attachment; filename=\"example.txt\"; size=1024";
        assertEquals("example.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_QuotedFilenameWithEscapedQuotes() {
        String header = "attachment; filename=\"example\\\"quoted\\\"file.txt\"";
        assertEquals("example\\\"quoted\\\"file.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_UnquotedFilenameWithSpaces() {
        String header = "attachment; filename=my file.txt";
        assertEquals("my", ContentDispositionParser.extractFilenameFromHeader(header));
    }

    @Test
    void testExtractFilenameFromHeader_UnquotedFilenameWithEquals() {
        String header = "attachment; filename=my=file.txt";
        assertEquals("my=file.txt", ContentDispositionParser.extractFilenameFromHeader(header));
    }
}
