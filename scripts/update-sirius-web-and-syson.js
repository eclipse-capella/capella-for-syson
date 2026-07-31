/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
const childProcess = require("child_process");
const path = require("path");
const fs = require("fs");

const newSiriusWebVersion = process.argv[2];
let newSysonVersion = process.argv[3];

if (!newSiriusWebVersion) {
  console.log("Use this script like this:");
  console.log("node scripts/update-sirius-web.js 2024.7.1");
  process.exit(1);
}

if (!newSysonVersion) {
  newSysonVersion = newSiriusWebVersion;
}

const workspace = process.cwd();

const projects = new Map();
projects.set("capella-parent", "releng")
projects.set("capella-application", "application");
projects.set("capella-application-configuration", "application");
projects.set("capella-extension", "application")
projects.set("capella-frontend", "application");
projects.set("capella-model-edit", "model");
projects.set("capella-model-services", "model");
projects.set("capella-tests", "test");
projects.set("capella-diagram-common-view", "views");
projects.set("capella-diagram-lab-view", "views");
projects.set("capella-diagram-oab-view", "views");
projects.set("capella-diagram-sab-view", "views")
projects.set("capella-form-view", "views")
projects.set("capella-table-view", "views");
projects.set("capella-ddv-view", "views");


console.log("Updating the following pom.xml:");
projects.forEach((folder, project) => {
  const pomXmlPath = path.join(
      workspace,
      "backend",
      folder,
      project,
      "pom.xml"
  );
  console.log(pomXmlPath);

  let pomXmlContent = fs.readFileSync(pomXmlPath, {encoding: "utf-8"});
  const startSiriusTagIndex = pomXmlContent.indexOf("<sirius.web.version>");
  const endSiriusTagIndex = pomXmlContent.indexOf("</sirius.web.version>");
  if (startSiriusTagIndex !== -1 && endSiriusTagIndex !== -1) {
    let newPomXmlContent = pomXmlContent.substring(
        0,
        startSiriusTagIndex + "<sirius.web.version>".length
    );
    newPomXmlContent += newSiriusWebVersion;
    newPomXmlContent += pomXmlContent.substring(endSiriusTagIndex);
    fs.writeFileSync(pomXmlPath, newPomXmlContent, {encoding: "utf-8"});
    pomXmlContent = fs.readFileSync(pomXmlPath, {encoding: "utf-8"});
  }
  const startSysonTagIndex = pomXmlContent.indexOf("<syson.version>");
  const endSysonTagIndex = pomXmlContent.indexOf("</syson.version>");
  if (startSysonTagIndex !== -1 && endSysonTagIndex !== -1) {
    let newPomXmlContent = pomXmlContent.substring(
        0,
        startSysonTagIndex + "<syson.version>".length
    );
    newPomXmlContent += newSysonVersion;
    newPomXmlContent += pomXmlContent.substring(endSysonTagIndex);
    fs.writeFileSync(pomXmlPath, newPomXmlContent, {encoding: "utf-8"});
  }
});

const updateSiriusWebCommand = `npm install --save-exact @eclipse-sirius/sirius-components-browser@${newSiriusWebVersion} @eclipse-sirius/sirius-components-charts@${newSiriusWebVersion} @eclipse-sirius/sirius-components-core@${newSiriusWebVersion} @eclipse-sirius/sirius-components-datatree@${newSiriusWebVersion} @eclipse-sirius/sirius-components-deck@${newSiriusWebVersion} @eclipse-sirius/sirius-components-diagrams@${newSiriusWebVersion} @eclipse-sirius/sirius-components-formdescriptioneditors@${newSiriusWebVersion} @eclipse-sirius/sirius-components-forms@${newSiriusWebVersion} @eclipse-sirius/sirius-components-gantt@${newSiriusWebVersion} @eclipse-sirius/sirius-components-impactanalysis@${newSiriusWebVersion} @eclipse-sirius/sirius-components-markdown@${newSiriusWebVersion} @eclipse-sirius/sirius-components-omnibox@${newSiriusWebVersion} @eclipse-sirius/sirius-components-portals@${newSiriusWebVersion} @eclipse-sirius/sirius-components-widget-reference@${newSiriusWebVersion} @eclipse-sirius/sirius-components-widget-table@${newSiriusWebVersion} @eclipse-sirius/sirius-components-selection@${newSiriusWebVersion} @eclipse-sirius/sirius-components-tables@${newSiriusWebVersion} @eclipse-sirius/sirius-components-trees@${newSiriusWebVersion} @eclipse-sirius/sirius-components-validation@${newSiriusWebVersion} @eclipse-sirius/sirius-components-tsconfig@${newSiriusWebVersion} @eclipse-sirius/sirius-web-application@${newSiriusWebVersion} @eclipse-syson/syson-components@${newSysonVersion} @eclipse-sirius/sirius-components-palette@${newSiriusWebVersion}`;

