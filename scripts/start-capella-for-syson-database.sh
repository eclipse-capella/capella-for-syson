#!/bin/sh
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

docker kill capella-for-syson-postgres || true
docker rm capella-for-syson-postgres || true
docker run -p 5442:5432 --name capella-for-syson-postgres -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=dbpwd -e POSTGRES_DB=capella-for-syson-db -d postgres:15
