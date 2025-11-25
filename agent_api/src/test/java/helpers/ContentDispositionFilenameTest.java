package helpers;

import dev.aikido.agent_api.helpers.ContentDispositionFilename;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

class ContentDispositionFilenameTest {
    @Test
    void testExtract_NullInput() {
        Optional<String> result = ContentDispositionFilename.extract(null);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtract_EmptyInput() {
        Optional<String> result = ContentDispositionFilename.extract("");
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractFilenameFromHeader_StandardQuotedFilename() {
        String header = "attachment; filename=\"example.txt\"";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("example.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_StandardUnquotedFilename() {
        String header = "attachment; filename=example.txt";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("example.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_FilenameWithSpaces() {
        String header = "attachment; filename=\"my file.txt\"";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("my file.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_FilenameWithSpecialChars() {
        String header = "attachment; filename=\"my-file_123.txt\"";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("my-file_123.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_FilenameWithPath() {
        String header = "attachment; filename=\"/path/to/example.txt\"";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("/path/to/example.txt", result.get());
    }

    @Test
    void testExtract_MultipleAttributes() {
        String header = "attachment; filename=\"example.txt\"; size=1024";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("example.txt", result.get());
    }

    @Test
    void testExtract() {
        String header = "attachment; filename=; size=1024";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractFilenameFromHeader_NoFilenameAttribute() {
        String header = "attachment; size=1024";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertFalse(result.isPresent());
    }

    @Test
    void testExtractFilenameFromHeader_OnlyFilenameAttribute() {
        String header = "filename=\"example.txt\"";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("example.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_UnquotedFilenameWithSemicolon() {
        String header = "attachment; filename=example.txt; size=1024";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("example.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_QuotedFilenameWithSemicolon() {
        String header = "attachment; filename=\"example.txt\"; size=1024";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("example.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_QuotedFilenameWithEscapedQuotes() {
        String header = "attachment; filename=\"example\\\"quoted\\\"file.txt\"";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("example\\\"quoted\\\"file.txt", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_UnquotedFilenameWithSpaces() {
        String header = "attachment; filename=my file.txt";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("my", result.get());
    }

    @Test
    void testExtractFilenameFromHeader_UnquotedFilenameWithEquals() {
        String header = "attachment; filename=my=file.txt";
        Optional<String> result = ContentDispositionFilename.extract(header);
        assertTrue(result.isPresent());
        assertEquals("my=file.txt", result.get());
    }
}
