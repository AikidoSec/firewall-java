package dev.aikido.agent_api.vulnerabilities.sql_injection;

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Memory;

import java.nio.charset.StandardCharsets;

final class WasmInstance {
    private final ExportFunction alloc;
    private final ExportFunction free;
    private final ExportFunction detectSql;
    private final Memory memory;

    WasmInstance(Instance instance) {
        alloc = instance.export("wasm_alloc");
        free = instance.export("wasm_free");
        detectSql = instance.export("detect_sql_injection");
        memory = instance.memory();
    }

    int detect(String query, String userInput, int dialect) {
        byte[] queryBytes = query == null
                ? new byte[0]
                : query.getBytes(StandardCharsets.UTF_8);
        byte[] userInputBytes = userInput == null
                ? new byte[0]
                : userInput.getBytes(StandardCharsets.UTF_8);
        try (Allocation queryAllocation = allocate(queryBytes.length);
                Allocation userInputAllocation = allocate(userInputBytes.length)) {
            memory.write(queryAllocation.pointer, queryBytes);
            memory.write(userInputAllocation.pointer, userInputBytes);
            long[] result = detectSql.apply(
                    Integer.toUnsignedLong(queryAllocation.pointer),
                    queryBytes.length,
                    Integer.toUnsignedLong(userInputAllocation.pointer),
                    userInputBytes.length,
                    dialect
            );
            if (result.length == 0) {
                throw new IllegalStateException("detect_sql_injection returned no result");
            }
            return Math.toIntExact(result[0]);
        }
    }

    private Allocation allocate(int length) {
        long[] result = alloc.apply(length);
        if (result.length == 0 || result[0] > 0xffff_ffffL) {
            throw new IllegalStateException("invalid WASM allocation result");
        }
        return new Allocation((int) result[0], length);
    }

    private final class Allocation implements AutoCloseable {
        private final int pointer;
        private final int length;

        private Allocation(int pointer, int length) {
            this.pointer = pointer;
            this.length = length;
        }

        @Override
        public void close() {
            free.apply(Integer.toUnsignedLong(pointer), length);
        }
    }
}
