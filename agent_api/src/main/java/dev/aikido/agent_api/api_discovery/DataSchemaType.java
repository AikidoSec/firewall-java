package dev.aikido.agent_api.api_discovery;

public enum DataSchemaType {
    STRING("string"),
    NUMBER("number"),
    BOOL("boolean"),
    ARRAY("array"),
    OBJECT("object"),
    EMPTY("null");

    private final String type;
    DataSchemaType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
