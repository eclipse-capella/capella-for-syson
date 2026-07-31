#!/usr/bin/env bash
#
# Copyright (c) 2026 Obeo.
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#     Obeo - initial API and implementation
#

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_PID_FILE="/tmp/capella-for-syson-backend.pid"
FRONTEND_PID_FILE="/tmp/capella-for-syson-frontend.pid"

compose_command() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  else
    docker-compose "$@"
  fi
}

kill_if_running() {
  local pid_file="$1"
  if [ -f "$pid_file" ]; then
    local pid
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$pid_file"
  fi
}

kill_if_running "$BACKEND_PID_FILE"
kill_if_running "$FRONTEND_PID_FILE"

pkill -9 -f "$ROOT_DIR.*backend/application/capella-application.*spring-boot:run" || true
pkill -9 -f "$ROOT_DIR.*CapellaForSysonApplication" || true
pkill -9 -f "$ROOT_DIR/frontend/capella-for-syson.*vite --host" || true
pkill -9 -f "$ROOT_DIR/node_modules/.bin/vite --host" || true

rm -f "$BACKEND_PID_FILE" "$FRONTEND_PID_FILE"

compose_command -f "$ROOT_DIR/docker-compose.yml" stop database >/dev/null 2>&1 || true

printf 'Capella for SysON stopped.\n'
