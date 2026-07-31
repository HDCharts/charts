#!/usr/bin/env bash
#
# verify-signing-key.sh
#
# Validates the OpenPGP secret key used by Gradle/Vanniktech in-memory signing.
# The key is inspected in an isolated GnuPG home without being imported.
#
# The script verifies that:
#   - the supplied material is an OpenPGP secret-key bundle;
#   - it contains exactly one primary key;
#   - the primary key is not expired, revoked, or invalid;
#   - exactly one usable signing-capable key/subkey can be selected; and
#   - the selected signing key/subkey is not expired, revoked, or invalid.
#
# Supported variables:
#   SIGNING_IN_MEMORY_KEY
#   SIGNING_IN_MEMORY_KEY_ID                 optional
#
# Gradle/Vanniktech variable names are also accepted directly:
#   ORG_GRADLE_PROJECT_signingInMemoryKey
#   ORG_GRADLE_PROJECT_signingInMemoryKeyId  optional
#
# Usage:
#   verify-signing-key.sh
#     Skip successfully when no signing key is configured. Useful locally.
#
#   verify-signing-key.sh --required
#     Fail when the signing key is missing. Use this in release workflows.
#
# Requires Bash 4+ and GnuPG.
# The Maven publishing workflow runs on GitHub-hosted Ubuntu runners.
set -euo pipefail

script_name="verify-signing-key"
required="false"

usage() {
  cat <<'USAGE'
Usage: verify-signing-key.sh [--required]

  --required  Fail if the signing key is missing or is a null/undefined
              GitHub Actions placeholder.
USAGE
}

fail() {
  echo "${script_name}: FAILED: $*" >&2
  exit 1
}

case "${1:-}" in
  "")
    ;;
  --required)
    required="true"
    ;;
  -h|--help)
    usage
    exit 0
    ;;
  *)
    usage >&2
    echo "${script_name}: unknown argument: ${1}" >&2
    exit 2
    ;;
esac

if (( $# > 1 )); then
  usage >&2
  echo "${script_name}: too many arguments" >&2
  exit 2
fi

in_memory_key="${SIGNING_IN_MEMORY_KEY:-${ORG_GRADLE_PROJECT_signingInMemoryKey:-}}"
configured_key_id="${SIGNING_IN_MEMORY_KEY_ID:-${ORG_GRADLE_PROJECT_signingInMemoryKeyId:-}}"

case "${in_memory_key}" in
  ""|null|undefined)
    if [[ "${required}" == "true" ]]; then
      fail "signing key is required but was not configured."
    fi

    echo "${script_name}: signing key not configured; skipping validation."
    exit 0
    ;;
esac

if ! command -v gpg >/dev/null 2>&1; then
  fail "gpg is required but was not found in PATH."
fi

now="$(date +%s)"
if [[ ! "${now}" =~ ^[0-9]+$ ]]; then
  fail "could not determine the current UNIX timestamp."
fi

tmp_gpg_home="$(mktemp -d)"
cleanup() {
  rm -rf "${tmp_gpg_home}"
}
trap cleanup EXIT HUP INT TERM
chmod 700 "${tmp_gpg_home}"

listing_file="${tmp_gpg_home}/key-listing.txt"

# show-only performs a dry-run import and emits machine-readable metadata.
# Secret key material is not written into the temporary keyring.
if ! printf '%s\n' "${in_memory_key}" \
  | LC_ALL=C gpg \
      --homedir "${tmp_gpg_home}" \
      --no-options \
      --batch \
      --no-tty \
      --with-colons \
      --import-options show-only \
      --import \
      >"${listing_file}" 2>/dev/null; then
  fail "could not parse the configured OpenPGP signing key."
fi

record_types=()
record_validities=()
record_key_ids=()
record_expirations=()
record_capabilities=()
record_fingerprints=()
record_count=0
pending_record=-1

# Capture each secret primary/subkey record and its following fingerprint.
while IFS=: read -r f1 f2 f3 f4 f5 f6 f7 f8 f9 f10 f11 f12 remainder; do
  case "${f1}" in
    sec|ssb)
      record_types[${record_count}]="${f1}"
      record_validities[${record_count}]="${f2}"
      record_key_ids[${record_count}]="${f5}"
      record_expirations[${record_count}]="${f7}"
      record_capabilities[${record_count}]="${f12}"
      record_fingerprints[${record_count}]=""
      pending_record=${record_count}
      record_count=$((record_count + 1))
      ;;
    fpr)
      if (( pending_record >= 0 )); then
        record_fingerprints[${pending_record}]="${f10}"
        pending_record=-1
      fi
      ;;
  esac
done <"${listing_file}"

if (( record_count == 0 )); then
  fail "configured material does not contain an OpenPGP secret key."
fi

primary_count=0
primary_index=-1
index=0
while (( index < record_count )); do
  if [[ "${record_types[${index}]}" == "sec" ]]; then
    primary_count=$((primary_count + 1))
    primary_index=${index}
  fi
  index=$((index + 1))
done

if (( primary_count == 0 )); then
  fail "secret-key bundle contains no primary key."
fi

