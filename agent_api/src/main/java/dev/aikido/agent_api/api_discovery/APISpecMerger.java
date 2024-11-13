package dev.aikido.agent_api.api_discovery;

import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.api_discovery.AuthTypeMerger.mergeAuthTypes;
import static dev.aikido.agent_api.api_discovery.DataSchemaMerger.mergeDataSchemas;

public final class APISpecMerger {
    private APISpecMerger() {}
    public static APISpec mergeAPISpecs(APISpec updatedApiSpec, APISpec oldApiSpec) {
        if (updatedApiSpec == null) {
            return oldApiSpec;
        }
        if(oldApiSpec == null) {
            return updatedApiSpec;
        }

        // Body :
        APISpec.Body body = oldApiSpec.body();
        if (updatedApiSpec.body() != null && oldApiSpec.body() != null) {
            DataSchemaItem schema = mergeDataSchemas(oldApiSpec.body().schema(), updatedApiSpec.body().schema());
            body = new APISpec.Body(/* schema: */ schema, /* type: */ updatedApiSpec.body().type());
        } else if (updatedApiSpec.body() != null) {
            body = updatedApiSpec.body();
        }

        // Query :
        DataSchemaItem query = oldApiSpec.query();
        if (oldApiSpec.query() != null && updatedApiSpec.query() != null) {
            query = mergeDataSchemas(oldApiSpec.query(), updatedApiSpec.query());
        } else if(updatedApiSpec.query() != null) {
            query = updatedApiSpec.query();
        }

        // Auth :
        List<Map<String, String>> auth = mergeAuthTypes(oldApiSpec.auth(), updatedApiSpec.auth());
        return new APISpec(body, query, auth);
    }
}
