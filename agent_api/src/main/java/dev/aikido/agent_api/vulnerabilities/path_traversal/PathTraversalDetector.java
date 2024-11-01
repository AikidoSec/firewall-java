package dev.aikido.agent_api.vulnerabilities.path_traversal;

import dev.aikido.agent_api.vulnerabilities.Detector;

import java.util.HashMap;

import static dev.aikido.agent_api.vulnerabilities.path_traversal.FileUrlParser.parseAsFileUrl;
import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathChecker.startsWithUnsafePath;
import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathPartsChecker.containsUnsafePathParts;

public class PathTraversalDetector implements Detector {
    /**
     * @param userInput, this is the user input which we will evaluate
     * @param arguments, Includes one element : filename
     * @return boolean value which is true if a vulnerability is detected
     */
    @Override
    public DetectorResult run(String userInput, String[] arguments) {
        if (arguments.length != 1 || arguments[0].isEmpty()) {
            return new DetectorResult();
        }
        String filePath = arguments[0];
        if (userInput.length() <= 1) {
            // Ignore single characters since they don't pose a big threat.
            return new DetectorResult();
        }

        boolean isUrl = false; // Fix later
        if (isUrl && containsUnsafePathParts(userInput)) {
            // Check for URL path traversal
            String filePathFromUrl = parseAsFileUrl(userInput);
            if (filePathFromUrl != null && filePathFromUrl.equals(filePath)) {
                return new DetectorResult(true, new HashMap<>(), PathTraversalException.get());
            }
        }
        if (userInput.length() > filePath.length()) {
            // Ignore cases where the user input is longer than the file path.
            return new DetectorResult();
        }
        if (!filePath.contains(userInput)) {
            // Ignore cases where the user input is not part of the file path.
            return new DetectorResult();
        }
        if (containsUnsafePathParts(filePath) && containsUnsafePathParts(userInput)) {
            // Check for unsafe path parts in both file path and user input
            return new DetectorResult(true, new HashMap<>(), PathTraversalException.get());
        }

        // Check for absolute path traversal
        if (startsWithUnsafePath(filePath, userInput)) {
            return new DetectorResult(true, new HashMap<>(), PathTraversalException.get());
        };
        return new DetectorResult();
    }
}
