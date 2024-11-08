package api_discovery;

import static dev.aikido.agent_api.api_discovery.DataSchemaMerger.mergeDataSchemas;
import static org.junit.jupiter.api.Assertions.*;

import dev.aikido.agent_api.api_discovery.DataSchemaItem;
import dev.aikido.agent_api.api_discovery.DataSchemaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class DataSchemaMergerTest {

    private DataSchemaItem schemaA;
    private DataSchemaItem schemaB;
    private DataSchemaItem schemaC;
    private DataSchemaItem schemaD;
    private DataSchemaItem schemaE;
    private DataSchemaItem schemaF;

    @BeforeEach
    public void setUp() {
        schemaA = new DataSchemaItem(DataSchemaType.OBJECT, Map.of(
                "name", new DataSchemaItem(DataSchemaType.STRING),
                "age", new DataSchemaItem(DataSchemaType.NUMBER)
        ));

        schemaB = new DataSchemaItem(DataSchemaType.OBJECT, Map.of(
                "age", new DataSchemaItem(DataSchemaType.NUMBER),
                "email", new DataSchemaItem(DataSchemaType.STRING)
        ));

        schemaC = new DataSchemaItem(DataSchemaType.EMPTY);

        schemaD = new DataSchemaItem(DataSchemaType.OBJECT, Map.of(
                "address", new DataSchemaItem(DataSchemaType.STRING)
        ));

        schemaE = new DataSchemaItem(DataSchemaType.ARRAY, new DataSchemaItem(DataSchemaType.STRING));

        schemaF = new DataSchemaItem(DataSchemaType.ARRAY, new DataSchemaItem(DataSchemaType.NUMBER));
    }

    @Test
    public void testMergeSameTypeSchemas() {
        DataSchemaItem merged = mergeDataSchemas(schemaA, schemaB);
        Map<String, DataSchemaItem> props = merged.properties();
        assertEquals(DataSchemaType.STRING, props.get("name").type());
        assertEquals(DataSchemaType.NUMBER, props.get("age").type());
        assertEquals(DataSchemaType.STRING, props.get("email").type());
        assertFalse(props.get("age").optional());
        assertTrue(props.get("email").optional());
    }

    @Test
    public void testMergeDifferentTypeSchemas() {
        DataSchemaItem merged = mergeDataSchemas(schemaA, schemaC);
        assertEquals(schemaA, merged);
    }

    @Test
    public void testMergeNullTypePreference() {
        DataSchemaItem merged = mergeDataSchemas(schemaC, schemaB);
        assertEquals(schemaB, merged);
    }

    @Test
    public void testMergeWithOptionalProperties() {
        DataSchemaItem merged = mergeDataSchemas(schemaA, schemaD);
        assertTrue(merged.properties().containsKey("address"));
        assertTrue(merged.properties().get("address").optional());
    }

    @Test
    public void testMergeArraySchemas() {
        DataSchemaItem merged = mergeDataSchemas(schemaE, schemaF);
        assertEquals(DataSchemaType.ARRAY, merged.type());
        assertEquals(DataSchemaType.STRING, merged.items().type()); // Assuming we prefer the first schema's item type
    }

    @Test
    public void testMergeEmptyProperties() {
        DataSchemaItem emptySchema = new DataSchemaItem(DataSchemaType.OBJECT);
        DataSchemaItem merged = mergeDataSchemas(schemaA, emptySchema);
        assertEquals(schemaA.properties(), merged.properties());
    }

    @Test
    public void testMergeWithNestedSchemas() {
        DataSchemaItem nestedSchemaA = new DataSchemaItem(DataSchemaType.OBJECT, Map.of(
                "details", schemaA
        ));
        DataSchemaItem nestedSchemaB = new DataSchemaItem(DataSchemaType.OBJECT, Map.of(
                "details", schemaB
        ));
        DataSchemaItem merged = mergeDataSchemas(nestedSchemaA, nestedSchemaB);
        assertTrue(merged.properties().containsKey("details"));
        assertEquals(DataSchemaType.STRING, merged.properties().get("details").properties().get("name").type());
        assertEquals(DataSchemaType.STRING, merged.properties().get("details").properties().get("email").type());
        assertEquals(DataSchemaType.NUMBER, merged.properties().get("details").properties().get("age").type());
    }

    @Test
    public void testMergeWithNoItems() {
        DataSchemaItem emptyArraySchema = new DataSchemaItem(DataSchemaType.ARRAY, (DataSchemaItem) null);
        DataSchemaItem merged = mergeDataSchemas(schemaE, emptyArraySchema);
        assertEquals(schemaE.items(), merged.items());
    }

    @Test
    public void testWithDifferentItemTypes() {
        DataSchemaItem merged = mergeDataSchemas(schemaE, schemaF);
        assertEquals(merged.items().type(), DataSchemaType.STRING);
    }
}