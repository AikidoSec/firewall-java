# Middleware
This folder contains the rate-limting/blocking middleware per platform. It uses the `shouldBlockRequest` function,
which you can also use yourself in your own middleware. But this makes install a bit easier.

## Currently available : 
- [`AikidoJavalinMiddleware`](./AikidoJavalinMiddleware.java) With an easy install : 
```java
import dev.aikido.agent_api.middleware.AikidoJavalinMiddleware;

// ...

app.before(new SetUserHandler()); // This is the handler that sets a user, optional.
app.before(new AikidoJavalinMiddleware());
```