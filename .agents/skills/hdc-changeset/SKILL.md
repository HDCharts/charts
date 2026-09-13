---
name: hdc-changeset
description: Create or update a release changeset for the current repository. Use when the user asks to create or update release notes for a user-facing change.
---

# Create release changesets

Scope: the current repository. Commit and push actions require an explicit user
request.

## User-impact gate

The changeset gate covers observable features, fixes, behavior changes, API
changes, and public documentation changes.

CI configuration, dependency updates with no behavior changes, internal
refactors, build cleanup, formatting, linting, repository maintenance, and
release-process documentation that does not affect users use the no-changeset
result.

For those changes, output exactly:

```text
No changeset needed.
```

## Workflow

1. Resolve the current pull request number, or use the explicit number provided
   by the user.
2. Read `.version`, require `<major>.<minor>.<patch>-SNAPSHOT`, and remove the
   suffix to obtain `release_version`.
3. Ensure these directories exist:

   ```text
   release-notes/<release_version>/changes/
   release-notes/<release_version>/migrations/
   ```

4. Create the changeset at:

   ```text
   release-notes/<release_version>/changes/<pr-number>-<short-kebab>.md
   ```

   Use [assets/pr-changeset.md](assets/pr-changeset.md) as the template.
5. Populate and validate the type, published module, pull request URL,
   release-note word count, and absence of template placeholders.
6. Report the release version and created or updated path.
