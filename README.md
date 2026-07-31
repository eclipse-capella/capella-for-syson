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

# Capella for SysON

Capella for SysON provides a web-based implementation of Capella and the Arcadia methodology on top of SysON, Sirius Web, and SysML v2 technologies.

This repository contains the source code of the project published under the Eclipse Public License v2.0.

## Repository Structure

- `backend/`: Java backend modules and application configuration.
- `frontend/`: React frontend applications and components.
- `doc/`: project documentation.
- `resources/`: shared resources and libraries.
- `scripts/`: development and maintenance scripts.

## Prerequisites

The project uses both Java/Maven and Node.js/npm.

- Java and Maven compatible with the backend modules.
- Node.js `22.16.0`.
- npm `10.9.2`.
- Docker, if you want to run the application with Docker Compose.

## Build

Install the frontend dependencies:

```bash
npm ci
```

Build the frontend workspaces:

```bash
npm run build
```

Copy frontend in backend folder:

```bash
cp -r frontend/capella-for-syson/dist/* backend/application/capella-extension/src/main/resources/static/capella-for-syson/
```

Build the Maven modules:

```bash
mvn clean verify
```

Some dependencies may require additional repository configuration depending on how the upstream SysON and Sirius Web artifacts are distributed.

## Run Locally

Before running locally the application, the database needs to be started:

```bash
./scripts/start-capella-for-syson-database.sh
```

Then start the application as follows:

```bash
java -jar backend/application/capella-application/target/capella-application-2026.7.0.jar
```

The application is exposed on `localhost:8080`.

Alternatively, you can launch Capella for SysON as a SysON plugin with Docker Compose.
To do so, build the extension image after the Maven build, then start the stack:

```bash
SYSON_VERSION=$(mvn --quiet --file backend/application/capella-extension/pom.xml help:evaluate -Dexpression=syson.version -DforceStdout) # 2026.7.2
EXTENSION_VERSION=$(mvn --quiet --file backend/application/capella-extension/pom.xml help:evaluate -Dexpression=project.version -DforceStdout) # 2026.7.0
docker build --build-arg SYSON_IMAGE=eclipsesyson/syson:v$SYSON_VERSION --build-arg EXTENSION_JAR=backend/application/capella-extension/target/capella-extension-$EXTENSION_VERSION.jar --file backend/application/capella-extension/Dockerfile --tag capella-for-syson:latest .
docker compose up
```

## Development

Useful commands:

```bash
./scripts/start-capella-for-syson-database.sh
npm run format-lint
npm run format
npm run build
npm run start
```

The root Maven project aggregates the backend modules under `backend/`.

When working from an IDE, activate the maven `ide` profile to let the editor retrieve the extension jars and correctly launch the application with the extension.

From your IDE, application is to be launched from the `/capella-application/src/main/java/org/eclipse/capella/CapellaForSysonApplication.java` file to start the backend.

Frontend is started using `npm run start`.

In development mode, the application is exposed on `localhost:5173`.

## Installation

> [!INFO]
> This section assumes you already have a SysON up and running.
> If you need to start SysON from scratch you can follow the instructions in the [documentation](https://doc.mbse-syson.org/syson/main/installation-guide/how-tos/install.html).

Capella for SysON is an extension that can be added to an existing SysON installation.
Download the version of the extension that matches your SysON version, then add the following line in your `application.properties`:

```properties
spring.web.resources.static-locations=classpath:/static/capella-for-syson/
```

Or provide the following argument when starting the application `-Dspring.web.resources.static-locations=classpath:/static/capella-for-syson`.
This ensures that SysON displays the frontend with the Capella extensions.

Then run the following command:

```bash
java -Dloader.path=./capella-extension-<Capella for SysON version>.jar \
     -Dloader.main=org.eclipse.syson.SysONApplication \
     -cp syson-application-<SysON version>.jar \
     org.springframework.boot.loader.launch.PropertiesLauncher
```

You application should start and display the SysON banner in its logs, and navigating to `localhost:8080` should display the application with the Capella icon on the top left.

## Functional tests

The Playwright functional tests follow the Sirius Web test architecture and live in `integration-tests-playwright/`. Start the application with `./scripts/start-capella-for-syson.sh`, then run the tests from this directory:

```bash
npm ci
npx playwright test
```

Use `npx playwright test --ui` to debug a scenario locally. The HTML report is generated in `playwright-report/`.

## Contributing

Contributions are welcome. See `CONTRIBUTING.md` for development and contribution guidelines.

## License

Capella for SysON is licensed under the Eclipse Public License v2.0. See `LICENSE` for details.
