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
MAX_ATTEMPTS=40
SLEEP_SECONDS=3

print_service_logs() {
  systemctl --no-pager --full status monu.service || true
  journalctl -u monu.service -n 100 --no-pager || true
}

for attempt in $(seq 1 "${MAX_ATTEMPTS}"); do
  if command -v systemctl >/dev/null 2>&1 && ! systemctl is-active --quiet monu.service; then
    echo "monu.service is not active during health check."
    print_service_logs
    exit 1
  fi

  if curl -fsS --connect-timeout 2 --max-time 2 "${HEALTH_URL}" >/dev/null; then
    exit 0
  fi

  echo "Waiting for Monu health check (${attempt}/${MAX_ATTEMPTS}): ${HEALTH_URL}"
  sleep "${SLEEP_SECONDS}"
done

print_service_logs
exit 1
