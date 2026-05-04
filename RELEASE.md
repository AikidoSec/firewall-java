# Releasing

1. Create and push a tag for the version you want to release (e.g. `git tag v1.2.3 && git push origin v1.2.3`) — this triggers the release workflow
2. The workflow builds the artifacts and uploads them to a **draft** release on GitHub
3. Go to **GitHub → Releases**, review the draft, then click **Edit → Publish release** to make it public
