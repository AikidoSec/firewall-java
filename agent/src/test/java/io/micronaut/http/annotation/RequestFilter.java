package io.micronaut.http.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Test-only stand-in for the real @RequestFilter (Micronaut 4.3+ needs a newer JDK than this
// module targets). MicronautControllerWrapper matches by fully-qualified name, so same
// name+package is enough.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestFilter {
}