console.log("Updating @eclipse-sirius/sirius-web and @eclipse-syson/syson in the frontend");
const capellaFrontendWorkingDirectory = path.join(workspace, "frontend", "capella-for-syson");
childProcess.execSync(updateSiriusWebCommand, {
  cwd: capellaFrontendWorkingDirectory,
  stdio: "inherit",
});

const updateSiriusWebPeerCommand = `npm install --save-peer --save-exact @eclipse-sirius/sirius-components-browser@${newSiriusWebVersion} @eclipse-sirius/sirius-components-charts@${newSiriusWebVersion} @eclipse-sirius/sirius-components-core@${newSiriusWebVersion} @eclipse-sirius/sirius-components-datatree@${newSiriusWebVersion} @eclipse-sirius/sirius-components-deck@${newSiriusWebVersion} @eclipse-sirius/sirius-components-diagrams@${newSiriusWebVersion} @eclipse-sirius/sirius-components-formdescriptioneditors@${newSiriusWebVersion} @eclipse-sirius/sirius-components-forms@${newSiriusWebVersion} @eclipse-sirius/sirius-components-gantt@${newSiriusWebVersion} @eclipse-sirius/sirius-components-impactanalysis@${newSiriusWebVersion} @eclipse-sirius/sirius-components-markdown@${newSiriusWebVersion} @eclipse-sirius/sirius-components-omnibox@${newSiriusWebVersion} @eclipse-sirius/sirius-components-portals@${newSiriusWebVersion} @eclipse-sirius/sirius-components-widget-reference@${newSiriusWebVersion} @eclipse-sirius/sirius-components-widget-table@${newSiriusWebVersion} @eclipse-sirius/sirius-components-selection@${newSiriusWebVersion} @eclipse-sirius/sirius-components-tables@${newSiriusWebVersion} @eclipse-sirius/sirius-components-trees@${newSiriusWebVersion} @eclipse-sirius/sirius-components-validation@${newSiriusWebVersion} @eclipse-sirius/sirius-components-tsconfig@${newSiriusWebVersion} @eclipse-sirius/sirius-web-application@${newSiriusWebVersion} @eclipse-syson/syson-components@${newSysonVersion} @eclipse-sirius/sirius-components-palette@${newSiriusWebVersion}`;

const capellaComponentsFrontendWorkingDirectory = path.join(
    workspace,
    "frontend",
    "capella-for-syson-components"
);
childProcess.execSync(updateSiriusWebPeerCommand, {
  cwd: capellaComponentsFrontendWorkingDirectory,
  stdio: "inherit",
});

childProcess.execSync('npm dedupe', {
  cwd: workspace,
  stdio: 'inherit',
});

const gitAddCommand = `git add .`;
console.log(gitAddCommand);
childProcess.execSync(gitAddCommand, {stdio: "inherit"});

const gitCommitCommand = `git commit -s -m "[releng] Switch to Sirius Web ${newSiriusWebVersion} and Syson ${newSysonVersion}"`;
console.log(gitCommitCommand);
childProcess.execSync(gitCommitCommand, {stdio: "inherit"});
