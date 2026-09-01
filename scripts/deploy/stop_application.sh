#!/usr/bin/env bash
set -euo pipefail

SERVICE_NAME="monu.service"

if systemctl cat "${SERVICE_NAME}" >/dev/null 2>&1; then
  systemctl stop "${SERVICE_NAME}" || true
  systemctl reset-failed "${SERVICE_NAME}" || true
fi
