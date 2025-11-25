package helpers;

import dev.aikido.agent_api.helpers.ContentDispositionHeader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContentDispositionHeaderTest {

    @Test
    public void testParseRequiresString() {
        assertThrows(IllegalArgumentException.class, () -> ContentDispositionHeader.parse(null),
            "argument string is required");
    }

    @Test
    public void testParseRejectsQuotedValue() {
        assertThrows(IllegalArgumentException.class, () -> ContentDispositionHeader.parse("\"attachment\""),
            "invalid type format");
    }

    @Test
    public void testThrowsIfUnknownCharset() {
        assertThrows(IllegalArgumentException.class, () -> ContentDispositionHeader.parse("attachment; filename*=UTF-7''file%20name.jpg"),
            "unsupported charset in extended field");
    }

    @Test
    public void testThrowsIfFirstRegex() {
        assertThrows(IllegalArgumentException.class, () -> ContentDispositionHeader.parse("}"),
            "invalid extended field value");
    }

    @Test
    public void testParseAttachment() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment");
        assertEquals("attachment", result.type());
        assertTrue(result.params().isEmpty());
    }

    @Test
    public void testParseInline() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("inline");
        assertEquals("inline", result.type());
        assertTrue(result.params().isEmpty());
    }

    @Test
    public void testParseFormData() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("form-data");
        assertEquals("form-data", result.type());
        assertTrue(result.params().isEmpty());
    }

    @Test
    public void testParseWithTrailingLWS() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment \t ");
        assertEquals("attachment", result.type());
        assertTrue(result.params().isEmpty());
    }

    @Test
    public void testParseNormalizeToLowerCase() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("ATTACHMENT");
        assertEquals("attachment", result.type());
        assertTrue(result.params().isEmpty());
    }

    @Test
    public void testParseQuotedParameterValue() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename=\"plans.pdf\"");
        assertEquals("attachment", result.type());
        assertEquals("plans.pdf", result.params().get("filename"));
    }

    @Test
    public void testParseUnescapeQuotedValue() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename=\"the \\\"plans\\\".pdf\"");
        assertEquals("attachment", result.type());
        assertEquals("the \"plans\".pdf", result.params().get("filename"));
    }

    @Test
    public void testParseIncludeAllParameters() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename=\"plans.pdf\"; foo=bar");
        assertEquals("attachment", result.type());
        assertEquals("plans.pdf", result.params().get("filename"));
        assertEquals("bar", result.params().get("foo"));
    }

    @Test
    public void testParseParametersWithAnyLWS() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment;filename=\"plans.pdf\" \t;    \t\t foo=bar");
        assertEquals("attachment", result.type());
        assertEquals("plans.pdf", result.params().get("filename"));
        assertEquals("bar", result.params().get("foo"));
    }

    @Test
    public void testParseTokenFilename() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename=plans.pdf");
        assertEquals("attachment", result.type());
        assertEquals("plans.pdf", result.params().get("filename"));
    }

    @Test
    public void testParseISO88591Filename() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename=\"£ rates.pdf\"");
        assertEquals("attachment", result.type());
        assertEquals("£ rates.pdf", result.params().get("filename"));
    }

    @Test
    public void testParseUTF8ExtendedParameterValue() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename*=UTF-8\'\'%E2%82%AC%20rates.pdf");
        assertEquals("attachment", result.type());
        assertEquals("€ rates.pdf", result.params().get("filename"));
    }

    @Test
    public void testParseUTF8ExtendedParameterValueCaseInsensitive() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename*=utf-8\'\'%E2%82%AC%20rates.pdf");
        assertEquals("attachment", result.type());
        assertEquals("€ rates.pdf", result.params().get("filename"));
    }

    @Test
    public void testParseISO88591ExtendedParameterValue() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename*=ISO-8859-1\'\'%A3%20rates.pdf");
        assertEquals("attachment", result.type());
        assertEquals("£ rates.pdf", result.params().get("filename"));
    }

    @Test
    public void testParseWithEmbeddedLanguage() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename*=UTF-8\'en\'%E2%82%AC%20rates.pdf");
        assertEquals("attachment", result.type());
        assertEquals("€ rates.pdf", result.params().get("filename"));
    }

    @Test
    public void testPreferExtendedParameterValue() {
        ContentDispositionHeader.ParseResult result = ContentDispositionHeader.parse("attachment; filename=\"EURO rates.pdf\"; filename*=UTF-8\'\'%E2%82%AC%20rates.pdf");
        assertEquals("attachment", result.type());
        assertEquals("€ rates.pdf", result.params().get("filename"));
    }
}
