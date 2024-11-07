package dev.aikido.agent_api.api_discovery;

import dev.aikido.agent_api.context.ContextObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static dev.aikido.agent_api.api_discovery.DataSchemaGenerator.getDataSchema;
import static dev.aikido.agent_api.api_discovery.GetAuthTypes.getAuthTypes;
import static dev.aikido.agent_api.api_discovery.GetBodyDataType.getBodyDataType;

public class GetApiInfo {
    private static final Logger logger = LogManager.getLogger(GetApiInfo.class);
    public static APISpec getApiInfo(ContextObject context) {
        try {
            return new APISpec(
                    getBodyInfo(context),
                    getQueryInfo(context),
                    getAuthTypes(context)
            );
        } catch (Throwable e) {
            logger.trace(e);
        }
        return null;
    }

    private static Map<String, Object> getBodyInfo(ContextObject context) {
        Object body = context.getBody();
        if (body != null) {
            return Map.of(
                "type", getBodyDataType(context.getHeaders()),
                "schema", getDataSchema(body)
            );
        }
        return Map.of();
    }
    private static DataSchemaItem getQueryInfo(ContextObject context) {
        Map<String, String[]> query =  context.getQuery();
        if(query != null && !query.isEmpty()) {
            return getDataSchema(query);
        }
        return null;
    }

}
