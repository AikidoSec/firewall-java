# Troubleshooting

## Check logs for errors

Typical places:
- Docker: `docker logs <your-app-container>`
- systemd: `journalctl -u <your-app-service> --since "1 hour ago"`
- Local dev: your IDE or `stdout`

Tip: search for lines containing `Aikido` or `Zen` to spot initialization and request logs.

## Contact support

If you still can’t resolve the issue:

- Use the in-app chat to reach our support team directly.
- Or create an issue on [GitHub](../../issues) with details about your setup, framework, and logs.

Include as much context as possible (framework, logs, and how Aikido was added) so we can help you quickly.
