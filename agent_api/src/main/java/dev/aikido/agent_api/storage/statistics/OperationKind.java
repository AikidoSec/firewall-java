package dev.aikido.agent_api.storage.statistics;

import com.google.gson.annotations.SerializedName;

public enum OperationKind {
    @SerializedName("sql_op")
    SQL_OP,

    @SerializedName("nosql_op")
    NOSQL_OP,

    @SerializedName("outgoing_http_op")
    OUTGOING_HTTP_OP,

    @SerializedName("fs_op")
    FS_OP,

    @SerializedName("path_op")
    PATH_OP,

    @SerializedName("exec_op")
    EXEC_OP,

    @SerializedName("deserialize_op")
    DESERIALIZE_OP,

    @SerializedName("graphql_op")
    GRAPHQL_OP,

    @SerializedName("eval_op")
    EVAL_OP
}