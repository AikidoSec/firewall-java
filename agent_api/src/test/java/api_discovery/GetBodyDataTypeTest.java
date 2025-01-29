package api_discovery;

import static dev.aikido.agent_api.api_discovery.GetBodyDataType.getBodyDataType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class GetBodyDataTypeTest {

    @Test
    public void testJsonContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        assertEquals("json", getBodyDataType(headers));
    }

    @Test
    public void testVndApiJsonContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/vnd.api+json");
        assertEquals("json", getBodyDataType(headers));
    }

    @Test
    public void testCspReportContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/csp-report");
        assertEquals("json", getBodyDataType(headers));
    }

    @Test
    public void testXJsonContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/x-json");
        assertEquals("json", getBodyDataType(headers));
    }

    @Test
    public void testFormUrlEncodedContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded");
        assertEquals("form-urlencoded", getBodyDataType(headers));
    }

    @Test
    public void testMultipartFormDataContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "multipart/form-data; boundary=---");
        assertEquals("form-data", getBodyDataType(headers));
    }

    @Test
    public void testXmlContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/xml");
        assertEquals("xml", getBodyDataType(headers));
    }

    @Test
    public void testEmptyContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "");
        assertNull(getBodyDataType(headers));
    }

    @Test
    public void testNullContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", null);
        assertNull(getBodyDataType(headers));
    }

    @Test
    public void testNullHeaders() {
        assertNull(getBodyDataType(null));
    }

    @Test
    public void testUnknownContentType() {
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "text/plain");
        assertNull(getBodyDataType(headers));
    }
}
