# Track custom events

Use `Track.track(...)` to report events that only your application knows about, such as failed logins. [Playbooks](https://help.aikido.dev/zen-firewall/zen-features/playbooks) can act when an event occurs repeatedly, for example by blocking an IP after three failed logins in five minutes.

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

Call `Track.track(...)` while handling an HTTP request. Zen associates the event with the request's IP address. Playbook counts are per IP, not across your whole app. If you call [`setUser`](./user.md) before `Track.track(...)`, Zen also includes the current user. Calling `setUser` is optional — events without a user are still tracked.

> [!NOTE]
> `Track.track(...)` doesn't support Spring WebFlux yet.

Event names can use any format. We recommend lowercase, dot-separated names such as `user.login_failed`.
