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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Checks the adjusted size of a pull request.
 *
 * <p>
 * Generated code, documentation, tests, and deleted files do not inflate the pull request size. Added and removed
 * lines in the remaining files count equally.
 * </p>
 *
 * @author tbezierslafosse
 */
public class PullRequestSizeChecker {

    public static void main(String[] args) {
        var baseCommit = args[0];
        var headCommit = args[1];

        try {
            var processBuilder = new ProcessBuilder("git", "log", "-p", baseCommit + ".." + headCommit);
            processBuilder.redirectErrorStream(true);
            var process = processBuilder.start();

            var lineCount = PullRequestSizeChecker.getContributionLineCount(process);
            var exitCode = process.waitFor();

            PullRequestSizeChecker.publishResult(lineCount, exitCode);
        } catch (IOException | InterruptedException exception) {
            System.out.println(exception);
        }
    }

    private static void publishResult(long lineCount, int processExitCode) {
        System.out.println("## PR Size Report\n");
        System.out.println("**Total modified lines (adjusted):** " + lineCount + "\n");
        if (lineCount <= 100) {
            System.out.println("### Perfect\nThis PR is tiny and focused. It will be incredibly easy to review!");
        } else if (lineCount <= 300) {
            System.out.println("### Good\nThis PR is a very reasonable size. Good job keeping it manageable.");
        } else if (lineCount <= 500) {
            System.out.println("### Getting Large\nThis PR is quite large. Make sure you've provided excellent documentation and context for the reviewers.");
        } else {
            System.out.println("### Too Long\nThis PR is too large (over 500 lines). Review fatigue is highly likely. Consider breaking this down into smaller, standalone Pull Requests if possible.");
            System.exit(1);
        }

        System.exit(processExitCode);
    }

    private static long getContributionLineCount(Process process) {
        long lineCount = 0;

        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            boolean inDiff = false;
            boolean skipCurrentFile = false;
            boolean currentFileDeleted = false;

            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("commit ")) {
                    inDiff = false;
                } else if (line.startsWith("diff --git ")) {
                    inDiff = true;
                    skipCurrentFile = PullRequestSizeChecker.shouldIgnoreFile(line);
                    currentFileDeleted = false;
                } else if (inDiff && !skipCurrentFile) {
                    if (line.startsWith("deleted file mode ")) {
                        currentFileDeleted = true;
                        lineCount = lineCount + 1;
                    } else if (!currentFileDeleted) {
                        var isAddition = line.startsWith("+") && !line.startsWith("+++");
                        var isRemoval = line.startsWith("-") && !line.startsWith("---");
                        if (isAddition || isRemoval) {
                            lineCount = lineCount + 1;
                        }
                    }
                }

                line = reader.readLine();
            }
        } catch (IOException exception) {
            System.out.println(exception);
        }

        return lineCount;
    }

    private static boolean shouldIgnoreFile(String line) {
        if (line.contains(".ecore") || line.contains(".genmodel")) {
            return false;
        }
        return List.of(
                "CHANGELOG.adoc",
                "package-lock.json",
                "doc/",
                "tests/",
                "org.eclipse.core.resources.prefs",
                "org.eclipse.core.runtime.prefs",
                "org.eclipse.jdt.apt.core.prefs",
                "org.eclipse.jdt.core.prefs",
                "org.eclipse.jdt.ui.prefs",
                "org.eclipse.m2e.core.prefs",
                "org.springframework.ide.eclipse.prefs",
                ".checkstyle",
                ".classpath",
                ".project").stream().anyMatch(line::contains);
    }
}
