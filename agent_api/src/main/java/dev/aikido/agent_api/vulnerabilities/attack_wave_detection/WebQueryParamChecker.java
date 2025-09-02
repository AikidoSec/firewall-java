package dev.aikido.agent_api.vulnerabilities.attack_wave_detection;

import java.util.HashMap;
import java.util.List;

public final class WebQueryParamChecker {
    private static final List<String> KEYWORDS = List.of(
        "SELECT (CASE WHEN",
        "SELECT COUNT(",
        "SLEEP(",
        "WAITFOR DELAY",
        "SELECT LIKE(CHAR(", "INFORMATION_SCHEMA.COLUMNS",
        "INFORMATION_SCHEMA.TABLES",
        "MD5(",
        "DBMS_PIPE.RECEIVE_MESSAGE",
        "SYSIBM.SYSTABLES",
        "RANDOMBLOB(",
        "SELECT * FROM",
        "1'='1",
        "PG_SLEEP(",
        "UNION ALL SELECT",
        "../"
    );

    private WebQueryParamChecker() {
    }

    public static boolean queryParamsContainDangerousPayload(HashMap<String, List<String>> queryStrings) {
        if (queryStrings == null) {
            return false;
        }

        for (List<String> strs : queryStrings.values()) {
            for (String str: strs) {
                if (str.length() < 5 || str.length() > 1000) {
                    continue;
                }
                String upperStr = str.toUpperCase();
                for (String keyword : KEYWORDS) {
                    if (upperStr.contains(keyword)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
