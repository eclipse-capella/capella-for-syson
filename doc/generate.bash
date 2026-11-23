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

SCRIPT_DIR=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
PLAYBOOK=${1:-doc/docs-site/antora-playbook.yml}
IMAGE=capella-for-syson-doc-generator:local

docker build "$SCRIPT_DIR" --tag "$IMAGE" --build-arg "ANTORA_PLAYBOOK=$PLAYBOOK"
docker run --rm --user "$(id -u):$(id -g)" \
  --env HOME=/tmp \
  --volume "$SCRIPT_DIR/..:/usr/app:z" \
  "$IMAGE"
