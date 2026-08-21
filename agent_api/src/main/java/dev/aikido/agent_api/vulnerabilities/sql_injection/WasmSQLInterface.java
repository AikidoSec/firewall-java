package dev.aikido.agent_api.vulnerabilities.sql_injection;

import com.dylibso.chicory.compiler.InterpreterFallback;
import com.dylibso.chicory.compiler.MachineFactoryCompiler;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Machine;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.HexFormat;
import java.util.Set;
import java.util.function.Function;

public final class WasmSQLInterface {
    private static final Logger logger = LogManager.getLogger(WasmSQLInterface.class);
    private static final String WASM_RESOURCE = "zen_internals.wasm";
    private static final String CHECKSUM_RESOURCE = "zen_internals.wasm.sha256sum";
    private static final int MAX_IDLE_INSTANCES = 10;
    // These functions exceed Java's 65,535-byte method limit, so run them in the interpreter and fail if any others cannot compile.
    private static final Set<Integer> INTERPRETED_FUNCTIONS = Set.of(865, 3165);

    private static volatile RuntimeState runtime;
    private static volatile Throwable initializationFailure;

    private WasmSQLInterface() {}

    public static boolean initialize() {
        try {
            getRuntime();
            return true;
        } catch (Throwable e) {
            logger.error("Failed to initialize zen-internals WASM: %s", e.getMessage());
            logger.trace(e);
            return false;
        }
    }

    public static boolean detectSqlInjection(String query, String userInput, Dialect dialect) {
        RuntimeState current;
        WasmInstance instance;
        try {
            current = getRuntime();
            instance = current.pool.acquire();
        } catch (Throwable e) {
            logger.trace(e);
            // If it cannot initialize or create an instance, let the query proceed.
            return false;
        }

        boolean reusable = false;
        try {
            int result = instance.detect(query, userInput, dialect.getDialectInteger());
            reusable = true;
            return result == 1;
        } catch (Throwable e) {
            logger.trace(e);
            // If zen-internals fails while checking the query, let the query proceed.
            return false;
        } finally {
            if (reusable) {
                current.pool.release(instance);
            }
        }
    }

    private static RuntimeState getRuntime() {
        RuntimeState current = runtime;
        if (current != null) {
            return current;
        }
        Throwable failure = initializationFailure;
        if (failure != null) {
            throw new IllegalStateException("zen-internals WASM initialization failed", failure);
        }

        synchronized (WasmSQLInterface.class) {
            current = runtime;
            if (current != null) {
                return current;
            }
            failure = initializationFailure;
            if (failure != null) {
                throw new IllegalStateException("zen-internals WASM initialization failed", failure);
            }
            try {
                current = createRuntime();
                runtime = current;
                return current;
            } catch (Throwable e) {
                initializationFailure = e;
                throw new IllegalStateException("zen-internals WASM initialization failed", e);
            }
        }
    }

    private static RuntimeState createRuntime() throws IOException {
        byte[] wasm = readResource(WASM_RESOURCE);
        String[] checksumFields = new String(readResource(CHECKSUM_RESOURCE), StandardCharsets.UTF_8)
                .trim()
                .split("\\s+", 2);
        if (checksumFields[0].isEmpty()) {
            throw new IllegalStateException("invalid zen-internals WASM checksum");
        }
        String expectedChecksum = checksumFields[0];
        String actualChecksum = sha256(wasm);
        if (!actualChecksum.equals(expectedChecksum)) {
            throw new IllegalStateException(
                    "zen-internals WASM checksum mismatch: expected " + expectedChecksum
                            + ", got " + actualChecksum);
        }

        WasmModule module = Parser.parse(wasm);
        Function<Instance, Machine> machineFactory =
                MachineFactoryCompiler.builder(module)
                        .withInterpreterFallback(InterpreterFallback.FAIL)
                        .withInterpretedFunctions(INTERPRETED_FUNCTIONS)
                        .compile();
        return new RuntimeState(module, machineFactory);
    }

    private static byte[] readResource(String name) throws IOException {
        ClassLoader classLoader = WasmSQLInterface.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IOException("resource not found: " + name);
            }
            return stream.readAllBytes();
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError("SHA-256 is required by every supported JDK", e);
        }
    }

    private static final class RuntimeState {
        private final WasmModule module;
        private final Function<Instance, Machine> machineFactory;
        private final InstancePool pool;

        private RuntimeState(WasmModule module, Function<Instance, Machine> machineFactory) {
            this.module = module;
            this.machineFactory = machineFactory;
            this.pool = new InstancePool(this);
        }

        private WasmInstance newInstance() {
            Instance instance = Instance.builder(module)
                    .withMachineFactory(machineFactory)
                    .build();
            return new WasmInstance(instance);
        }
    }

    private static final class InstancePool {
        private final RuntimeState runtimeState;
        private final ArrayDeque<WasmInstance> idleInstances = new ArrayDeque<>();

        private InstancePool(RuntimeState runtimeState) {
            this.runtimeState = runtimeState;
        }

        private synchronized WasmInstance acquire() {
            WasmInstance instance = idleInstances.pollLast();
            return instance != null ? instance : runtimeState.newInstance();
        }

        private synchronized void release(WasmInstance instance) {
            if (idleInstances.size() < MAX_IDLE_INSTANCES) {
                idleInstances.addLast(instance);
            }
        }
    }

}
