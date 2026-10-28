---
title: Release Recovery
order: 7
---

# Release Recovery

Recover a release when Maven Central publishes artifacts but the release workflow fails.
Keep the published version, approved source SHA, and existing API/demo assets intact.

## Establish the publication state

1. Record the version and full source SHA from the failed run's **Approve Release** summary.
   Every recovery job must use that source.
2. Check each deployment in [Maven Central](https://central.sonatype.com/). Wait while it is
   `PENDING`, `VALIDATING`, or `PUBLISHING`. For `VALIDATED`, inspect the existing bundle in the
   Portal: publish it if it matches the approved release, or drop it before preparing a corrected
   upload. Resolve that deployment before considering a new upload.
3. Check the expected artifacts directly at `https://repo.maven.apache.org/maven2/`. Include
   the BOM, multiplatform root and platform publications, and any additional namespaces required
   by the approved source. A missing POM alone does not prove that an upload failed.

If no artifacts were published and no deployment can still publish them, return to the
[Release Checklist](release-checklist.md#when-a-run-fails). If the Maven job failed after any
artifacts became public, keep their version and source fixed and continue below. **Re-run failed
jobs** restarts the entire Maven job and can attempt to upload those immutable artifacts again.

## Resolve missing artifacts

If all required Maven artifacts are public, continue to
[Complete the release](#complete-the-release). Recovery then needs no Maven upload. Current releases
publish the chart modules and BOM under
`io.github.hdcharts`; the legacy relocation module was removed after 3.0.0.

If required artifacts are missing, identify the exact coordinates and the original deployment
that should contain them. Once its state is resolved:

1. Validate the existing API/demo assets with the `validate-docs-static-release` action used by
   `release.yml`. They must match the approved version and source SHA.
2. Prepare an upload containing only the missing publications. Validate their coordinates,
   required POM metadata, signatures, and any relocation targets before uploading.
3. Publish those artifacts and wait until Central reports success and serves every expected POM.
   If a task would also upload already-published artifacts, stop and narrow the upload first.

The [validated 3.0.0 recovery workflow](https://github.com/HDCharts/charts/blob/14f71d388d482fe06ea5f1de5675ab259db726e8/.github/workflows/release.yml)
shows the special case of a wholly missing relocation bundle. It refuses to upload if only part
of that bundle is public. Adapt its publication logic only when the affected release requires
those artifacts.

If static-asset validation fails, stop and investigate the mismatch before continuing. Keep
`replace_static_assets` disabled once any Maven artifacts are public.

## Complete the release

Use a temporary recovery branch to modify the existing `.github/workflows/release.yml`.
That workflow already has `workflow_dispatch` on `main`, so it can be dispatched with a recovery
branch ref. Keep the recovery workflow on that branch; keep `main`'s normal release workflow.
The Android workflow is reusable only and must be called by this workflow.

Recovery reuses the original release approval for the recorded version and source SHA. The
replacement jobs below omit `approve-release`, so there is no second `Release Approval` gate.

Prepare the branch workflow as follows:

1. Keep the existing trigger and token permissions. Set its concurrency group to
   `recover-release-<version>` with `cancel-in-progress: false`.
2. Replace the jobs before `build-android` with the example below. It checks the approved source,
   verifies public Maven POMs and static assets, then creates or verifies the release tag.
3. Keep `build-android`, `promote-docs`, `verify-docs-deployment`, and `announce-release` from the
   existing workflow. Their `prepare-release` and `publish-maven` dependencies remain valid.

Replace `<version>`, `<source-sha>`, and `<recovery-branch>` throughout the example. Replace
`<expected-coordinates>` with the complete whitespace-separated list of `groupId:artifactId`
coordinates for the approved release, including the BOM and every platform publication. Use the
approved source's `ChartsModules.publishable`, module publishing configuration, and Kotlin targets
to establish that list. For example, `io.github.hdcharts:line` and
`io.github.hdcharts:line-jvm` are separate coordinates. Check the list against the Central deployment
contents; checking only the umbrella `charts` POM is insufficient.

```yaml
jobs:
  prepare-release:
    name: Verify published release source
    if: github.ref == 'refs/heads/<recovery-branch>'
    runs-on: ubuntu-26.04
    outputs:
      release_version: '<version>'
      source_sha: '<source-sha>'
    steps:
      - uses: actions/checkout@v7
        with:
          fetch-depth: 0
          ref: <source-sha>
      - name: Verify source and public Maven POMs
        env:
          SOURCE_SHA: <source-sha>
          RELEASE_VERSION: <version>
          EXPECTED_COORDINATES: |
            <expected-coordinates>
        shell: bash
        run: |
          set -euo pipefail
          test "$(git rev-parse HEAD)" = "$SOURCE_SHA"
          python3 - <<'CHECK'
          import os
          import urllib.request

          version = os.environ['RELEASE_VERSION']
          coordinates = os.environ['EXPECTED_COORDINATES'].split()
          if not coordinates:
              raise SystemExit('Expected publication list is empty')
          for coordinate in coordinates:
              group, artifact = coordinate.split(':')
              path = f"{group.replace('.', '/')}/{artifact}/{version}/{artifact}-{version}.pom"
              url = f'https://repo.maven.apache.org/maven2/{path}'
              with urllib.request.urlopen(url, timeout=30) as response:
                  if response.status != 200:
                      raise SystemExit(f'POM unavailable: {coordinate}:{version}')
              print(f'Published: {coordinate}:{version}', flush=True)
          CHECK

  publish-maven:
    name: Verify static assets and finish release tag
    needs: prepare-release
    runs-on: ubuntu-26.04
    environment: Production
    steps:
      - uses: actions/checkout@v7
        with:
          fetch-depth: 0
          ref: ${{ needs.prepare-release.outputs.source_sha }}
      - uses: aws-actions/configure-aws-credentials@v6
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
          aws-region: ${{ vars.AWS_REGION }}
      - uses: ./.github/actions/validate-docs-static-release
        with:
          release-version: ${{ needs.prepare-release.outputs.release_version }}
          source-sha: ${{ needs.prepare-release.outputs.source_sha }}
          docs-static-bucket: ${{ vars.DOCS_STATIC_BUCKET }}
      - name: Create or verify the release tag
        env:
          RELEASE_VERSION: ${{ needs.prepare-release.outputs.release_version }}
          SOURCE_SHA: ${{ needs.prepare-release.outputs.source_sha }}
        shell: bash
        run: |
          set -euo pipefail
          if git rev-parse -q --verify "refs/tags/$RELEASE_VERSION" >/dev/null; then
            test "$(git rev-parse "refs/tags/$RELEASE_VERSION^{commit}")" = "$SOURCE_SHA"
          else
            git tag "$RELEASE_VERSION" "$SOURCE_SHA"
            git push origin "refs/tags/$RELEASE_VERSION"
          fi
```

Temporarily grant the exact recovery branch access to `Production` through the GitHub API.
Record the returned policy ID for cleanup:

```bash
gh api --method POST repos/HDCharts/charts/environments/Production/deployment-branch-policies \
  -f 'name=<recovery-branch>' -f type=branch --jq '.id'
```

Set the repository Actions variable `DOCS_APP_URL` to the canonical `https://charts.hdcode.dev` URL.

After reviewing and pushing the branch workflow, dispatch it explicitly:

```bash
gh workflow run release.yml --repo HDCharts/charts --ref <recovery-branch>
```

Watch the recovery run through Android APK publication, docs promotion, public verification, and
GitHub release publication. The verification job checks `/release-manifest.json` for the approved
version and source SHA, plus `/<version>/wiki`, `/<version>/api`, and `/demo/<version>/`. A legacy
domain that redirects can prevent that check from succeeding.

If a recovery job fails, inspect which steps already succeeded before retrying it. A mismatching
existing tag must be investigated; keep release tags fixed at their published source.

After all jobs succeed, remove the temporary `Production` permission using the recorded policy ID:

```bash
gh api --method DELETE 'repos/HDCharts/charts/environments/Production/deployment-branch-policies/<policy-id>'
```

Delete the temporary branch. Keep the run URL and publication checks as the incident record.

## Worked example: 3.0.0

The [3.0.0 publishing run](https://github.com/HDCharts/charts/actions/runs/38978316459) timed out
after 900 seconds while Central was still `PUBLISHING`. Central later published the
`io.github.hdcharts` chart bundle from
[60864f821601ef8b12afe84586638553ce8f8090](https://github.com/HDCharts/charts/commit/60864f821601ef8b12afe84586638553ce8f8090).
The ten legacy relocation POMs under `io.github.dautovicharis` had not been uploaded.

The [successful recovery run](https://github.com/HDCharts/charts/actions/runs/38984909994) used
the validated workflow linked above. It verified the published chart bundle, prepared and
validated the missing relocation POMs, published only those POMs, and waited for their public
availability. It then created the `3.0.0` tag at the approved SHA and completed Android publication,
docs promotion, public docs verification, and the GitHub release. Public verification succeeded
after `DOCS_APP_URL` was set to `https://charts.hdcode.dev`.

Recovery reused the original release approval. Temporary `Production` access for
`recovery/release-3.0.0` was added through the GitHub API and removed after the jobs using that
environment finished.
