#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/monu"
SERVICE_FILE="/etc/systemd/system/monu.service"

if [ ! -f "${APP_DIR}/app.jar" ]; then
  echo "app.jar was not copied to ${APP_DIR}" >&2
  exit 1
fi

cat > "${SERVICE_FILE}" <<'EOF'
[Unit]
Description=Monu Spring Boot Application
After=network-online.target
Wants=network-online.target

[Service]
User=monu
Group=monu
WorkingDirectory=/opt/monu
EnvironmentFile=-/etc/monu/monu.env
ExecStart=/bin/bash -lc 'exec /usr/bin/java ${JAVA_OPTS:-} -jar /opt/monu/app.jar'
SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

chmod 644 "${SERVICE_FILE}"
systemctl daemon-reload
systemctl enable monu.service
systemctl reset-failed monu.service || true
