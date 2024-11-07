package dev.aikido.agent_api.api_discovery;

import com.google.gson.annotations.SerializedName;

public enum DataSchemaType {
    @SerializedName("string")
    STRING,
    
    @SerializedName("number")
    NUMBER,

    @SerializedName("boolean")
    BOOL,

    @SerializedName("array")
    ARRAY,

    @SerializedName("object")
    OBJECT,

    @SerializedName("null")
    EMPTY;
}
