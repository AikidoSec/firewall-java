package api_discovery;

import com.google.gson.Gson;
import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.api_discovery.DataSchemaItem;
import dev.aikido.agent_api.api_discovery.DataSchemaType;
import dev.aikido.agent_api.api_discovery.GetApiInfo;
import dev.aikido.agent_api.context.ContextObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GetApiInfoTest {

    private ContextObject context;

    @BeforeEach
    public void setUp() {
        context = Mockito.mock(ContextObject.class);
    }

    @Test
    public void testGetApiInfoWithFormEncodedContext() {
        Map<String, Object> body = new HashMap<>();
        body.put("data1", Map.of(
                "data2", List.of(Map.of("Help", true), Map.of("Help", true, "location", "Sea")),
                "identifier", "hsfkjewhfwehgkjwehgkj",
                "active", true
        ));
        body.put("user", Map.of(
                "name", "John Doe",
                "email", "john.doe@example.com"
        ));

        Mockito.when(context.getMethod()).thenReturn("GET");
        Mockito.when(context.getUrl()).thenReturn("/api/resource1");
        Mockito.when(context.getBody()).thenReturn(body);
        HashMap<String, String> headers = new HashMap<>(Map.of("content-type", "application/x-www-form-urlencoded"));
        Mockito.when(context.getHeaders()).thenReturn(headers);
        APISpec apiInfo = GetApiInfo.getApiInfo(context);
        assertNotNull(apiInfo);
        assertNotNull(apiInfo.body());
        Gson gson = new Gson();
        assertEquals(
                "{\"type\":\"object\",\"properties\":{\"data1\":{\"type\":\"object\",\"properties\":{\"identifier\":{\"type\":\"string\",\"optional\":false},\"data2\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"location\":{\"type\":\"string\",\"optional\":true},\"Help\":{\"type\":\"boolean\",\"optional\":false}},\"optional\":false},\"optional\":false},\"active\":{\"type\":\"boolean\",\"optional\":false}},\"optional\":false},\"user\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\",\"optional\":false},\"email\":{\"type\":\"string\",\"optional\":false}},\"optional\":false}},\"optional\":false}",
                gson.toJson(apiInfo.body().get("schema")));
        assertEquals("form-urlencoded", apiInfo.body().get("type"));
    }

    @Test
    public void testGetApiInfoWithJson() {
        Map<String, Object> body = new HashMap<>();
        body.put("user", Map.of(
                "name", "John Doe",
                "email", "john.doe@example.com"
        ));

        HashMap<String, String[]> query = new HashMap<>();
        query.put("user2", List.of("a", "b").toArray(new String[0]));

        Mockito.when(context.getMethod()).thenReturn("GET");
        Mockito.when(context.getUrl()).thenReturn("/api/resource1");
        Mockito.when(context.getBody()).thenReturn(body);
        Mockito.when(context.getQuery()).thenReturn(query);
        Mockito.when(context.getHeaders()).thenReturn(new HashMap<>(Map.of("content-type", "application/json")));

        APISpec apiInfo = GetApiInfo.getApiInfo(context);
        assertNotNull(apiInfo);
        assertNotNull(apiInfo.body());
        assertNotNull(apiInfo.query());
        Gson gson = new Gson();
        assertEquals("json", apiInfo.body().get("type"));
        assertEquals("{\"type\":\"object\",\"properties\":{\"user\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\",\"optional\":false},\"email\":{\"type\":\"string\",\"optional\":false}},\"optional\":false}},\"optional\":false}", gson.toJson(apiInfo.body().get("schema")));
        assertEquals("{\"type\":\"object\",\"properties\":{\"user2\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"optional\":false},\"optional\":false}},\"optional\":false}", gson.toJson(apiInfo.query()));
        assertNull(apiInfo.auth());
    }

}