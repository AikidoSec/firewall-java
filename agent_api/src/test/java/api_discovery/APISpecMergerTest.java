package api_discovery;

import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.api_discovery.APISpecMerger;
import dev.aikido.agent_api.api_discovery.DataSchemaItem;
import dev.aikido.agent_api.api_discovery.DataSchemaType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.api_discovery.DataSchemaMerger.mergeDataSchemas;
import static org.junit.jupiter.api.Assertions.*;

class APISpecMergerTest {

    @Test
    void testMergeAPISpecsBothNull() {
        APISpec result = APISpecMerger.mergeAPISpecs(null, null);
        assertNull(result);
    }

    @Test
    void testMergeAPISpecsUpdatedNull() {
        APISpec oldSpec = createAPISpec("oldType", "oldQuery", List.of(Map.of("type", "apiKey")));
        APISpec result = APISpecMerger.mergeAPISpecs(null, oldSpec);
        assertEquals(oldSpec, result);
    }

    @Test
    void testMergeAPISpecsOldNull() {
        APISpec updatedSpec = createAPISpec("newType", "newQuery", List.of(Map.of("type", "bearer")));
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, null);
        assertEquals(updatedSpec, result);
    }

    @Test
    void testMergeAPISpecsBodyOnlyUpdated() {
        APISpec updatedSpec = createAPISpec("newType", null, null);
        APISpec oldSpec = createAPISpec("oldType", null, null);
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);
        assertEquals(updatedSpec.body().type(), result.body().type());
        assertNotEquals(oldSpec.body().type(), result.body().type());
    }

    @Test
    void testMergeAPISpecsBodyOnlyOld() {
        APISpec updatedSpec = createAPISpec(null, null, null);
        APISpec oldSpec = createAPISpec("oldType", null, null);
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);
        assertEquals(oldSpec.body().type(), result.body().type());
    }

    @Test
    void testMergeAPISpecsBodyBothPresent() {
        APISpec updatedSpec = createAPISpec("newType", null, null);
        APISpec oldSpec = createAPISpec("oldType", null, null);
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);
        assertEquals("newType", result.body().type());
    }

    @Test
    void testMergeAPISpecsQueryOnlyUpdated() {
        APISpec updatedSpec = createAPISpec(null, "oldQuery", null);
        APISpec oldSpec = createAPISpec(null, "oldQuery", null);
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);
        assertEquals(updatedSpec.query(), result.query());
        assertEquals(oldSpec.query(), result.query());
    }

    @Test
    void testMergeAPISpecsQueryRunsMergeFunction() {
        APISpec updatedSpec = createAPISpec(null, "oldQuery", null);
        APISpec oldSpec = createAPISpec(null, "newQuery", null);
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);
        assertEquals(mergeDataSchemas(updatedSpec.query(), oldSpec.query()), result.query());
    }

    @Test
    void testMergeAPISpecsQueryOnlyOld() {
        APISpec updatedSpec = createAPISpec(null, null, null);
        APISpec oldSpec = createAPISpec(null, "oldQuery", null);
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);
        assertEquals(oldSpec.query(), result.query());
    }

    @Test
    void testMergeAPISpecsQueryBothPresent() {
        DataSchemaItem oldQuerySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.emptyMap());
        DataSchemaItem newQuerySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("newProp", new DataSchemaItem(DataSchemaType.STRING)));

        APISpec updatedSpec = new APISpec(new APISpec.Body(newQuerySchema, "newType"), newQuerySchema, null);
        APISpec oldSpec = new APISpec(new APISpec.Body(oldQuerySchema, "oldType"), oldQuerySchema, null);

        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);
        assertNotNull(result.query());
        assertNotEquals(oldSpec.query(), result.query());
    }

    @Test
    void testMergeAPISpecsAuthOnlyUpdated() {
        APISpec updatedSpec = createAPISpec(null, null, List.of(Map.of("type", "bearer")));
        APISpec oldSpec = createAPISpec(null, null, List.of(Map.of("type", "apiKey")));
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertEquals(2, result.auth().size());
        assertTrue(result.auth().stream().anyMatch(auth -> auth.get("type").equals("bearer")));
        assertTrue(result.auth().stream().anyMatch(auth -> auth.get("type").equals("apiKey")));
    }

    @Test
    void testMergeAPISpecsAuthOnlyOld() {
        APISpec updatedSpec = createAPISpec(null, null, null);
        APISpec oldSpec = createAPISpec(null, null, List.of(Map.of("type", "apiKey")));
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertEquals(1, result.auth().size());
        assertEquals("apiKey", result.auth().get(0).get("type"));
    }

    @Test
    void testMergeAPISpecsAuthBothPresent() {
        APISpec updatedSpec = createAPISpec(null, null, List.of(Map.of("type", "bearer")));
        APISpec oldSpec = createAPISpec(null, null, List.of(Map.of("type", "apiKey")));
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertEquals(2, result.auth().size());
        assertTrue(result.auth().stream().anyMatch(auth -> auth.get("type").equals("bearer")));
        assertTrue(result.auth().stream().anyMatch(auth -> auth.get("type").equals("apiKey")));
    }

    @Test
    void testMergeAPISpecsAuthWithDuplicates() {
        APISpec updatedSpec = createAPISpec(null, null, List.of(Map.of("type", "bearer"), Map.of("type", "apiKey")));
        APISpec oldSpec = createAPISpec(null, null, List.of(Map.of("type", "apiKey")));
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertEquals(2, result.auth().size());
        assertTrue(result.auth().stream().anyMatch(auth -> auth.get("type").equals("bearer")));
        assertTrue(result.auth().stream().anyMatch(auth -> auth.get("type").equals("apiKey")));
    }

    @Test
    void testMergeAPISpecsEmptyAuthLists() {
        APISpec updatedSpec = createAPISpec(null, null, Collections.emptyList());
        APISpec oldSpec = createAPISpec(null, null, Collections.emptyList());
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertEquals(0, result.auth().size());
    }

    @Test
    void testMergeAPISpecsBodyAndAuthPresent() {
        DataSchemaItem oldBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("oldProp", new DataSchemaItem(DataSchemaType.STRING)));
        DataSchemaItem newBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("newProp", new DataSchemaItem(DataSchemaType.STRING)));

        APISpec updatedSpec = new APISpec(new APISpec.Body(newBodySchema, "newType"), null, List.of(Map.of("type", "bearer")));
        APISpec oldSpec = new APISpec(new APISpec.Body(oldBodySchema, "oldType"), null, List.of(Map.of("type", "apiKey")));

        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertNotNull(result.body());
        assertEquals("newType", result.body().type());
        assertEquals(2, result.auth().size());
    }

    @Test
    void testMergeWithOldSpecBodyQueryNull() {
        APISpec updatedSpec = createAPISpec("newType", "newQuery", List.of(Map.of("type", "bearer")));
        APISpec oldSpec = createAPISpec(null, null, List.of(Map.of("type", "bearer")));

        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertNotNull(result.body());
        assertEquals("newType", result.body().type());
        assertEquals(updatedSpec.query(), result.query());
        assertEquals(1, result.auth().size());
    }

    @Test
    void testMergeWithOldSpecBodyQueryNullReversed() {
        APISpec updatedSpec = createAPISpec("newType", "newQuery", List.of(Map.of("type", "bearer")));
        APISpec oldSpec = createAPISpec(null, null, List.of(Map.of("type", "bearer")));

        APISpec result = APISpecMerger.mergeAPISpecs(oldSpec, updatedSpec);

        assertNotNull(result.body());
        assertEquals("newType", result.body().type());
        assertEquals(updatedSpec.query(), result.query());
        assertEquals(1, result.auth().size());
    }

    @Test
    void testMergeAPISpecsWithNullBodyAndAuth() {
        APISpec updatedSpec = createAPISpec(null, null, null);
        APISpec oldSpec = createAPISpec(null, null, null);
        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertNull(result.body());
        assertNull(result.query());
        assertNull(result.auth());
    }

    @Test
    void testMergeAPISpecsWithPartialUpdates() {
        DataSchemaItem oldBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("oldProp", new DataSchemaItem(DataSchemaType.STRING)));
        APISpec updatedSpec = new APISpec(new APISpec.Body(oldBodySchema, "updatedType"), null, null);
        APISpec oldSpec = new APISpec(new APISpec.Body(oldBodySchema, "oldType"), null, List.of(Map.of("type", "apiKey")));

        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertEquals("updatedType", result.body().type());
        assertEquals(1, result.auth().size());
    }

    @Test
    void testMergeAPISpecsWithDifferentBodyTypes() {
        DataSchemaItem oldBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("oldProp", new DataSchemaItem(DataSchemaType.STRING)));
        DataSchemaItem newBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("newProp", new DataSchemaItem(DataSchemaType.STRING)));

        APISpec updatedSpec = new APISpec(new APISpec.Body(newBodySchema, "newType"), null, null);
        APISpec oldSpec = new APISpec(new APISpec.Body(oldBodySchema, "oldType"), null, null);

        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertEquals("newType", result.body().type());
        assertNotEquals(oldBodySchema, result.body().schema());
    }

    @Test
    void testMergeAPISpecsWithEmptyBodyAndAuth() {
        APISpec updatedSpec = createAPISpec(null, null, List.of(Map.of("type", "bearer")));
        APISpec oldSpec = createAPISpec(null, null, List.of(Map.of("type", "apiKey")));

        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertNull(result.body());
        assertEquals(2, result.auth().size());
    }

    @Test
    void testMergeAPISpecsWithComplexSchemas() {
        DataSchemaItem oldBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Map.of(
            "oldProp", new DataSchemaItem(DataSchemaType.STRING),
            "anotherOldProp", new DataSchemaItem(DataSchemaType.NUMBER)
        ));

        DataSchemaItem newBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Map.of(
            "newProp", new DataSchemaItem(DataSchemaType.STRING),
            "anotherNewProp", new DataSchemaItem(DataSchemaType.BOOL)
        ));

        APISpec updatedSpec = new APISpec(new APISpec.Body(newBodySchema, "newType"), null, null);
        APISpec oldSpec = new APISpec(new APISpec.Body(oldBodySchema, "oldType"), null, null);

        APISpec result = APISpecMerger.mergeAPISpecs(updatedSpec, oldSpec);

        assertNotNull(result.body());
        assertEquals("newType", result.body().type());
        assertNotEquals(oldBodySchema, result.body().schema());
    }

    // Helper method to create APISpec instances for testing
    private APISpec createAPISpec(String bodyType, String queryType, List<Map<String, String>> auth) {
        DataSchemaItem bodySchema = bodyType != null ? new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("prop", new DataSchemaItem(DataSchemaType.STRING))) : null;
        DataSchemaItem querySchema = queryType != null
            ? new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap(queryType + "Prop", new DataSchemaItem(DataSchemaType.STRING)))
            : null;
        return new APISpec(bodySchema != null ? new APISpec.Body(bodySchema, bodyType) : null, querySchema, auth);
    }
}
