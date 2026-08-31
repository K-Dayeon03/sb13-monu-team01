#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="monu.service"

systemctl reset-failed "${SERVICE_NAME}" || true
systemctl restart "${SERVICE_NAME}"
sleep 5
systemctl is-active --quiet "${SERVICE_NAME}"
systemctl --no-pager --full status "${SERVICE_NAME}"
