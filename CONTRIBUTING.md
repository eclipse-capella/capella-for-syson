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

### Backend

#### Java

All Capella for SysON backend modules use Java 21.
You can download Java 21 JDK here: https://adoptium.net/fr/temurin/releases?version=21&os=any&arch=any
Then, you can test that your favorite shell is using Java 21 by applying this command (from anywhere):

```bash
java --version
```

#### Apache Maven

All Capella for SysON backend modules are exposed as Maven artifacts.
The minimum version is Maven 3.6.3, but please download the latest 3.x version and try to stay up-to-date.
You can download Maven here: https://maven.apache.org/download.cgi
You can follow the installation instructions here: https://maven.apache.org/install.html
Then, you can test that your favorite shell knows Maven by applying this command (from anywhere):

```bash
mvn --version
```

#### GitHub and GitHub Personal Access Token

You will need a GitHub account and a GitHub personal access token.
Indeed, the backend components of Capella for SysON depend on
[SysON](https://github.com/eclipse-syson/syson), [Sirius Web](https://github.com/eclipse-sirius/sirius-web),
and [sirius-emf-json](https://github.com/eclipse-sirius/sirius-emf-json), [Arcadia](https://github.com/eclipse-capella/arcadia-sysmlv2-lib)
which are published as Maven artifacts on GitHub packages.
To build Capella for SysON locally, you need a GitHub access token so that Maven can download `syson`, `sirius-web`, and
`sirius-emf-json` artifacts.

1. Create a personal token with a `read:package` scope following
   the [GitHub documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens).

> [!WARNING]
> Once generated, a token cannot be displayed anymore, so make sure to copy it in a secure location

2. Create or edit `$HOME/.m2/settings.xml` to tell Maven to use this token when accessing the `syson`/ `sirius-web`,
   `sirius-emf-json` repositories:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github-sirius-emfjson</id>
            <username>$GITHUB_USERNAME</username>
            <password>$GITHUB_ACCESS_TOKEN</password>
        </server>
        <server>
            <id>github-sirius-web</id>
            <username>$GITHUB_USERNAME</username>
            <password>$GITHUB_ACCESS_TOKEN</password>
        </server>
        <server>
            <id>github-syson</id>
            <username>$GITHUB_USERNAME</username>
            <password>$GITHUB_ACCESS_TOKEN</password>
        </server>
        <server>
            <id>github-arcadia</id>
            <username>$GITHUB_USERNAME</username>
            <password>$GITHUB_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```

Be sure to replace `$GITHUB_USERNAME` with your GitHub user id, and `$GITHUB_ACCESS_TOKEN` with the value of your access
token.

> [!CAUTION]
> The id used in your setting.xml must be `github-syson`, `github-sirius-web`, `github-sirius-emfjson`, and
`github-arcadia` to match the ids used in the POMs.

#### Docker

Docker is the preferred way to install and manage the PostgreSQL database used by Capella for SysON.
You can download Docker here: https://www.docker.com/products/docker-desktop/.
On most Linux distribution, Docker is packaged and can be installed directly.

Please download the latest version and try to stay up-to-date.

Then, you can test that your favorite shell knows Docker by applying this command (from anywhere):

```bash
docker --version
```

When developing Capella for SysON, you can execute the following script to start a PostgreSQL instance with the
appropriate parameters:

```bash
./scripts/start-capella-for-syson-database.sh
```

#### IDE resources

When working from an IDE, activate the maven `ide` profile to let the editor retrieve the extension jars and correctly
launch the application with the extension.

Some resources are available to configure your IDE in `backend/releng/capella-resources`:

- `checkstyle/CheckstyleConfiguration.xml`: the checkstyle configuration of the project.
  Note that this configuration works with Checkstyle `13.3.0`, which may not be the default checkstyle version used by
  your IDE.
- `editor/CleanupProfile.xml`: the Eclipse/STS cleanup profile
- `editor/codetemplates.xml`: the Eclipse/STS code templates
- `editor/JavaFormatter.xml`: the Eclipse/STS formatting rules
- `editor/sirius.importorder`: the Eclipse/STS import order

Note that the resources aim at STS/Eclipse, but other IDEs like IntelliJ allow to import Eclipse configuration files.

### Frontend

#### Node/npm

All Capella for SysON frontend modules are Node.js packages.
The current Node version to use is 22.16.0.
The current npm version to use is 10.9.2.

We strongly encourage the use of Node Version Manager (NVM) to manage your Node.js/npm versions.
Indeed, Node.js/npm versions have changed since the beginning of the development of Capella for SysON, and will change
again in the future.

NVM helps to quickly switch between versions of Node.js/npm.
You can download and follow the installation instructions of NVM here: https://github.com/nvm-sh/nvm.
On Windows, please follow: https://github.com/coreybutler/nvm-windows.

Once installed, you can run the following shell command (from anywhere):

```bash
nvm install 22.16.0
```

This will install the appropriate version of npm as well.

Then check the node version with the following shell command (from anywhere):

```bash
node -v
```

#### GitHub and GitHub Personal Access Token

As for the backend configuration, the `syson` and `sirius-web` frontend packages are hosted on GitHub.
After creating your GitHub personal access token, you will need to create a `.npmrc` file in your `$HOME` with the
following content:

```
//npm.pkg.github.com/:_authToken=$GITHUB_ACCESS_TOKEN
```

Be sure to replace `$GITHUB_ACCESS_TOKEN` with the value of your access token.

#### IDE resources

All Capella for SysON frontend modules use Prettier to format the code.
You can configure your IDE to use the provided `.prettierrc` files (note that some editors like VSCode require to
install a prettier plugin first).

### Build

#### Backend

Your IDE should be able to build the backend based on the POM files.
You can create a run configuration to start the application using `CapellaForSysonApplication.java`.

Note that you can build the backend from the command line with:

```bash
mvn clean verify
```

This will build the backend and run all the tests.

#### Frontend

Install the frontend dependencies:

```bash
npm ci
```

Start the frontend

```bash
npm run start
```

Once the frontend and the backend are started, navigate to `localhost:5173` to access the application.

## Reporting Issues

When reporting an issue, include:

- the version or commit used;
- the steps to reproduce the problem;
- the expected behavior;
- the actual behavior;
- relevant logs or screenshots when available.

## Submitting Changes

### Follow the coding conventions already in place in the repository

When working on a feature, look for similar pieces of code from contributions that have already been merged and are
maintained by the team.
This ensures your code fits well with the existing architecture and established best practices.

Note that it is rare to build a feature entirely from scratch, and some elements can often be reused from other
contributions, for example a contribution to a menu, or a modal window to open in an interface.

Make sure you keep the EPL-2.0 license headers and SPDX identifiers consistent with existing files.

Refer to the [development setup instructions](#development-setup) to configure your IDE with the appropriate resources (
checkstyle, code templates, etc).

### Keep changes focused and minimal

Keep your pull requests short.
The automated checks will reject any PR exceeding 500 lines of production code.
Tests and documentation don't count toward this limit, so it is never a reason to skimp on either.

If you think a change will be large, reach out to the team to discuss how to break it down before you start.
This clarifies what is acceptable and what is not, and ensure everyone is aligned.

Reviewers generally expect a single commit.
Some PRs legitimately contain several, but it is unusual, so mention it up front.
If you feel you need multiple commits, that often means the change could be split into several PRs.

A typical split is one PR for the frontend and one for the backend.
The frontend PR relies on hardcoded data and lets you work on the visual rendering.
The backend PR then provides the data the feature needs.
If the feature is unfinished, use capabilities so it isn't visible to all users.

### Review your own PR before requesting a review

Before asking for a review, go through your own PR and perform a self-review.
This lets you 1) make sure you haven't pushed changes you didn't intend to push, such as leftover debugging code or
comments that should be removed, and 2) point out the hot spots of the PR to help the reviewer.

Leave comments on the pieces of code that aren't obvious or need to be discussed.
This greatly increases the reviewer's confidence in your contribution: it shows that you have checked the quality of
what you pushed and are not trying to slip in shaky code.

This is especially important in an AI-assisted world, where you are expected to take full responsibility for the code
you push.
If a tool wrote part of your contribution, you must be able to explain it, justify it, and modify it if
needed.

We may have to set aside contributions that clearly don't fit with the existing code base, as this usually means
they haven't been reviewed.

### Make sure your changes pass the checks

Run the relevant frontend or backend checks locally for the area you changed.
Reviewers expect PR they review to build and tests to pass.
If this is not the case, you should at least put a comment on the PR to indicate you are aware of the issue (e.g. an
unrelated, flaky frontend test fails on your branch).
PRs that don't build without explanation won't be reviewed.

If the checks fail on code coverage, it is generally more efficient to add a test than to try to negotiate a lower
coverage threshold for a module.

If you do opt for lowering the threshold (for example, for generated code that has no reason to be tested), include the
change to the expected coverage rates in your PR.
This makes your intent explicit to the reviewer and lets the build pass.
Be aware, however, that the change may be rejected.

### Document your changes

Document non-trivial choices in the code.
This means choices that differ from the usual assumptions elsewhere in the project.
A PR description is useful during the review, but it gets lost over time, whereas a comment in the code stays visible
to anyone who comes across it later.
Don't overdo it: the goal is to document what is out of the ordinary, not to explain every line.

### Update the changelog

Add an entry in the changelog referencing the related issue.

If your contribution breaks an API or requires a migration, you must document it in the changelog.
The following should be considered breaking changes:

- Moving a class or interface to another package
- Renaming a class: include both the original and the new name in the changelog, to make upgrades easier for projects
  that depend on this one
- Adding or removing a mandatory parameter on a public method;
- Removing a public method: include the solution to use instead, for the same reason.

In general, there is never too much documentation for breaking changes.
Keep in mind that projects that depend on this one may be affected several weeks later, and that your contribution will
only be one part of the API changes they have to absorb.
