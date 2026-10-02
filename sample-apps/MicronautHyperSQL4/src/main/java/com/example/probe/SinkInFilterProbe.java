package com.example.probe;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static io.micronaut.http.annotation.ServerFilter.MATCH_ALL_PATTERN;

/** TEMP diagnostic: does a JDBC sink called from inside a @RequestFilter body get caught? */
@ServerFilter(MATCH_ALL_PATTERN)
public class SinkInFilterProbe {
    @RequestFilter
    public void onRequest(HttpRequest<?> request) {
        String token = request.getHeaders().get("X-Token");
        if (token == null) {
            return;
        }
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:sinkinfilter", "SA", "")) {
            Statement st = c.createStatement();
            st.execute("CREATE TABLE IF NOT EXISTS users (token VARCHAR(255))");
            st.executeQuery("SELECT * FROM users WHERE token = '" + token + "'");
        } catch (Exception e) {
            throw new RuntimeException("SINK_IN_FILTER_RESULT: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }
}
