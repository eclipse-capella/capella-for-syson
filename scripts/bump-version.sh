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

VERSION="$1"
mvn versions:set -DnewVersion="$VERSION" -DprocessAllModules

npm version "$VERSION"  --commit-hooks=false --git-tag-version=false --workspaces=true --include-workspace-root=true --workspaces-update=false
npm i
