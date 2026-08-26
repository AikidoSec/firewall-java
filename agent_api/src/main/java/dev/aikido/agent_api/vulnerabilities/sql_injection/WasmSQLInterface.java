package dev.aikido.agent_api.vulnerabilities.sql_injection;

import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Machine;
import com.dylibso.chicory.wasm.WasmModule;
import dev.aikido.agent_api.helpers.logging.LogManager;
import dev.aikido.agent_api.helpers.logging.Logger;
import dev.aikido.agent_api.vulnerabilities.sql_injection.generated.ZenInternals;

import java.util.ArrayDeque;
import java.util.function.Function;

public final class WasmSQLInterface {
    private static final Logger logger = LogManager.getLogger(WasmSQLInterface.class);
    private static final int MAX_IDLE_INSTANCES = 10;

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

    private static RuntimeState createRuntime() {
        WasmModule module = ZenInternals.load();
        return new RuntimeState(module, ZenInternals::create);
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
