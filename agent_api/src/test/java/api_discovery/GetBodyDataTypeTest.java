package api_discovery;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static dev.aikido.agent_api.api_discovery.GetBodyDataType.getBodyDataType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GetBodyDataTypeTest {

    @Test
    public void testJsonContentType() {
        assertEquals("json", getBodyDataType("application/json"));
    }

    @Test
    public void testVndApiJsonContentType() {
        assertEquals("json", getBodyDataType("application/vnd.api+json"));
    }

    @Test
    public void testCspReportContentType() {
        assertEquals("json", getBodyDataType("application/csp-report"));
    }

    @Test
    public void testXJsonContentType() {
        assertEquals("json", getBodyDataType("application/x-json"));
    }

    @Test
    public void testFormUrlEncodedContentType() {
        assertEquals("form-urlencoded", getBodyDataType("application/x-www-form-urlencoded"));
    }

    @Test
    public void testMultipartFormDataContentType() {
        assertEquals("form-data", getBodyDataType("multipart/form-data; boundary=---"));
    }

    @Test
    public void testXmlContentType() {
        assertEquals("xml", getBodyDataType("application/xml"));
    }

    @Test
    public void testEmptyContentType() {
        assertNull(getBodyDataType(""));
    }

    @Test
    public void testNullContentType() {
        assertNull(getBodyDataType(null));
    }

    @Test
    public void testNullHeaders() {
        assertNull(getBodyDataType(null));
    }

    @Test
    public void testUnknownContentType() {
        assertNull(getBodyDataType("text/plain"));
    }
}
