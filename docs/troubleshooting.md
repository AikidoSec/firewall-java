# Troubleshooting

## Check logs for errors

Typical places:
- Docker: `docker logs <your-app-container>`
- systemd: `journalctl -u <your-app-service> --since "1 hour ago"`
- Local dev: your IDE or `stdout`

Tip: search for lines containing `Aikido` or `Zen` to spot initialization and request logs.

**Spring Boot**
To increase verbosity during troubleshooting, add temporarily to `application.properties`:

`logging.level.root=INFO`

optionally narrow to the Aikido package if you use it `logging.level.com.yourorg.aikido=INFO`

## Confirm the dependency is present

**Maven**

```
grep -i “aikido” pom.xml
mvn -q dependency:tree | grep -i “aikido”
```

**Gradle**
```
grep -i “aikido” build.gradle*
./gradlew dependencies | grep -i “aikido”
```

## Contact support

If you still can’t resolve the issue:

- Use the in-app chat to reach our support team directly.
- Or create an issue on [GitHub](../../issues) with details about your setup, framework, and logs.

Include as much context as possible (framework, logs, and how Aikido was added) so we can help you quickly.
