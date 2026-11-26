#!/usr/bin/env sh
set -eu
URL='https://fliarbi.com/datasets/files/india-startup-funding/india_startup_funding_sample.csv'
DEST="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)/india_startup_funding_sample.csv"
curl --fail --location --retry 2 --output "$DEST" "$URL"
HEADER=$(head -n 1 "$DEST" 2>/dev/null || true)
if [ ! -s "$DEST" ] || [ "$(wc -c < "$DEST")" -lt 1024 ] || ! printf '%s' "$HEADER" | grep -q 'round_id' || ! printf '%s' "$HEADER" | grep -q 'company_name'; then
  rm -f "$DEST"
  echo 'The downloaded file does not look like the publisher CSV. Retry from the publisher dataset page.' >&2
  exit 1
fi
printf 'Downloaded actual publisher CSV to: %s\n' "$DEST"
