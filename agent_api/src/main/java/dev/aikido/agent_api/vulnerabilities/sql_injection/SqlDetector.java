package dev.aikido.agent_api.vulnerabilities.sql_injection;

import dev.aikido.agent_api.vulnerabilities.Detector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.regex.Pattern;

public class SqlDetector implements Detector {
    private static final Logger logger = LogManager.getLogger(SqlDetector.class);
    /**
     * @param userInput contains the user input which we want to scan
     * @param arguments contains: [query, dialect]
     * @return True if it detected an injection
     */
    public DetectorResult run(String userInput, String[] arguments) {
        if (arguments.length != 2) {
            logger.debug("Arguments mismatch for SqlDetector");
            return new DetectorResult();
        }
        String query = arguments[0];
        Dialect dialect = new Dialect(arguments[1]);
        boolean detectedAttack = detectSqlInjection(query, userInput, dialect);
        if (detectedAttack) {
            Map<String, String> metadata = Map.of("sql", query);
            return new DetectorResult(/* detectedAttack*/ true, metadata, SQLInjectionException.get(dialect));
        }
        return new DetectorResult();
    }
    public static boolean detectSqlInjection(String query, String userInput, Dialect dialect) {
        String queryLower = query.toLowerCase();
        String userInputLower = userInput.toLowerCase();
        if (shouldReturnEarly(queryLower, userInputLower)) {
            return false;
        }
        return RustSQLInterface.detectSqlInjection(queryLower, userInputLower, dialect);
    }
    /**
     *     Input : Lowercased query and user_input.
     *     Returns true if the detect_sql_injection algo should return early :
     *     - user_input is <= 1 char or user input larger than query
     *     - user_input not in query
     *     - user_input is alphanumerical
     *     - user_input is an array of integers
     */
    public static boolean shouldReturnEarly(String query, String userInput) {
        // Check if userInput is less than or equal to 1 character or larger than query
        if (userInput.length() <= 1 || query.length() < userInput.length()) {
            return true;
        }

        // Check if userInput is not in query
        if (!query.contains(userInput)) {
            return true;
        }

        // Check if userInput is alphanumerical (with underscores)
        if (userInput.replace("_", "").matches("^[a-zA-Z0-9_]+$")) {
            return true;
        }

        // Clean input for list and check if it's a valid comma-separated number list
        String cleanedInputForList = userInput.replace(" ", "").replace(",", "");
        Pattern pattern = Pattern.compile("^\\d+$");
        return pattern.matcher(cleanedInputForList).matches();
    }
}

