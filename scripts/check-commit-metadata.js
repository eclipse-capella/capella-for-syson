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

const COMMIT_TITLE_PATTERN = /^\[(?:\d+|doc|releng|deps)\]\s+\S/;

const isValidCommitTitle = (title) => COMMIT_TITLE_PATTERN.test(title);

const main = () => {
  const body = JSON.parse(process.env.GITHUB_EVENT);
  const baseSHA = body.pull_request.base.sha;
  const headSHA = body.pull_request.head.sha;
  const result = childProcess.execSync(`git log --format=%s ${baseSHA}..${headSHA}`, { encoding: "utf8" });
  const titles = result.split(/\r?\n/).filter(Boolean);
  const invalidTitles = titles.filter((title) => !isValidCommitTitle(title));

  if (invalidTitles.length > 0) {
    console.log("The following commit titles must start with [issue], [doc], [releng], or [deps]:");
    console.log(invalidTitles);
    process.exit(1);
  }
};

if (require.main === module) {
  main();
}

module.exports = { isValidCommitTitle };
