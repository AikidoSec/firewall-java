package api_discovery;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.api_discovery.DataSchemaItem;
import dev.aikido.agent_api.api_discovery.DataSchemaType;
import dev.aikido.agent_api.api_discovery.GetApiInfo;
import dev.aikido.agent_api.context.ContextObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class GetApiInfoTest {

    private ContextObject context;

    @BeforeEach
    public void setUp() {
        context = Mockito.mock(ContextObject.class);
    }

    @Test
    public void testGetApiInfoWithFormEncodedContext() {
        Map<String, Object> body = new HashMap<>();
        body.put(
                "data1",
                Map.of(
                        "data2",
                        List.of(Map.of("Help", true), Map.of("Help", true, "location", "Sea")),
                        "identifier",
                        "hsfkjewhfwehgkjwehgkj",
                        "active",
                        true));
        body.put(
                "user",
                Map.of(
                        "name", "John Doe",
                        "email", "john.doe@example.com"));

        Mockito.when(context.getMethod()).thenReturn("GET");
        Mockito.when(context.getUrl()).thenReturn("/api/resource1");
        Mockito.when(context.getBody()).thenReturn(body);
        HashMap<String, String> headers = new HashMap<>(Map.of("content-type", "application/x-www-form-urlencoded"));
        Mockito.when(context.getHeaders()).thenReturn(headers);
        APISpec apiInfo = GetApiInfo.getApiInfo(context);
        assertNotNull(apiInfo);
        assertNotNull(apiInfo.body().schema());
        DataSchemaItem schema = apiInfo.body().schema();
        assertEquals(DataSchemaType.OBJECT, schema.type());

        // Check properties of the schema
        Map<String, DataSchemaItem> properties = schema.properties();
        assertNotNull(properties);
        assertEquals(2, properties.size()); // Assuming there are 2 properties: data1 and user

        // Check the 'data1' property
        assertEquals(DataSchemaType.OBJECT, properties.get("data1").type());
        Map<String, DataSchemaItem> data1Properties = properties.get("data1").properties();
        assertEquals(3, data1Properties.size()); // Assuming there are 3 properties: identifier, data2, active
        assertEquals(DataSchemaType.STRING, data1Properties.get("identifier").type());
        assertFalse(data1Properties.get("identifier").optional()); // Assuming optional is a method that returns boolean

        // Check 'data2' property
        DataSchemaItem data2 = data1Properties.get("data2");
        assertEquals(DataSchemaType.ARRAY, data2.type());
        assertFalse(data2.optional());
        assertNotNull(data2.items());

        assertEquals(DataSchemaType.OBJECT, data2.items().type());
        Map<String, DataSchemaItem> data2ItemProperties = data2.items().properties();
        assertEquals(2, data2ItemProperties.size()); // Assuming there are 2 properties: location, Help

        assertEquals(DataSchemaType.STRING, data2ItemProperties.get("location").type());
        assertTrue(data2ItemProperties.get("location").optional());
        assertEquals(DataSchemaType.BOOL, data2ItemProperties.get("Help").type());
        assertFalse(data2ItemProperties.get("Help").optional());

        // Check 'active' property
        assertEquals(DataSchemaType.BOOL, data1Properties.get("active").type());
        assertFalse(data1Properties.get("active").optional());

        // Check the 'user' property
        DataSchemaItem user = properties.get("user");
        assertEquals(DataSchemaType.OBJECT, user.type());
        Map<String, DataSchemaItem> userProperties = user.properties();
        assertEquals(2, userProperties.size()); // Assuming there are 2 properties: name, email

        assertEquals(DataSchemaType.STRING, userProperties.get("name").type());
        assertFalse(userProperties.get("name").optional());
        assertEquals(DataSchemaType.STRING, userProperties.get("email").type());
        assertFalse(userProperties.get("email").optional());

        assertEquals("form-urlencoded", apiInfo.body().type());
    }

    @Test
    public void testGetApiInfoWithJson() {
        Map<String, Object> body = new HashMap<>();
        body.put(
                "user",
                Map.of(
                        "name", "John Doe",
                        "email", "john.doe@example.com"));

        HashMap<String, List<String>> query = new HashMap<>();
        query.put("user2", List.of("a", "b"));

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
        assertEquals("json", apiInfo.body().type());
        assertEquals(
                "{\"type\":\"object\",\"properties\":{\"user\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\",\"optional\":false},\"email\":{\"type\":\"string\",\"optional\":false}},\"optional\":false}},\"optional\":false}",
                gson.toJson(apiInfo.body().schema()));
        assertEquals(
                "{\"type\":\"object\",\"properties\":{\"user2\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"optional\":false},\"optional\":false}},\"optional\":false}",
                gson.toJson(apiInfo.query()));
        assertNull(apiInfo.auth());
    }

    @Test
    public void testGetApiInfoWithEmptyBody() {
        HashMap<String, List<String>> query = new HashMap<>();
        query.put("user2", List.of("a", "b"));

        Mockito.when(context.getMethod()).thenReturn("GET");
        Mockito.when(context.getUrl()).thenReturn("/api/resource1");
        Mockito.when(context.getBody()).thenReturn(null);
        Mockito.when(context.getQuery()).thenReturn(query);
        Mockito.when(context.getHeaders()).thenReturn(new HashMap<>(Map.of("content-type", "application/json")));

        APISpec apiInfo = GetApiInfo.getApiInfo(context);
        assertNotNull(apiInfo);
        assertNull(apiInfo.body());
        assertNotNull(apiInfo.query());
        Gson gson = new Gson();
        assertEquals(
                "{\"type\":\"object\",\"properties\":{\"user2\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"optional\":false},\"optional\":false}},\"optional\":false}",
                gson.toJson(apiInfo.query()));
        assertNull(apiInfo.auth());
    }

    @Test
    public void testGetApiInfoWithEmptyQueryAndBody() {
        HashMap<String, List<String>> query = new HashMap<>();

        Mockito.when(context.getMethod()).thenReturn("GET");
        Mockito.when(context.getUrl()).thenReturn("/api/resource1");
        Mockito.when(context.getBody()).thenReturn(null);
        Mockito.when(context.getQuery()).thenReturn(query);
        Mockito.when(context.getHeaders()).thenReturn(new HashMap<>(Map.of("content-type", "application/json")));

        APISpec apiInfo = GetApiInfo.getApiInfo(context);
        assertNotNull(apiInfo);
        assertNull(apiInfo.body());
        assertNull(apiInfo.query());
        assertNull(apiInfo.auth());
    }

    @Test
    public void testGetApiInfoWithNullQueryAndBody() {
        Mockito.when(context.getMethod()).thenReturn("GET");
        Mockito.when(context.getUrl()).thenReturn("/api/resource1");
        Mockito.when(context.getBody()).thenReturn(null);
        Mockito.when(context.getQuery()).thenReturn(null);
        Mockito.when(context.getHeaders()).thenReturn(new HashMap<>(Map.of("content-type", "application/json")));

        APISpec apiInfo = GetApiInfo.getApiInfo(context);
        assertNotNull(apiInfo);
        assertNull(apiInfo.body());
        assertNull(apiInfo.query());
        assertNull(apiInfo.auth());
    }

    @Test
    public void testGetApiInfoWithInvalidHeader() {
        Map<String, Object> body = new HashMap<>();
        body.put(
                "data1",
                Map.of(
                        "data2",
                        List.of(Map.of("Help", true), Map.of("Help", true, "location", "Sea")),
                        "identifier",
                        "hsfkjewhfwehgkjwehgkj",
                        "active",
                        true));
        body.put(
                "user",
                Map.of(
                        "name", "John Doe",
                        "email", "john.doe@example.com"));

        Mockito.when(context.getMethod()).thenReturn("GET");
        Mockito.when(context.getUrl()).thenReturn("/api/resource1");
        Mockito.when(context.getBody()).thenReturn(body);
        HashMap<String, String> headers = new HashMap<>(Map.of("content-type", "application/invalid-form-type"));
        Mockito.when(context.getHeaders()).thenReturn(headers);

        APISpec apiInfo = GetApiInfo.getApiInfo(context);
        assertNotNull(apiInfo);
        assertNull(apiInfo.body());
        assertNull(apiInfo.query());
        assertNull(apiInfo.auth());
    }
}
