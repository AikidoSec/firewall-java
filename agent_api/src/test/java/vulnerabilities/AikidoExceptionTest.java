package vulnerabilities;


import dev.aikido.agent_api.vulnerabilities.AikidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AikidoExceptionTest {

    @Test
    public void testAikidoExceptionWithMessage() {
        String message = "Custom vulnerability message";
        AikidoException exception = new AikidoException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    public void testAikidoExceptionWithoutMessage() {
        AikidoException exception = new AikidoException();

        assertEquals("Aikido Zen has blocked an unknown vulnerability", exception.getMessage());
    }

    @Test
    public void testGenerateDefaultMessage() {
        String vulnerabilityName = "SQL Injection";
        String expectedMessage = "Aikido Zen has blocked SQL Injection";

        String actualMessage = AikidoException.generateDefaultMessage(vulnerabilityName);

        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testAikidoExceptionWithNullMessage() {
        AikidoException exception = new AikidoException(null);

        assertEquals(null, exception.getMessage());
    }
}
