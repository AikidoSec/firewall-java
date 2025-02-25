package dev.aikido.agent_api.helpers;

import com.google.gson.*;
import dev.aikido.agent_api.helpers.net.IPList;

import java.lang.reflect.Type;

public class IPListDeserializer implements JsonDeserializer<IPList> {
    @Override
    public IPList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
        JsonArray jsonArray = json.getAsJsonArray();
        if (jsonArray == null) {
            return null;
        }

        IPList ipList = new IPList();
        json.getAsJsonArray().forEach(ip -> {
                ipList.add(ip.getAsString());
        });
        return ipList;
    }
}
