package com.example.probe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadLog {
    private static final Logger LOG = LoggerFactory.getLogger("PROBE");
    private ThreadLog() {}

    public static void log(String id, String stage) {
        String ctx = ProbeContext.CTX.get();
        LOG.info("id={} | {} | thread={} | zenCtx={}", id, stage,
                Thread.currentThread().getName(),
                ctx == null ? "NULL (context lost!)" : (ctx.equals(id) ? "ok(" + ctx + ")" : "WRONG(" + ctx + ")"));
    }
}
