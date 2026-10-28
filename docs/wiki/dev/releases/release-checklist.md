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
after the run started are not included.

Before retrying a failed Maven job or starting a new release, check its deployment in
[Maven Central](https://central.sonatype.com/). A timeout or missing public POM can mean that
Central is still publishing the accepted upload.

- **Deployment still active:** wait while it is processing or publishing. Resolve any deployment
  awaiting manual publication before starting another. Keep the approved source and existing
  API/demo assets unchanged while publication is unresolved.
- **Any release artifacts already published:** preserve the version and approved source SHA.
  Follow [Release Recovery](release-recovery.md) if the Maven job failed. Rerunning that job can
  upload immutable artifacts again.
- **No release artifacts published and no deployment can still publish them:** for an external
  failure, such as an outage or expired secret, rerun the failed job. If the fix needs a new
  commit, merge it, wait for its snapshot, and run a new **Release** with `replace_static_assets`
  enabled when existing pre-release assets need replacement.

If a later job fails after its prerequisites succeed, rerun that later job separately once its
cause is resolved. Check that the rerun can safely repeat any steps that already succeeded.
