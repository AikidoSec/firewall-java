package helpers;

import dev.aikido.agent_api.helpers.ShouldBlockHelper;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShouldBlockHelperTest {

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "true")
    public void testWithInvalidAikidoTokenTrue() {
        assertTrue(ShouldBlockHelper.shouldBlock());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_TOKEN", value = "")
    @SetEnvironmentVariable(key = "AIKIDO_BLOCK", value = "false")
    public void testWithInvalidAikidoTokenFalse() {
        assertFalse(ShouldBlockHelper.shouldBlock());
    }
}