if (( primary_count > 1 )); then
  fail "secret-key bundle contains ${primary_count} primary keys; expected exactly one."
fi

record_problem=""
validate_record() {
  local validity="$1"
  local expiration="$2"

  record_problem=""

  case "${validity}" in
    e)
      record_problem="is expired"
      return 1
      ;;
    r)
      record_problem="is revoked"
      return 1
      ;;
    i)
      record_problem="is invalid"
      return 1
      ;;
  esac

  if [[ -n "${expiration}" && "${expiration}" != "0" ]]; then
    if [[ ! "${expiration}" =~ ^[0-9]+$ ]]; then
      record_problem="has an invalid expiration value '${expiration}'"
      return 1
    fi

    if (( expiration <= now )); then
      record_problem="expired at UNIX epoch ${expiration}"
      return 1
    fi
  fi

  return 0
}

record_label() {
  local record_index="$1"
  local fingerprint="${record_fingerprints[${record_index}]:-}"
  local key_id="${record_key_ids[${record_index}]:-}"

  if [[ -n "${fingerprint}" ]]; then
    printf '%s' "${fingerprint}"
  elif [[ -n "${key_id}" ]]; then
    printf '%s' "${key_id}"
  else
    printf '%s' '<unknown>'
  fi
}

primary_label="$(record_label "${primary_index}")"
if ! validate_record \
  "${record_validities[${primary_index}]}" \
  "${record_expirations[${primary_index}]}"; then
  fail "primary key ${primary_label} ${record_problem}."
fi

normalized_key_id=""
if [[ -n "${configured_key_id}" ]]; then
  normalized_key_id="${configured_key_id//[[:space:]]/}"
  normalized_key_id="${normalized_key_id^^}"
  normalized_key_id="${normalized_key_id#0X}"

  if [[ ! "${normalized_key_id}" =~ ^[0-9A-F]+$ ]] || (( ${#normalized_key_id} < 8 )); then
    fail "configured signing key ID must contain at least 8 hexadecimal characters."
  fi
fi

matches_configured_id() {
  local value="$1"
  local normalized_value="${value//[[:space:]]/}"
  normalized_value="${normalized_value^^}"

  [[ -n "${normalized_value}" && "${normalized_value}" == *"${normalized_key_id}" ]]
}

selected_index=-1

if [[ -n "${normalized_key_id}" ]]; then
  matched_records=0
  matched_signing_records=0
  index=0

  while (( index < record_count )); do
    if matches_configured_id "${record_key_ids[${index}]}" \
      || matches_configured_id "${record_fingerprints[${index}]:-}"; then
      matched_records=$((matched_records + 1))

      # Lowercase 's' means this specific key record can sign. Uppercase 'S'
      # on a primary record can merely describe capability available somewhere
      # in the complete key, such as on a signing subkey.
      if [[ "${record_capabilities[${index}]}" == *s* ]]; then
        matched_signing_records=$((matched_signing_records + 1))
        selected_index=${index}
      fi
    fi

    index=$((index + 1))
  done

  if (( matched_records == 0 )); then
    fail "configured signing key ID ${normalized_key_id} was not found in the secret-key bundle."
  fi

  if (( matched_signing_records == 0 )); then
    fail "configured key ID ${normalized_key_id} does not identify a signing-capable key or subkey."
  fi

  if (( matched_signing_records > 1 )); then
    fail "configured key ID ${normalized_key_id} matches more than one signing key; use a longer key ID or fingerprint."
  fi
else
  signing_candidates=0
  usable_signing_candidates=0
  index=0

  while (( index < record_count )); do
    if [[ "${record_capabilities[${index}]}" == *s* ]]; then
      signing_candidates=$((signing_candidates + 1))

      if validate_record \
        "${record_validities[${index}]}" \
        "${record_expirations[${index}]}"; then
        usable_signing_candidates=$((usable_signing_candidates + 1))
        selected_index=${index}
      fi
    fi

    index=$((index + 1))
  done

  if (( signing_candidates == 0 )); then
    fail "secret-key bundle contains no signing-capable key or subkey."
  fi

  if (( usable_signing_candidates == 0 )); then
    fail "secret-key bundle contains no usable signing-capable key or subkey."
  fi

  if (( usable_signing_candidates > 1 )); then
    fail "secret-key bundle contains multiple usable signing keys; configure SIGNING_IN_MEMORY_KEY_ID to select one explicitly."
  fi
fi

selected_label="$(record_label "${selected_index}")"
if ! validate_record \
  "${record_validities[${selected_index}]}" \
  "${record_expirations[${selected_index}]}"; then
  fail "selected signing key ${selected_label} ${record_problem}."
fi

selected_type="primary key"
if [[ "${record_types[${selected_index}]}" == "ssb" ]]; then
  selected_type="subkey"
fi

selected_expiration="${record_expirations[${selected_index}]}"
if [[ -z "${selected_expiration}" || "${selected_expiration}" == "0" ]]; then
  selected_expiration="never"
fi

echo "${script_name}: signing ${selected_type} ${selected_label} is valid (expiry=${selected_expiration}, now=${now})."
