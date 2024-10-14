package dev.aikido.AikidoAgent.vulnerabilities.sql_injection;

import java.util.regex.Pattern;
import java.util.regex.Pattern;
public class SqlInjection {
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

