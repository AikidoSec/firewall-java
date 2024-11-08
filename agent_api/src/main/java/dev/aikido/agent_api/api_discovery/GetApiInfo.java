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

    private static APISpec.Body getBodyInfo(ContextObject context) {
        Object body = context.getBody();
        if (body != null) {
            DataSchemaItem dataSchema = getDataSchema(body);
            String dataType = getBodyDataType(context.getHeaders());
            if(dataType != null && dataSchema != null) {
                return new APISpec.Body(
                        /* schema: */ getDataSchema(body),
                        /* type: */ getBodyDataType(context.getHeaders())
                );
            }
        }
        return null;
    }
    private static DataSchemaItem getQueryInfo(ContextObject context) {
        Map<String, String[]> query =  context.getQuery();
        if(query != null && !query.isEmpty()) {
            return getDataSchema(query);
        }
        return null;
    }

}
