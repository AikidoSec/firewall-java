package com.example.probe;

/** Mimics Zen's Context: a plain ThreadLocal set at inbound entry, read at the sink. */
public final class ProbeContext {
    public static final ThreadLocal<String> CTX = new ThreadLocal<>();
    private ProbeContext() {}
}
