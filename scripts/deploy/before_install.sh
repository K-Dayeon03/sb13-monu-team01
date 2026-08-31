#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/monu"
ENV_DIR="/etc/monu"
LOG_DIR="/var/log/monu"
APP_USER="monu"

if ! id "${APP_USER}" >/dev/null 2>&1; then
  useradd --system --home-dir "${APP_DIR}" --shell /sbin/nologin "${APP_USER}" \
    || useradd --system --home-dir "${APP_DIR}" --shell /usr/sbin/nologin "${APP_USER}"
fi

install -d -m 755 "${APP_DIR}"
install -d -m 755 "${ENV_DIR}"
install -d -m 755 "${LOG_DIR}"

chown -R "${APP_USER}:${APP_USER}" "${APP_DIR}" "${LOG_DIR}"

if [ ! -f "${ENV_DIR}/monu.env" ]; then
  cat > "${ENV_DIR}/monu.env" <<'EOF'
SPRING_PROFILES_ACTIVE=prod
SPRING_BATCH_JOB_ENABLED=false
BATCH_ENABLED=true
BATCH_SCHEDULER_ENABLED=true
AWS_REGION=ap-northeast-2
SERVER_PORT=8080
JAVA_OPTS=-Xms256m -Xmx512m
EOF
fi

chown root:"${APP_USER}" "${ENV_DIR}/monu.env"
chmod 640 "${ENV_DIR}/monu.env"
