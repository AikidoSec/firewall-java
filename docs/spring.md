# Setting up Spring
> Currently we only offer support for **Spring MVC**, support for Spring Webflux is in the pipeline.

To setup with Spring you just have to follow the normal installation instructions for the Java Agent.
## Rate-limiting
Adding rate-limiting and user blocking capabilities to your Spring app requires you to run `ShouldBlockRequest.shouldBlockRequest()`.

To do this using a filter we have provided an example of what that might look like : 
```java
@Component
@Order(2)
public class RateLimitingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        ShouldBlockRequest.ShouldBlockRequestResult shouldBlockRequestResult = ShouldBlockRequest.shouldBlockRequest();
        if (shouldBlockRequestResult.block()) {
            if (shouldBlockRequestResult.data().type().equals("ratelimited")) {
                String message = "You are rate limited by Zen.";
                if (shouldBlockRequestResult.data().trigger().equals("ip")) {
                    message = message + " (Your IP: " + shouldBlockRequestResult.data().ip() + ")";
                }
                setResponse(response, message, 429);
            } else if (shouldBlockRequestResult.data().type().equals("blocked")) {
                setResponse(response, "You are blocked by Zen.", 403);
            }
            return;
        }
        chain.doFilter(request, response);
    }
    private static void setResponse(ServletResponse response, String text, int statusCode) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(statusCode);
        httpResponse.setContentType("text/plain");
        PrintWriter writer = httpResponse.getWriter();
        writer.write(text);
        writer.flush();
    }
}
```
We are planning on providing such a filter ourselves for easy plug & play use, once we release our maven package.

## Setting a user
If you want support for user-blocking or rate-limiting per user you will have to set your user. Do make sure that you run your SetUser filter before you run your rate-limiting filter.

To set the current user, you can use the `setUser` function. Here's an example :
```java
import dev.aikido.agent_api.SetUser;
// ...

@Component
@Order(0) // Depends on your setup
public class SetUserFilter implements Filter {
    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        SetUser.setUser(
                // Replace "123" with your own ID
                new SetUser.UserObject("123", "John Doe")
        );
        chain.doFilter(request, response);
    }
}
```

Using `setUser` has the following benefits:

- The user ID is used for more accurate rate limiting (you can change IP addresses, but you can't change your user ID).
- Whenever attacks are detected, the user will be included in the report to Aikido.
- The dashboard will show all your users, where you can also block them.
- Passing the user's name is optional, but it can help you identify the user in the dashboard. You will be required to list Aikido Security as a subprocessor if you choose to share personal identifiable information (PII).
