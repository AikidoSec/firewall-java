# Tracking events

`track` lets you record things happening in your app — like failed logins, signups, or password resets. [Playbooks](https://help.aikido.dev/zen-firewall/zen-features/playbooks) can act when an event occurs repeatedly, for example by blocking an IP after three failed logins in five minutes.

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

After adding `Track.track(...)`, trigger the event at least once. It will then appear on the Playbooks page in the Aikido dashboard. From there, you can create a playbook and choose what should happen when the event occurs. Calling `Track.track(...)` by itself does not create a playbook or block anything.

Call `Track.track(...)` while handling an HTTP request. Zen associates the event with the request's IP address, and playbook counts are per IP, not across your whole app. Zen also picks up the user agent and current user (if you called [`setUser`](./user.md)) — you don't need to pass those yourself. Calling `setUser` is optional; events without a user are still tracked.

## More examples

```java
Track.track("user.signed_up");
Track.track("user.password_reset_requested");
Track.track("plan.invite_sent");
Track.track("payment.failed");
```

## Naming events

Event names can use any format. We recommend lowercase, dot-separated names to group related events, such as:

- `user.login_failed`
- `user.login_succeeded`
- `user.signed_up`
- `user.password_reset_requested`
- `payment.failed`
- `plan.invite_sent`

## Things to know

`track` only works inside an HTTP request. If you call it in a background job or outside of a request, nothing gets sent and you'll see a warning in the console.

Each request accepts up to 25 tracked events. Any further calls within the same request are dropped and logged as a warning.
