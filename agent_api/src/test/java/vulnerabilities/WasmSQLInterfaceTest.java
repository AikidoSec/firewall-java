package vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.sql_injection.Dialect;
import dev.aikido.agent_api.vulnerabilities.sql_injection.WasmSQLInterface;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WasmSQLInterfaceTest {
    @Test
    public void detectsSqlInjection() {
        Dialect postgresql = new Dialect("postgresql");
        assertTrue(WasmSQLInterface.initialize());

        assertTrue(
                WasmSQLInterface.detectSqlInjection(
                        "SELECT * FROM users WHERE id = '1' OR 1=1",
                        "1' OR 1=1",
                        postgresql
                )
        );
        assertFalse(
                WasmSQLInterface.detectSqlInjection(
                        "SELECT * FROM users WHERE id = '1'",
                        "1",
                        postgresql
                )
        );
    }

    @Test
    public void supportsConcurrentCallsWithIndependentInstances() throws Exception {
        assertTrue(WasmSQLInterface.initialize());
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> calls = new ArrayList<>();
            for (int i = 0; i < 64; i++) {
                calls.add(() -> WasmSQLInterface.detectSqlInjection(
                        "SELECT * FROM users WHERE id = '1' OR 1=1",
                        "1' OR 1=1",
                        new Dialect("mysql")));
            }
            for (Future<Boolean> call : executor.invokeAll(calls)) {
                assertTrue(get(call));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static boolean get(Future<Boolean> call) throws ExecutionException, InterruptedException {
        return call.get();
    }
}
