---
title: Release Checklist
order: 5
---

# Release Checklist

1. Merge every change meant for the release.
2. Check that the latest snapshot was published from the current `main` commit. If not, run
   **Snapshot Release** manually.
3. Run **Release** from `main`.
4. Approve the `Release Approval` environment.

[Release](release.md) explains what the workflow checks and publishes.

## When a run fails

**Re-run failed jobs** uses the original commit and workflow files. Commits pushed to `main`
after the run started are not included. Use it when the cause is outside this repository, such as
an outage, an expired secret, or `charts-docs` state.

When the fix needs a new commit, first check whether the version is on
[Maven Central](https://central.sonatype.com/). The Maven step can fail after the version is
already published.

- **Not on Maven Central:** merge the fix, wait for its snapshot, and run a new **Release** with
  `replace_static_assets` enabled.
- **On Maven Central:** the version is final. Finish the remaining steps by hand and ship the fix
  in the next version.

If only the tag push failed, tag the commit shown in the **Approve Release** summary:

```bash
git tag <version> <source-sha>
git push origin <version>
```

The later jobs stay skipped, so finish the Android build, docs promotion, and GitHub release by
hand.
