# Tracking events

`track` lets you record things happening in your app — like failed logins, signups, or password resets. Zen sends these to Aikido so patterns can be detected, like someone failing to log in 50 times in a minute.

```java
import dev.aikido.agent_api.Track;
import dev.aikido.agent_api.SetUser;

public void login(HttpServletRequest request) {
    User user = authenticate(request);

    if (user == null) {
        Track.track("user.login_failed");
        throw new UnauthorizedException();
    }

    SetUser.setUser(new SetUser.UserObject(user.getId(), user.getName()));
    Track.track("user.login_succeeded");
}
```

Zen automatically picks up the IP address, user agent, and current user (if you called [`setUser`](./user.md)) from the request — you don't need to pass those yourself.

## More examples

```java
Track.track("user.signed_up");
Track.track("user.password_reset_requested");
Track.track("plan.invite_sent");
Track.track("payment.failed");
```

## Naming events

Use lowercase with dots to group related events:

- `user.login_failed`
- `user.login_succeeded`
- `user.signed_up`
- `user.password_reset_requested`
- `payment.failed`
- `plan.invite_sent`

## Things to know

`track` only works inside an HTTP request. If you call it in a background job or outside of a request, nothing gets sent and you'll see a warning in the console.

If you haven't called `setUser` yet, the event still goes through — it just won't have a user attached.
