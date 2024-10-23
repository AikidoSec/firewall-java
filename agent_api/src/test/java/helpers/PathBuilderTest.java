package helpers;

import dev.aikido.agent_api.helpers.extraction.PathBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathBuilderTest {

    @Test
    public void testBuildPathToPayloadEmpty() {
        assertEquals(".", PathBuilder.buildPathToPayload(Arrays.asList()));
    }

    @Test
    public void testBuildPathToPayloadSingleObject() {
        PathBuilder.PathPart pathPart = new PathBuilder.PathPart("object", "name");
        assertEquals(".name", PathBuilder.buildPathToPayload(Arrays.asList(pathPart)));
    }

    @Test
    public void testBuildPathToPayloadSingleArray() {
        PathBuilder.PathPart pathPart = new PathBuilder.PathPart("array", 0);
        assertEquals(".[0]", PathBuilder.buildPathToPayload(Arrays.asList(pathPart)));
    }

    @Test
    public void testBuildPathToPayloadSingleJwt() {
        PathBuilder.PathPart pathPart = new PathBuilder.PathPart("jwt", null);
        assertEquals("<jwt>", PathBuilder.buildPathToPayload(Arrays.asList(pathPart)));
    }

    @Test
    public void testBuildPathToPayloadMixedTypes() {
        PathBuilder.PathPart[] pathParts = {
                new PathBuilder.PathPart("object", "user"),
                new PathBuilder.PathPart("array", 2),
                new PathBuilder.PathPart("jwt", null),
                new PathBuilder.PathPart("object", "details"),
                new PathBuilder.PathPart("array", 1)
        };
        assertEquals(".user.[2]<jwt>.details.[1]", PathBuilder.buildPathToPayload(Arrays.asList(pathParts)));
    }

    @Test
    public void testBuildPathToPayloadMultipleObjects() {
        PathBuilder.PathPart[] pathParts = {
                new PathBuilder.PathPart("object", "user"),
                new PathBuilder.PathPart("object", "details"),
                new PathBuilder.PathPart("object", "address")
        };
        assertEquals(".user.details.address", PathBuilder.buildPathToPayload(Arrays.asList(pathParts)));
    }
}
