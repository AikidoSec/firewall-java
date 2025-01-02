package storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aikido.agent_api.api_discovery.APISpec;
import dev.aikido.agent_api.api_discovery.DataSchemaItem;
import dev.aikido.agent_api.api_discovery.DataSchemaType;
import dev.aikido.agent_api.storage.routes.RouteEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteEntryTest {
    private RouteEntry route1;

    @BeforeEach
    public void setup() {
        route1 = new RouteEntry("GET", "/api/1");
        DataSchemaItem oldBodySchema = new DataSchemaItem(DataSchemaType.OBJECT, Collections.singletonMap("oldProp", new DataSchemaItem(DataSchemaType.STRING)));
        APISpec spec = new APISpec(new APISpec.Body(oldBodySchema, "oldType"), null, List.of(Map.of("type", "apiKey")));
        route1.updateApiSpec(spec);
    }

    @Test
    public void testGsonWithSerializer() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RouteEntry.class, new RouteEntry.RouteEntrySerializer())
                .create();
        String json = gson.toJson(route1);
        assertEquals(
                "{\"method\":\"GET\",\"path\":\"/api/1\",\"hits\":0,\"apispec\":{\"body\":{\"schema\":{\"type\":\"object\",\"properties\":{\"oldProp\":{\"type\":\"string\",\"optional\":false}},\"optional\":false},\"type\":\"oldType\"},\"auth\":[{\"type\":\"apiKey\"}]}}",
                json
        );
    }

    @Test
    public void testGsonWithoutSerializer() throws IOException {
        Gson gson = new GsonBuilder()
                .create();
        String json = gson.toJson(route1);
        assertEquals(
                "{\"method\":\"GET\",\"path\":\"/api/1\",\"hits\":0}",
                json
        );
    }

}
