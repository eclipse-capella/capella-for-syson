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
const fs = require("fs");
const path = require("path");

const SUPPORTED_EXTENSIONS = new Set([".java", ".js", ".sh", ".ts", ".tsx", ".xml", ".yaml", ".yml"]);

const isCopyrightCheckedFile = (filePath) => SUPPORTED_EXTENSIONS.has(path.extname(filePath));

const hasCurrentEplHeader = (content, year) => {
  const header = content.split(/\r?\n/).slice(0, 20).join("\n");
  return header.includes("Copyright") && header.includes(year.toString()) && header.includes("SPDX-License-Identifier: EPL-2.0");
};

const getFilesWithAnInvalidCopyright = (filePaths, workspace, year) =>
  filePaths.filter((filePath) => {
    if (!isCopyrightCheckedFile(filePath)) {
      return false;
    }

    const absolutePath = path.join(workspace, filePath);
    return fs.existsSync(absolutePath) && !hasCurrentEplHeader(fs.readFileSync(absolutePath, { encoding: "utf8" }), year);
  });

const main = () => {
  const workspace = process.env.GITHUB_WORKSPACE;
  const body = JSON.parse(process.env.GITHUB_EVENT);
  const baseSHA = body.pull_request.base.sha;
  const headSHA = body.pull_request.head.sha;
  const result = childProcess.execSync(`git diff --name-only ${baseSHA}...${headSHA}`, { encoding: "utf8" });
  const filePaths = result.split(/\r?\n/).filter(Boolean);

  console.log("The following files will be reviewed:");
  console.log(filePaths);
  console.log();

  const filesWithAnInvalidCopyright = getFilesWithAnInvalidCopyright(filePaths, workspace, new Date().getFullYear());
  if (filesWithAnInvalidCopyright.length > 0) {
    console.log("The following files are missing an up-to-date EPL copyright header:");
    console.log(filesWithAnInvalidCopyright);
    process.exit(1);
  }
};

if (require.main === module) {
  main();
}

module.exports = { getFilesWithAnInvalidCopyright, hasCurrentEplHeader, isCopyrightCheckedFile };
