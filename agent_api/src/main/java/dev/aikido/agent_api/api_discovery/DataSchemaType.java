package dev.aikido.agent_api.api_discovery;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public enum DataSchemaType implements Serializable {
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

    @SerializedName("enum")
    ENUM,

    @SerializedName("null")
    EMPTY;
}
