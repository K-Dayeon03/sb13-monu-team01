#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="/etc/monu/monu.env"

if [ -f "${ENV_FILE}" ]; then
  set -a
  # shellcheck disable=SC1090
  . "${ENV_FILE}"
  set +a
fi

PORT="${SERVER_PORT:-8080}"
HEALTH_URL="http://127.0.0.1:${PORT}/actuator/health"
VALIDATION_TIMEOUT_SECONDS=150
SLEEP_SECONDS=3
attempt=1

while [ "${SECONDS}" -lt "${VALIDATION_TIMEOUT_SECONDS}" ]; do
  if curl -fsS --connect-timeout 2 --max-time 2 "${HEALTH_URL}" >/dev/null; then
    exit 0
  fi

  echo "Waiting for Monu health check (${attempt}): ${HEALTH_URL}"
  attempt=$((attempt + 1))
  sleep "${SLEEP_SECONDS}"
done

systemctl --no-pager --full status monu.service || true
journalctl -u monu.service -n 100 --no-pager || true
exit 1
