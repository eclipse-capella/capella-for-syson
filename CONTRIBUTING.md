<!--
  Copyright (c) 2026 Obeo.
  This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v2.0
  which accompanies this distribution, and is available at
  https://www.eclipse.org/legal/epl-2.0/

  SPDX-License-Identifier: EPL-2.0

  Contributors:
      Obeo - initial API and implementation
-->
# Contributing to Capella for SysON

Thank you for your interest in contributing to Capella for SysON.

## Development Setup

Install the frontend dependencies:

```bash
npm install
```

Build the frontend workspaces:

```bash
npm run build
```

Build the backend modules:

```bash
mvn clean verify
```

## Before Submitting Changes

- Keep changes focused and minimal.
- Run the relevant frontend or backend checks for the area you changed.
- Update documentation when changing user-visible behavior.
- Keep the EPL-2.0 license headers and SPDX identifiers consistent with existing files.

## Reporting Issues

When reporting an issue, include:

- the version or commit used;
- the steps to reproduce the problem;
- the expected behavior;
- the actual behavior;
- relevant logs or screenshots when available.
