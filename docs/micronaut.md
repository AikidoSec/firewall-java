# Setting up Micronaut

> [!NOTE]
> Support currently covers **blocking** Micronaut controllers (the common case: controllers returning
> plain values, with JDBC / Micronaut Data JDBC). Fully **reactive** endpoints (controllers returning
> `Mono`/`Flux` with R2DBC) are a work in progress — see [Spring Webflux](spring_webflux.md) for the
> reactive story, which shares the same thread-hopping limitation.

To set up Zen with Micronaut you only have to follow the normal installation instructions for the
Java agent — add the `-javaagent` before `-jar`:

```
java -javaagent:/opt/zen/agent.jar -jar build/myapp.jar
```

Zen instruments your `@Controller` methods (annotated with `@Get`, `@Post`, `@Put`, `@Delete`,
`@Patch`). For each request it builds the request context (method, URL, IP, headers, cookies, query)
and reads user input from `@Body`, `@QueryValue`, `@PathVariable`, `@Part` and `@RequestBean`
parameters, so it can protect against SQL injection, command injection, path traversal and SSRF, and
apply IP blocking, bot blocking and rate limiting.

Because Zen sets the request context on the controller thread, protection works both for controllers
that run on the event loop and for blocking controllers offloaded with
`@ExecuteOn(TaskExecutors.BLOCKING)`.

## Rate limiting and user blocking

IP and bot blocking work automatically once the agent is installed. Rate limiting and user blocking
need a `@ServerFilter` that calls `ShouldBlockRequest.shouldBlockRequest()` — the same pattern as
`AikidoJavalinMiddleware` (Javalin) and Spring's `RateLimitingFilter`. Order it after your
user-setting filter so the user is available:

```java
import dev.aikido.agent_api.ShouldBlockRequest;
import io.micronaut.core.annotation.Order;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.exceptions.HttpStatusException;

import static io.micronaut.http.annotation.ServerFilter.MATCH_ALL_PATTERN;

@ServerFilter(MATCH_ALL_PATTERN)
@Order(100) // after your SetUser filter
public class AikidoRateLimitFilter {
    @RequestFilter
    public void filterRequest() {
        ShouldBlockRequest.ShouldBlockRequestResult result = ShouldBlockRequest.shouldBlockRequest();
        if (!result.block()) {
            return;
        }
        if ("ratelimited".equals(result.data().type())) {
            String message = "You are rate limited by Zen.";
            if ("ip".equals(result.data().trigger()) && result.data().ip() != null) {
                message += " (Your IP: " + result.data().ip() + ")";
            }
            throw new HttpStatusException(HttpStatus.TOO_MANY_REQUESTS, (Object) message);
        }
        throw new HttpStatusException(HttpStatus.FORBIDDEN, (Object) "You are blocked by Zen.");
    }
}
```

## Setting a user (optional)

To use user-based rate limiting or user blocking, set the current user early in the request. A
Micronaut `@ServerFilter` running before the controller is a good place:

```java
import dev.aikido.agent_api.SetUser;
import io.micronaut.core.annotation.Order;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;

import static io.micronaut.http.annotation.ServerFilter.MATCH_ALL_PATTERN;

@ServerFilter(MATCH_ALL_PATTERN)
@Order(10) // lower than AikidoRateLimitFilter, so it runs first and the user is set before the gate
public class SetUserFilter {
    @RequestFilter
    public void filterRequest(HttpRequest<?> request) {
        // Replace "123" with your own ID
        SetUser.setUser(new SetUser.UserObject("123", "John Doe"));
    }
}
```

Using `setUser` has the following benefits:

- The user ID is used for more accurate rate limiting (you can change IP addresses, but you can't
  change your user ID).
- Whenever attacks are detected, the user will be included in the report to Aikido.
- The dashboard will show all your users, where you can also block them.
- Passing the user's name is optional, but it can help you identify the user in the dashboard. You
  will be required to list Aikido Security as a subprocessor if you choose to share personal
  identifiable information (PII).
