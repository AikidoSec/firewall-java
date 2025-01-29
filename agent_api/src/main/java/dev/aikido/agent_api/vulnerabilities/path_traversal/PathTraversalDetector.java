package dev.aikido.agent_api.vulnerabilities.path_traversal;

import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathChecker.startsWithUnsafePath;
import static dev.aikido.agent_api.vulnerabilities.path_traversal.UnsafePathPartsChecker.containsUnsafePathParts;

import dev.aikido.agent_api.vulnerabilities.Detector;
import java.util.Map;

public class PathTraversalDetector implements Detector {
    /**
     * @param userInput, this is the user input which we will evaluate
     * @param arguments, Includes one element : filename
     * @return boolean value which is true if a vulnerability is detected
     */
    @Override
    public DetectorResult run(String userInput, String[] arguments) {
        // filePath can also result from URI object, but this gets solved in Collector.
        if (arguments.length != 1 || arguments[0].isEmpty()) {
            return new DetectorResult();
        }
        String filePath = arguments[0];
        if (userInput.length() <= 1) {
            // Ignore single characters since they don't pose a big threat.
            return new DetectorResult();
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
            return new DetectorResult(true, Map.of("filename", filePath), PathTraversalException.get());
        }

        // Check for absolute path traversal
        if (startsWithUnsafePath(filePath, userInput)) {
            return new DetectorResult(true, Map.of("filename", filePath), PathTraversalException.get());
        }
        ;
        return new DetectorResult();
    }

    @Override
    public boolean returnEarly(String[] args) {
        if (args.length == 1) {
            return PathTraversalEarlyReturn.shouldReturnEarly(args[0]);
        }
        return false;
    }
}
