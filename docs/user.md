# Setting the current user

To set the current user, you can use the `setUser` function. Here's an example for Spring:

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

# Rate limiting groups

To limit the number of requests for a group of users, you can use the `setRateLimitGroup` function. For example, this is useful if you want to limit the number of requests per team or company.
Please note that if a rate limit group is set, the configured rate limits are only applied to the group and not to individual users or IP addresses.

```java
import dev.aikido.agent_api.SetRateLimitGroup;
// Replace "123" with your own group id.
SetRateLimitGroup.setRateLimitGroup("123");
```
