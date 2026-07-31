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

const KEYWORDS = [
  "cleanup",
  "doc",
  "fix",
  "releng",
  "test",
  "perf",
  "infra",
  "enh"
];
const ISSUE_URL_PREFIX = "Bug: https://github.com/";
const SIGNED_OFF_BY_PREFIX = "Signed-off-by:";
const COMMIT_TITLE_PATTERN = new RegExp(
  `^\\[(?:\\d+|${KEYWORDS.join("|")})\\][^\\S\\r\\n]\\p{Lu}`,
  "u"
);
const COMMIT_TAG_PATTERN = /^\[([^\]]+)\]/;

const isValidCommitTitle = (title) => COMMIT_TITLE_PATTERN.test(title);

const validateCommitMessage = (message) => {
  const lines = message.replace(/\r\n/g, "\n").split("\n");
  const title = lines[0];
  const errors = [];

  if (!isValidCommitTitle(title)) {
    errors.push(
      `The title must start with an issue number or one of the regular keywords (${KEYWORDS.join(
        ", "
      )}), followed by a space and an uppercase letter`
    );
  }

  if (lines.length < 2 || !/^\s*$/.test(lines[1])) {
    errors.push("The title must be followed by a blank line");
  }

  const tagMatch = COMMIT_TAG_PATTERN.exec(title);
  if (tagMatch && /^\d+$/.test(tagMatch[1])) {
    const tag = tagMatch[1];
    const matchingIssueURLLines = lines.filter(
      (line) => line.trim().startsWith(ISSUE_URL_PREFIX) && line.endsWith(tag)
    );
    if (matchingIssueURLLines.length !== 1) {
      errors.push(
        `The message must contain exactly one issue URL footer for issue ${tag}`
      );
    }
  }

  const signedOffIndex = lines.findIndex((line) =>
    line.trim().startsWith(SIGNED_OFF_BY_PREFIX)
  );
  if (signedOffIndex === -1) {
    errors.push(`The message must contain a '${SIGNED_OFF_BY_PREFIX}' footer`);
  }

  const issueURLIndex = lines.findIndex((line) =>
    line.trim().startsWith(ISSUE_URL_PREFIX)
  );
  if (
    signedOffIndex !== -1 &&
    issueURLIndex !== -1 &&
    Math.abs(signedOffIndex - issueURLIndex) !== 1
  ) {
    errors.push(
      `The '${SIGNED_OFF_BY_PREFIX}' and '${ISSUE_URL_PREFIX}...' footers must be next to each other`
    );
  }

  return errors;
};

const main = () => {
  const body = JSON.parse(process.env.GITHUB_EVENT);
  const baseSHA = body.pull_request.base.sha;
  const headSHA = body.pull_request.head.sha;
  const result = childProcess.execFileSync(
    "git",
    ["rev-list", `${baseSHA}..${headSHA}`],
    { encoding: "utf8" }
  );
  const commitSHAs = result.split(/\r?\n/).filter(Boolean);
  const invalidCommits = commitSHAs
    .map((commitSHA) => {
      const message = childProcess.execFileSync(
        "git",
        ["show", "-s", "--format=%B", commitSHA],
        { encoding: "utf8" }
      );
      return {
        commitSHA,
        title: message.split(/\r?\n/, 1)[0],
        errors: validateCommitMessage(message),
      };
    })
    .filter((commit) => commit.errors.length > 0);

  if (invalidCommits.length > 0) {
    console.log("The following commits have invalid metadata:");
    invalidCommits.forEach((commit) => {
      console.log(`${commit.commitSHA} ${commit.title}`);
      commit.errors.forEach((error) => console.log(`  - ${error}`));
    });
    process.exit(1);
  }
};

if (require.main === module) {
  main();
}

module.exports = { isValidCommitTitle, validateCommitMessage };
