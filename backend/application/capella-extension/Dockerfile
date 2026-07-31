###############################################################################
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
###############################################################################
ARG SYSON_IMAGE
FROM ${SYSON_IMAGE}

ARG EXTENSION_JAR
COPY ${EXTENSION_JAR} /capella-extension.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dloader.path=/capella-extension.jar", "-Dloader.main=org.eclipse.syson.SysONApplication", "-Dspring.web.resources.static-locations=classpath:/static/capella-for-syson/", "-cp", "/syson-application.jar", "org.springframework.boot.loader.launch.PropertiesLauncher"]
