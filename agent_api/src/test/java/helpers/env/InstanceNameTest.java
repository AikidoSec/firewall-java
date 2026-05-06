package helpers.env;

import dev.aikido.agent_api.helpers.env.InstanceName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

public class InstanceNameTest {

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_INSTANCE_NAME", value = "my-service")
    public void testFromEnv_WithValue() {
        assertEquals("my-service", InstanceName.fromEnv());
    }

    @Test
    @SetEnvironmentVariable(key = "AIKIDO_INSTANCE_NAME", value = "")
    public void testFromEnv_WithEmptyString() {
        assertNull(InstanceName.fromEnv());
    }

    @Test
    public void testFromEnv_WithNullEnvironmentVariable() {
        assertNull(InstanceName.fromEnv());
    }
}
