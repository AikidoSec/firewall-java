package com.example.probe;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.annotation.QueryValue;

/** Aggregated bean for @RequestBean binding tests. */
@Introspected
public record SearchBean(@QueryValue(value = "name", defaultValue = "") String name) {
}
