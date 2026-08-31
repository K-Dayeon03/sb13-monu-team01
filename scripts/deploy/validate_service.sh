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

for attempt in $(seq 1 30); do
  if curl -fsS "${HEALTH_URL}" >/dev/null; then
    exit 0
  fi
  echo "Waiting for Monu health check (${attempt}/30): ${HEALTH_URL}"
  sleep 3
done

systemctl --no-pager --full status monu.service || true
journalctl -u monu.service -n 100 --no-pager || true
exit 1
