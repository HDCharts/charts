#!/usr/bin/env bash
set -euo pipefail

mode="${1:-}"
if [[ -z "${mode}" ]]; then
  echo "Usage: $0 <release|snapshot>" >&2
  exit 1
fi

if [[ -z "${CLOUDFRONT_DISTRIBUTION_ID:-}" ]]; then
  echo "Missing required environment variable: CLOUDFRONT_DISTRIBUTION_ID" >&2
  exit 1
fi

case "${mode}" in
  release)
    if [[ -z "${CURRENT_VERSION:-}" ]]; then
      echo "Missing required environment variable for release mode: CURRENT_VERSION" >&2
      exit 1
    fi

    aws cloudfront create-invalidation \
      --distribution-id "${CLOUDFRONT_DISTRIBUTION_ID}" \
      --paths \
        "/static/_meta/charts-release-publish.json" \
        "/static/api/${CURRENT_VERSION}/*" \
        "/static/demo/${CURRENT_VERSION}/*"
    ;;
  snapshot)
    aws cloudfront create-invalidation \
      --distribution-id "${CLOUDFRONT_DISTRIBUTION_ID}" \
      --paths \
        "/static/_meta/charts-snapshot-publish.json" \
        "/static/api/snapshot/*" \
        "/static/demo/snapshot/*" \
        "/static/playground/snapshot/*"
    ;;
  *)
    echo "Unsupported mode: ${mode}" >&2
    exit 1
    ;;
esac
