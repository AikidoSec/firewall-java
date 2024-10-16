package dev.aikido.AikidoAgent.vulnerabilities.path_traversal;

import dev.aikido.AikidoAgent.vulnerabilities.Detector;

import static dev.aikido.AikidoAgent.vulnerabilities.path_traversal.FileUrlParser.parseAsFileUrl;
import static dev.aikido.AikidoAgent.vulnerabilities.path_traversal.UnsafePathChecker.startsWithUnsafePath;
import static dev.aikido.AikidoAgent.vulnerabilities.path_traversal.UnsafePathPartsChecker.containsUnsafePathParts;

public class PathTraversalDetector implements Detector {
    /**
     * @param userInput, this is the user input which we will evaluate
     * @param arguments, Includes one element : filename
     * @return boolean value which is true if a vulnerability is detected
     */
    @Override
    public boolean run(String userInput, String[] arguments) {
        if (arguments.length != 1 || arguments[0].isEmpty()) {
            return false;
        }
        String filePath = arguments[0];
        if (userInput.length() <= 1) {
            // Ignore single characters since they don't pose a big threat.
            return false;
        }

        boolean isUrl = false; // Fix later
        if (isUrl && containsUnsafePathParts(userInput)) {
            // Check for URL path traversal
            String filePathFromUrl = parseAsFileUrl(userInput);
            if (filePathFromUrl != null && filePathFromUrl.equals(filePath)) {
                return true;
            }
        }
        if (userInput.length() > filePath.length()) {
            // Ignore cases where the user input is longer than the file path.
            return false;
        }
        if (!filePath.contains(userInput)) {
            // Ignore cases where the user input is not part of the file path.
            return false;
        }
        if (containsUnsafePathParts(filePath) && containsUnsafePathParts(userInput)) {
            // Check for unsafe path parts in both file path and user input
            return true;
        }

        // Check for absolute path traversal
        return startsWithUnsafePath(filePath, userInput);
    }
}
