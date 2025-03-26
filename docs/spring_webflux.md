# Setting up Spring Webflux

## Installation

1. Follow the normal installation instructions for the Java Agent.

2. Set your Aikido token as an environment variable:
```sh
AIKIDO_TOKEN="AIK_RUNTIME_YOUR_TOKEN_HERE"
```

You can get your token from the [Aikido Security Dashboard](https://help.aikido.dev/doc/creating-an-aikido-zen-firewall-token/doc6vRJNzC4u).

## Rate-limiting

Adding rate-limiting and user blocking capabilities to your Spring app requires you to run `ShouldBlockRequest.shouldBlockRequest()`.

To do this using a filter we have provided an example of what that might look like : 
```java
@Component
public class RatelimitingFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain webFilterChain) {

        // Check with Aikido if this request needs to be blocked
        ShouldBlockRequest.ShouldBlockRequestResult shouldBlockRequestResult = ShouldBlockRequest.shouldBlockRequest();
        if (shouldBlockRequestResult.block()) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            byte[] response = null;

            // Aikido Zen returns the specific reason why a request was blocked
            // Use this to show meaningful error messages to your users
            if (shouldBlockRequestResult.data().type().equals("ratelimited")) {
                String message = "You are rate limited by Zen.";
                if (shouldBlockRequestResult.data().trigger().equals("ip")) {
                    message = message + " (Your IP: " + shouldBlockRequestResult.data().ip() + ")";
                }
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS); // 429
                response = message.getBytes(StandardCharsets.UTF_8);
            } else if (shouldBlockRequestResult.data().type().equals("blocked")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN); // 403
                response = "You are blocked by Zen.".getBytes(StandardCharsets.UTF_8);
            }

            DataBufferFactory dataBufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap(response);
            return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        }

        return webFilterChain.filter(exchange);
    }
}
```


## Setting a user (optional)
If you want support for user-blocking or rate-limiting per user you will have to set your user. Do make sure that you run your SetUser filter before you run your rate-limiting filter.

To set the current user, you can use the `setUser` function. Here's an example :
```java
import dev.aikido.agent_api.SetUser;
// ...


@Component
@Order(1) // Make sure it's before the ratelimiting filter
public class SetUserFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange,
                             WebFilterChain webFilterChain) {
        SetUser.setUser(
                // Replace "123" with your own ID
                new SetUser.UserObject("123", "John Doe")
        );
        return webFilterChain.filter(serverWebExchange);
    }
}
```

Using `setUser` has the following benefits:

- The user ID is used for more accurate rate limiting (you can change IP addresses, but you can't change your user ID).
- Whenever attacks are detected, the user will be included in the report to Aikido.
- The dashboard will show all your users, where you can also block them.
- Passing the user's name is optional, but it can help you identify the user in the dashboard. You will be required to list Aikido Security as a subprocessor if you choose to share personal identifiable information (PII).
