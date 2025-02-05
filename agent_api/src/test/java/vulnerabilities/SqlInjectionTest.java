package vulnerabilities;

import dev.aikido.agent_api.vulnerabilities.sql_injection.Dialect;
import dev.aikido.agent_api.vulnerabilities.sql_injection.SqlDetector;
import org.junit.jupiter.api.Test;

import static dev.aikido.agent_api.vulnerabilities.sql_injection.SqlDetector.detectSqlInjection;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlInjectionTest {
    private void isNotSqlInjection(String sql, String input, String dialect) {
        boolean result;
        if ("mysql".equals(dialect) || "all".equals(dialect)) {
            result = detectSqlInjection(sql, input, new Dialect("mysql"));
            assertFalse(result, String.format("Expected no SQL injection for SQL: %s and input: %s", sql, input));
        }
        if ("postgresql".equals(dialect) || "all".equals(dialect)) {
            result = detectSqlInjection(sql, input, new Dialect("postgresql"));
            assertFalse(result, String.format("Expected no SQL injection for SQL: %s and input: %s", sql, input));
        }
    }
    private void isSqlInjection(String sql, String input, String dialect) {
        boolean result;
        if ("mysql".equals(dialect) || "all".equals(dialect)) {
            result = detectSqlInjection(sql, input, new Dialect("mysql"));
            assertTrue(result, String.format("Expected SQL injection for SQL: %s and input: %s", sql, input));
        }
        if ("postgresql".equals(dialect) || "all".equals(dialect)) {
            result = detectSqlInjection(sql, input, new Dialect("postgresql"));
            assertTrue(result, String.format("Expected SQL injection for SQL: %s and input: %s", sql, input));
        }
    }


    /**
     * Removed tests :
     * -> `I'm writting you` : Invalid SQL
     * -> Moved a lot of the keywords/words together collection to BAD_SQL_COMMANDS.
     * -> Removed the following GOOD_SQL_COMMANDS : "abcdefghijklmnop@hotmail.com", "steve@yahoo.com"
     *     Reason : This should never occur unencapsulated in query, results in 5 tokens or so being morphed into one.
      */
    private static final String[] BAD_SQL_COMMANDS = {
            "Roses are red insErt are blue",
            "Roses are red cREATE are blue",
            "Roses are red drop are blue",
            "Roses are red updatE are blue",
            "Roses are red SELECT are blue",
            "Roses are red dataBASE are blue",
            "Roses are red alter are blue",
            "Roses are red grant are blue",
            "Roses are red savepoint are blue",
            "Roses are red commit are blue",
            "Roses are red or blue",
            "Roses are red and lovely",
            "This is a group_concat_test",
            "Termin;ate",
            "Roses <> violets",
            "Roses < Violets",
            "Roses > Violets",
            "Roses != Violets",
            "Roses asks red truncates asks blue",
            "Roses asks reddelete asks blue",
            "Roses asks red WHEREis blue",
            "Roses asks red ORis isAND",
            "I was benchmark ing",
            "We were delay ed",
            "I will waitfor you"
    };

    // List of good SQL commands that should not be flagged as SQL injection
    private static final String[] GOOD_SQL_COMMANDS = {
            "              ",
            "#",
            "'"
    };

    /*
    List of SQL commands that are not considered injections
    Moved ["'union'  is not UNION", "UNION"], to IS_NOT_INJECTION : This is in fact, not an injection.
     */
    private static final String[][] IS_NOT_INJECTION = {
            {"'UNION 123' UNION \"UNION 123\"", "UNION 123"},
            {"'union'  is not \"UNION\"", "UNION!"},
            {"\"UNION;\"", "UNION;"},
            {"SELECT * FROM table", "*"},
            {"\"COPY/*\"", "COPY/*"},
            {"'union'  is not \"UNION--\"", "UNION--"},
            {"'union'  is not UNION", "UNION"}
    };

    // List of SQL commands that are considered injections
    private static final String[][] IS_INJECTION = {
            {"UNTER;", "UNTER;"}
    };
    @Test
    public void testBadSqlCommands() {
        for (String sql : BAD_SQL_COMMANDS) {
            isSqlInjection(sql, sql, "all");
        }
    }
    @Test
    public void testGoodSqlCommands() {
        for (String sql : GOOD_SQL_COMMANDS) {
            isNotSqlInjection(sql, sql, "all");
        }
    }
    @Test
    public void testIsInjection() {
        for (String[] sqlPair : IS_INJECTION) {
            isSqlInjection(sqlPair[0], sqlPair[1], "all");
        }
    }
    @Test
    public void testIsNotInjection() {
        for (String[] sqlPair : IS_NOT_INJECTION) {
            isNotSqlInjection(sqlPair[0], sqlPair[1], "all");
        }
    }

    @Test
    public void testShouldReturnEarly() {
        // Test cases where the function should return True

        // User input is empty
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", ""));

        // User input is a single char"postgresql"acter
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "a"));

        // User input is larger than query
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "SELECT * FROM users WHERE id = 1"));

        // User input not in query
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "DELETE"));

        // User input is alphanumerical
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users123", "users123"));
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users_123", "users_123"));
        assertTrue(SqlDetector.shouldReturnEarly("SELECT __1 FROM users_123", "__1"));
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM table_name_is_fun_12", "table_name_is_fun_12"));

        // User input is a valid comma-separated number list
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "1,2,3"));

        // User input is a valid number
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "123"));

        // User input is a valid number with spaces
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "  123  "));

        // User input is a valid number with commas
        assertTrue(SqlDetector.shouldReturnEarly("SELECT * FROM users", "1, 2, 3"));

        // Test cases where the function should return False

        // User input is in query
        assertFalse(SqlDetector.shouldReturnEarly("SELECT * FROM users", " users"));

        // User input is a valid string in query
        assertFalse(SqlDetector.shouldReturnEarly("SELECT * FROM users", "SELECT "));

        // User input is a valid string in query with special characters
        assertFalse(SqlDetector.shouldReturnEarly("SELECT * FROM users; DROP TABLE", "users; DROP TABLE"));
    }

    /**
     * Moved :
     * is_sql_injection("SELECT * FROM users WHERE id = 'users\\'", "users\\")
     * is_sql_injection("SELECT * FROM users WHERE id = 'users\\\\'", "users\\\\")
     * to is_not_sql_injection. Reason : Invalid SQL.
     */
    @Test
    public void testAllowEscapeSequences() {
        // Invalid queries:
        isNotSqlInjection("SELECT * FROM users WHERE id = 'users\\'", "users\\", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id = 'users\\\\'", "users\\\\", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id = '\nusers'", "\nusers", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id = '\rusers'", "\rusers", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id = '\tusers'", "\tusers", "all");
    }

    /**
     * Marked "SELECT * FROM users WHERE id IN ('123')", "'123'" as not a sql injection, Reason :
     * We replace '123' token with another token and the token count remains the same. This is also
     * Not an actual SQL Injection so the algorithm is valid in it's reasoning.
     */
    @Test
    public void testUserInputInsideIn() {
        isNotSqlInjection("SELECT * FROM users WHERE id IN ('123')", "'123'", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id IN (123)", "123", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id IN (123, 456)", "123", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id IN (123, 456)", "456", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id IN ('123')", "123", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id IN (13,14,15)", "13,14,15", "all");
        isNotSqlInjection("SELECT * FROM users WHERE id IN (13, 14, 154)", "13, 14, 154", "all");

        // Invalid query that should be detected as SQL injection:
        isSqlInjection("SELECT * FROM users WHERE id IN (13, 14, 154) OR (1=1)", "13, 14, 154) OR (1=1", "all");
    }
    @Test
    public void testCheckStringSafelyEscaped() {
        // Invalid queries that should be detected as SQL injection:
        isSqlInjection(
                "SELECT * FROM comments WHERE comment = \"I\" \"m writing you\"",
                "I\" \"m writing you", "all"
        );
        isSqlInjection("SELECT * FROM `comm`ents``", "`comm`ents", "all");

        // Valid queries that should not be detected as SQL injection:
        isNotSqlInjection(
                "SELECT * FROM comments WHERE comment = \"I\\'m writing you\"", "I'm writing you", "all"
        );
        isNotSqlInjection(
                "SELECT * FROM comments WHERE comment = 'I\"m writing you'", "I\"m writing you", "all"
        );
        isNotSqlInjection(
                "SELECT * FROM comments WHERE comment = \"I\\`m writing you\"", "I`m writing you", "all"
        );

        // Invalid query (strings don't terminate)
        isNotSqlInjection(
                "SELECT * FROM comments WHERE comment = 'I'm writing you'", "I'm writing you", "all"
        );

        // Positive example of same query:
        isSqlInjection(
                "SELECT * FROM comments WHERE comment = 'I'm writing you--'",
                "I'm writing you--", "all"
        );
        isSqlInjection(
                "SELECT * FROM comments WHERE comment = 'I'm writing you''",
                "I'm writing you'", "all"
        );

        // Invalid query in postgres, tests fallback:
        isSqlInjection("SELECT * FROM `comm` ents", "`comm` ents", "postgresql");

        // MySQL Specific code:
        isSqlInjection("SELECT * FROM `comm` ents", "`comm` ents", "mysql");
        isNotSqlInjection("SELECT * FROM `comm'ents`", "comm'ents", "mysql");
    }

    @Test
    public void testNotFlagSelectQueries() {
        isNotSqlInjection("SELECT * FROM users WHERE id = 1", "SELECT", "all");
    }

    @Test
    public void testNotFlagEscapedHash() {
        isNotSqlInjection("SELECT * FROM hashtags WHERE name = '#hashtag'", "#hashtag", "all");
    }

    @Test
    public void testCommentSameAsUserInput() {
        isSqlInjection(
                "SELECT * FROM hashtags WHERE name = '-- Query by name' -- Query by name",
                "-- Query by name", "all"
        );
    }

    @Test
    public void testInputOccursInComment() {
        isNotSqlInjection(
                "SELECT * FROM hashtags WHERE name = 'name' -- Query by name", "name", "all"
        );
    }

    @Test
    public void testUserInputIsMultiline() {
        isSqlInjection("SELECT * FROM users WHERE id = 'a'\nOR 1=1#'", "a'\nOR 1=1#", "mysql");
        isNotSqlInjection("SELECT * FROM users WHERE id = 'a\nb\nc';", "a\nb\nc", "all");
    }

    @Test
    public void testUserInputIsLongerThanQuery() {
        isNotSqlInjection("SELECT * FROM users", "SELECT * FROM users WHERE id = 'a'", "all");
    }
    @Test
    public void testMultilineQueries() {
        isSqlInjection(
                """
                SELECT * FROM `users`
                WHERE id = 123
                """,
                "users`", "all"
        );

        isSqlInjection(
                """
                SELECT *
                FROM users
                WHERE id = '1' OR 1=1
                """,
                "1' OR 1=1", "all"
        );

        isSqlInjection(
                """
                SELECT *
                FROM users
                WHERE id = '1' OR 1=1
                AND is_escaped = '1'' OR 1=1'
                """,
                "1' OR 1=1", "all"
        );

        isSqlInjection(
                """
                SELECT *
                FROM users
                WHERE id = '1' OR 1=1
                AND is_escaped = "1' OR 1=1"
                """,
                "1' OR 1=1", "all"
        );

        isNotSqlInjection(
                """
                SELECT * FROM `users`
                WHERE id = 123
                """,
                "123", "all"
        );

        isNotSqlInjection(
                """
                SELECT * FROM `us``ers`
                WHERE id = 123
                """,
                "users", "all"
        );

        isNotSqlInjection(
                """
                SELECT * FROM users
                WHERE id = 123
                """,
                "123", "all"
        );

        isNotSqlInjection(
                """
                SELECT * FROM users
                WHERE id = '123'
                """,
                "123", "all"
        );

        isNotSqlInjection(
                """
                SELECT *
                FROM users
                WHERE is_escaped = "1' OR 1=1"
                """,
                "1' OR 1=1", "all"
        );
    }
    @Test
    public void testLowercasedInputSqlInjection() {
        String sql = """
        SELECT id,
               email,
               password_hash,
               registered_at,
               is_confirmed,
               first_name,
               last_name
        FROM users WHERE email_lowercase = '' or 1=1 -- a'
    """;
        String expectedSqlInjection = "' OR 1=1 -- a";

        isSqlInjection(sql, expectedSqlInjection, "all");
    }

    /**
     * Marked the following as SQL injection since this would result in 2 or more tokens becoming one :
     * is_not_sql_injection("foobar)", "foobar)")
     * is_not_sql_injection("foobar      )", "foobar      )")
     * is_not_sql_injection("€foobar()", "€foobar()")
     */
    @Test
    public void testFunctionCallsAsSqlInjections() {
        isSqlInjection("foobar()", "foobar()", "all");
        isSqlInjection("foobar(1234567)", "foobar(1234567)", "all");
        isSqlInjection("foobar       ()", "foobar       ()", "all");
        isSqlInjection(".foobar()", ".foobar()", "all");
        isSqlInjection("20+foobar()", "20+foobar()", "all");
        isSqlInjection("20-foobar(", "20-foobar(", "all");
        isSqlInjection("20<foobar()", "20<foobar()", "all");
        isSqlInjection("20*foobar  ()", "20*foobar  ()", "all");
        isSqlInjection("!foobar()", "!foobar()", "all");
        isSqlInjection("=foobar()", "=foobar()", "all");
        isSqlInjection("1foobar()", "1foobar()", "all");
        isSqlInjection("1foo_bar()", "1foo_bar()", "all");
        isSqlInjection("1foo-bar()", "1foo-bar()", "all");
        isSqlInjection("#foobar()", "#foobar()", "all");
        isSqlInjection("foobar)", "foobar)", "all");
        isSqlInjection("foobar      )", "foobar      )", "all");
        isSqlInjection("€foobar()", "€foobar()", "all");
    }

}
