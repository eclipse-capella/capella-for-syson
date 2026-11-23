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
FROM node:22-alpine AS ui

WORKDIR /opt/doc-ui
COPY doc-ui/package*.json ./
RUN npm ci
COPY doc-ui/ ./
RUN npm run build

FROM node:22-alpine

RUN apk add --no-cache git
WORKDIR /opt/antora
COPY docs-site/package*.json ./
RUN npm ci --omit=dev
COPY --from=ui /opt/doc-ui/build/ui-bundle.zip /opt/antora/ui-bundle.zip

ENV PATH="/opt/antora/node_modules/.bin:${PATH}"
ENV NODE_PATH="/opt/antora/node_modules"
WORKDIR /usr/app/doc/docs-site
ARG ANTORA_PLAYBOOK=doc/docs-site/antora-playbook.yml
ENV ANTORA_PLAYBOOK_PATH=/usr/app/${ANTORA_PLAYBOOK}
CMD ["sh", "-c", "exec antora --stacktrace --log-failure-level=warn --ui-bundle-url=/opt/antora/ui-bundle.zip \"$ANTORA_PLAYBOOK_PATH\""]
