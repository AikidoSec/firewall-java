# Troubleshooting

## Review installation steps

Double-check your setup against the [installation guide](../README.md#installation).  
Make sure:
- The package installed correctly.  
- The firewall is initialized early in your app (before routes or handlers).  
- Your framework-specific integration (middleware, decorator, etc.) matches the example in the README.  
- You’re running a supported runtime version for your language.

## Check connection to Aikido

The firewall must be able to reach Aikido’s API endpoints.

Test from the same environment where your app runs and follow the instructions on this page: https://help.aikido.dev/zen-firewall/miscellaneous/outbound-network-connections-for-zen

## Check logs for errors

Typical places:
- Docker: `docker logs <your-app-container>`
- systemd: `journalctl -u <your-app-service> --since "1 hour ago"`
- Local dev: your IDE or `stdout`

Tip: search for lines containing `Aikido` or `Zen` to spot initialization and request logs.

## Enable trace mode

Run your application with `AIKIDO_LOG_LEVEL="trace"` and check logs again.

## Contact support

If you still can’t resolve the issue:

- Use the in-app chat to reach our support team directly.
- Or create an issue on [GitHub](https://github.com/AikidoSec/firewall-java/issues) with details about your setup, framework, and logs.

Include as much context as possible (framework, logs, and how Aikido was added) so we can help you quickly.
