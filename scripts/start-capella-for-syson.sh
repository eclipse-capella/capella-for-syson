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
BACKEND_LOG="/tmp/capella-for-syson-backend.log"
FRONTEND_LOG="/tmp/capella-for-syson-frontend.log"
BACKEND_PID_FILE="/tmp/capella-for-syson-backend.pid"
FRONTEND_PID_FILE="/tmp/capella-for-syson-frontend.pid"
BACKEND_PORT="8080"
FRONTEND_PORT="5173"
DATABASE_PORT="5442"
DATABASE_SERVICE="database"

compose_command() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  else
    docker-compose "$@"
  fi
}

stop_existing() {
  "$ROOT_DIR/scripts/stop-capella-for-syson.sh" >/dev/null 2>&1 || true
}

wait_for_port() {
  local port="$1"
  local timeout_seconds="$2"
  local waited=0

  while [ "$waited" -lt "$timeout_seconds" ]; do
    if ss -ltn "( sport = :$port )" | grep -q ":$port"; then
      return 0
    fi
    sleep 1
    waited=$((waited + 1))
  done

  return 1
}

cleanup_on_failure() {
  "$ROOT_DIR/scripts/stop-capella-for-syson.sh" >/dev/null 2>&1 || true
}

wait_for_database() {
  local container_id
  local timeout_seconds=120
  local waited=0

  while [ "$waited" -lt "$timeout_seconds" ]; do
    container_id="$(compose_command -f "$ROOT_DIR/docker-compose.yml" ps -q "$DATABASE_SERVICE")"
    if [ -n "$container_id" ]; then
      local health_status
      health_status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "$container_id" 2>/dev/null || true)"
      if [ "$health_status" = "healthy" ] || [ "$health_status" = "running" ]; then
        if wait_for_port "$DATABASE_PORT" 1; then
          return 0
        fi
      fi
    fi

    sleep 1
    waited=$((waited + 1))
  done

  return 1
}

start_database() {
  compose_command -f "$ROOT_DIR/docker-compose.yml" up -d "$DATABASE_SERVICE"

  if ! wait_for_database; then
    printf 'Database did not start. Check docker compose logs for %s\n' "$DATABASE_SERVICE" >&2
    return 1
  fi
}

start_backend() {
  rm -f "$BACKEND_LOG" "$BACKEND_PID_FILE"
  nohup bash -lc "cd '$ROOT_DIR' && mvn -pl backend/application/capella-application -am compile -DskipTests && exec mvn -pl backend/application/capella-application spring-boot:run" >> "$BACKEND_LOG" 2>&1 &
  echo "$!" > "$BACKEND_PID_FILE"

  if ! wait_for_port "$BACKEND_PORT" 600; then
    printf 'Backend did not start. Check %s\n' "$BACKEND_LOG" >&2
    return 1
  fi
}

start_frontend() {
  rm -f "$FRONTEND_LOG" "$FRONTEND_PID_FILE"
  nohup bash -lc "cd '$ROOT_DIR' && npm run build-dev --workspace @obeo/capella-for-syson-components && cd '$ROOT_DIR/frontend/capella-for-syson' && export VITE_HTTP_SERVER_PORT='$BACKEND_PORT' VITE_WS_SERVER_PORT='$BACKEND_PORT' && exec npm start" >> "$FRONTEND_LOG" 2>&1 &
  echo "$!" > "$FRONTEND_PID_FILE"

  if ! wait_for_port "$FRONTEND_PORT" 120; then
    printf 'Frontend did not start. Check %s\n' "$FRONTEND_LOG" >&2
    return 1
  fi
}

main() {
  trap cleanup_on_failure ERR

  stop_existing

  start_database
  start_backend
  start_frontend

  trap - ERR

  cat <<EOF
Capella for SysON launch started.

Frontend: http://localhost:${FRONTEND_PORT}
Backend:  http://localhost:${BACKEND_PORT}
Database: localhost:${DATABASE_PORT}

Logs:
- $BACKEND_LOG
- $FRONTEND_LOG

PIDs:
- backend:  $(cat "$BACKEND_PID_FILE")
- frontend: $(cat "$FRONTEND_PID_FILE")
EOF
}

cd "$ROOT_DIR"
main
