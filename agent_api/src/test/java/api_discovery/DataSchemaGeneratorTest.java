package api_discovery;

import com.google.gson.Gson;
import dev.aikido.agent_api.api_discovery.DataSchemaGenerator;
import dev.aikido.agent_api.api_discovery.DataSchemaItem;
import dev.aikido.agent_api.api_discovery.DataSchemaType;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class DataSchemaGeneratorTest {

    @Test
    public void testGetDataSchemaString() {
        DataSchemaItem schema = DataSchemaGenerator.getDataSchema("test");
        assertEquals(DataSchemaType.STRING, schema.type());
    }

    @Test
    public void testGetDataSchemaArray() {
        DataSchemaItem schema = DataSchemaGenerator.getDataSchema(List.of("test"));
        assertEquals(DataSchemaType.ARRAY, schema.type());
        assertEquals(DataSchemaType.STRING, schema.items().type());
    }

    @Test
    public void testGetDataSchemaObject() {
        Map<String, Object> input = new HashMap<>();
        input.put("test", "abc");
        DataSchemaItem schema = DataSchemaGenerator.getDataSchema(input);
        assertEquals(DataSchemaType.OBJECT, schema.type());
        assertTrue(schema.properties().containsKey("test"));
        assertEquals(DataSchemaType.STRING, schema.properties().get("test").type());
    }

    @Test
    public void testGetDataSchemaComplexObject() {
        Map<String, Object> input = new HashMap<>();
        input.put("test", 123);
        input.put("arr", Arrays.asList(1, 2, 3));
        DataSchemaItem schema = DataSchemaGenerator.getDataSchema(input);
        assertEquals(DataSchemaType.OBJECT, schema.type());
        assertEquals(DataSchemaType.NUMBER, schema.properties().get("test").type());
        assertEquals(DataSchemaType.ARRAY, schema.properties().get("arr").type());
        assertEquals(DataSchemaType.NUMBER, schema.properties().get("arr").items().type());
    }

    @Test
    public void testGetDataSchemaNestedObject() {
        Map<String, Object> input = new HashMap<>();
        input.put("test", 123);
        input.put("arr", Arrays.asList(Collections.singletonMap("sub", true)));
        input.put("x", null);
        DataSchemaItem schema = DataSchemaGenerator.getDataSchema(input);
        assertEquals(DataSchemaType.OBJECT, schema.type());
        assertEquals(DataSchemaType.NUMBER, schema.properties().get("test").type());
        assertEquals(DataSchemaType.ARRAY, schema.properties().get("arr").type());
        assertEquals(DataSchemaType.OBJECT, schema.properties().get("arr").items().type());
        assertEquals(DataSchemaType.BOOL, schema.properties().get("arr").items().properties().get("sub").type());
        assertEquals(DataSchemaType.EMPTY, schema.properties().get("x").type());
    }

    @Test
    public void testMaxDepth() {
        Gson gson = new Gson();
        Map<String, Object> obj = generateTestObjectWithDepth(10);
        DataSchemaItem schema = DataSchemaGenerator.getDataSchema(obj);
        String schemaAsString = gson.toJson(schema);
        assertTrue(schemaAsString.contains("string"));


        obj = generateTestObjectWithDepth(20);
        schema = DataSchemaGenerator.getDataSchema(obj);
        schemaAsString = gson.toJson(schema);

        assertTrue(schemaAsString.contains("string"));


        obj = generateTestObjectWithDepth(21);
        schema = DataSchemaGenerator.getDataSchema(obj);
        schemaAsString = gson.toJson(schema);

        assertFalse(schemaAsString.contains("string"));
    }

    @Test
    public void testMaxProperties() {
        Map<String, Object> obj = generateObjectWithProperties(80);
        DataSchemaItem schema = DataSchemaGenerator.getDataSchema(obj);
        assertEquals(80, schema.properties().size());

        obj = generateObjectWithProperties(100);
        schema = DataSchemaGenerator.getDataSchema(obj);
        assertEquals(100, schema.properties().size());

        obj = generateObjectWithProperties(120);
        schema = DataSchemaGenerator.getDataSchema(obj);
        assertEquals(100, schema.properties().size());
    }

    private Map<String, Object> generateTestObjectWithDepth(int depth) {
        if (depth == 0) {
            return Collections.singletonMap("value", "testValue");
        }
        return Collections.singletonMap("prop", generateTestObjectWithDepth(depth - 1));
    }

    private Map<String, Object> generateObjectWithProperties(int count) {
        Map<String, Object> obj = new HashMap<>();
        for (int i = 0; i < count; i++) {
            obj.put("props" + i, i);
        }
        return obj;
    }
}
