#!/usr/bin/env bash
set -euo pipefail

systemctl start monu.service
sleep 5
systemctl --no-pager --full status monu.service
