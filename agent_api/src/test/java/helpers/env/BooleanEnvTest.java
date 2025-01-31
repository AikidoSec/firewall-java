package helpers.env;

import dev.aikido.agent_api.helpers.env.BooleanEnv;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BooleanEnvTest {

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "1")
    public void testBooleanEnv_WithValueOne() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", false);
        assertTrue(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "true")
    public void testBooleanEnv_WithValueTrue() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", false);
        assertTrue(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "TRUE")
    public void testBooleanEnv_WithValueTrueUppercase() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", false);
        assertTrue(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "0")
    public void testBooleanEnv_WithValueZero() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", true);
        assertFalse(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "false")
    public void testBooleanEnv_WithValueFalse() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", true);
        assertFalse(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "FALSE")
    public void testBooleanEnv_WithValueFalseUppercase() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", true);
        assertFalse(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "")
    public void testBooleanEnv_WithEmptyString() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", true);
        assertTrue(booleanEnv.getValue());
    }

    @Test
    public void testBooleanEnv_WithNullEnvironmentVariable() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", true);
        assertTrue(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "randomString")
    public void testBooleanEnv_WithRandomString() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", false);
        assertFalse(booleanEnv.getValue());
    }

    @Test
    @SetEnvironmentVariable(key = "TEST_BOOLEAN_ENV", value = "TrUe")
    public void testBooleanEnv_WithMixedCaseTrue() {
        BooleanEnv booleanEnv = new BooleanEnv("TEST_BOOLEAN_ENV", false);
        assertTrue(booleanEnv.getValue());
    }
}
