package com.example.probe;

import io.micronaut.serde.annotation.Serdeable;

/** POJO body for @Body binding tests. */
@Serdeable
public record SearchRequest(String name) {
}
