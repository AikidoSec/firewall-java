# Setting up Javalin

To setup Zen for your Javalin app you first have to follow the normal installation instructions for our Java Agent.

## Rate-limiting

To add rate-limiting and (user) blocking capabilities to your Javalin app, you will have to import our middleware for
Javalin :

```java
import dev.aikido.agent_api.middleware.AikidoJavalinMiddleware;
```

And once you have created your app add it (if you are setting a user, after you've set your user.)

```java
app.before(new AikidoJavalinMiddleware());
```

## Setting a user

If you want support for user-blocking or rate-limiting per user you will have to set your user.
Please ensure that your SetUser handler runs before your Aikido Middleare.

To set the current user, you can use the `setUser` function. Here's an example handler :

```java
import dev.aikido.agent_api.SetUser;
// ...

public class SetUserHandler implements Handler {
    @Override
    public void handle(Context ctx) throws Exception {
        setUser(new SetUser.UserObject("your-id-here", "John Doe"));
    }
}
```

Using `setUser` has the following benefits:

- The user ID is used for more accurate rate limiting (you can change IP addresses, but you can't change your user ID).
- Whenever attacks are detected, the user will be included in the report to Aikido.
- The dashboard will show all your users, where you can also block them.
- Passing the user's name is optional, but it can help you identify the user in the dashboard. You will be required to
  list Aikido Security as a subprocessor if you choose to share personal identifiable information (PII).
