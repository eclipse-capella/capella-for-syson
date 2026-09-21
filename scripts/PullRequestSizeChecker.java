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
import java.util.ArrayList;

/**
 * Checks the adjusted size of a pull request.
 *
 * <p>
 * Generated code, documentation, tests, and deleted files do not inflate the pull request size.
 * Added and removed lines in the remaining files count equally.
 * </p>
 * <p>Copyright header of new files are not counted.</p>
 * <p>Empty line are removed from the count.</p>
 *
 * @author tbezierslafosse
 */
public class PullRequestSizeChecker {
    private static final int PERFECT_LIMIT = 100;
    private static final int GOOD_LIMIT = 300;
    private static final int LARGE_LIMIT = 500;

    public static void main(String[] args) {
        int exitCode = run(args);
        System.exit(exitCode);
    }

    static int run(String[] args) {
        try {
            var options = parseArguments(args);

            if (options.help()) {
                printUsage();
                return 0;
            }

            var processBuilder = new ProcessBuilder("git", "log", "-p", options.baseCommit() + ".." + options.headCommit());
            processBuilder.redirectErrorStream(true);
            var process = processBuilder.start();

            var lineCount = getContributionLineCount(process);
            var gitExitCode = process.waitFor();

            if (gitExitCode != 0) {
                System.err.println("Error: git failed with exit code " + gitExitCode + ".");
                return gitExitCode;
            }

            return publishResult(lineCount);
        } catch (IllegalArgumentException exception) {
            System.err.println("Error: " + exception.getMessage());
            System.err.println();
            printUsage();
            return 2;
        } catch (IOException exception) {
            System.err.println("Error: unable to execute git: " + exception.getMessage());
            return 3;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Error: command interrupted.");
            return 130;
        }
    }

    private static Options parseArguments(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Missing arguments.");
        }

        String baseCommit = null;
        String headCommit = "HEAD";
        boolean help = false;

        for (int index = 0; index < args.length; index++) {
            switch (args[index]) {
                case "-h", "--help" -> help = true;
                case "-b", "--base" -> {
                    if (++index >= args.length) {
                        throw new IllegalArgumentException("Missing value after --base.");
                    }
                    baseCommit = args[index];
                }
                case "-H", "--head" -> {
                    if (++index >= args.length) {
                        throw new IllegalArgumentException("Missing value after --head.");
                    }
                    headCommit = args[index];
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + args[index]);
            }
        }

        if (!help && (baseCommit == null || baseCommit.isBlank())) {
            throw new IllegalArgumentException("--base is required.");
        }

        return new Options(baseCommit, headCommit, help);
    }

    private static void printUsage() {
        System.out.println("""
                Usage:
                  java -jar pr-size.jar --base <commit> [--head <commit>]

                Options:
                  -b, --base <commit>   Base commit or branch (required)
                  -H, --head <commit>   Head commit or branch (default: HEAD)
                  -h, --help            Show this help

                Examples:
                  java -jar pr-size.jar --base origin/main
                  java -jar pr-size.jar --base origin/main --head feature/my-change
                  java -jar pr-size.jar -b abc123 -H def456
                """);
    }

    private static int publishResult(long lineCount) {
        System.out.println("## PR Size Report\n");
        System.out.println("**Total modified lines (adjusted):** " + lineCount + "\n");

        if (lineCount <= PERFECT_LIMIT) {
            System.out.println("### Perfect\nThis PR is tiny and focused. It will be incredibly easy to review!");
            return 0;
        } else if (lineCount <= GOOD_LIMIT) {
            System.out.println("### Good\nThis PR is a very reasonable size. Good job keeping it manageable.");
            return 0;
        } else if (lineCount <= LARGE_LIMIT) {
            System.out.println("### Getting Large\nThis PR is quite large. Make sure you've provided excellent documentation and context for the reviewers.");
            return 0;
        } else {
            System.out.println("### Too Long\nThis PR is too large (over 500 lines). Review fatigue is highly likely. Consider breaking this down into smaller, standalone Pull Requests if possible.");
            return 1;
        }
    }

    private static long getContributionLineCount(Process process) {
        long lineCount = 0;

        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            boolean inDiff = false;
            boolean skipCurrentFile = false;
            boolean currentFileDeleted = false;
            boolean currentFileNew = false;
            boolean lookingForNewFileHeader = false;
            List<String> headerCandidate = new ArrayList<>();

            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("commit ")) {
                    inDiff = false;
                } else if (line.startsWith("diff --git ")) {
                    // If a malformed/incomplete candidate was buffered, count its non-blank lines.
                    lineCount += countNonBlankContributionLines(headerCandidate);
                    headerCandidate.clear();

                    inDiff = true;
                    skipCurrentFile = shouldIgnoreFile(line);
                    currentFileDeleted = false;
                    currentFileNew = false;
                    lookingForNewFileHeader = false;
                } else if (inDiff && !skipCurrentFile) {
                    if (line.startsWith("new file mode ")) {
                        currentFileNew = true;
                        lookingForNewFileHeader = true;
                    } else if (line.startsWith("deleted file mode ")) {
                        currentFileDeleted = true;
                        lineCount++;
                    } else if (!currentFileDeleted) {
                        var isAddition = line.startsWith("+") && !line.startsWith("+++");
                        var isRemoval = line.startsWith("-") && !line.startsWith("---");

                        if (isAddition) {
                            if (currentFileNew && lookingForNewFileHeader) {
                                if (headerCandidate.isEmpty() && isBlankContributionLine(line)) {
                                    // Blank lines before the header are ignored anyway.
                                } else if (headerCandidate.isEmpty() && isLicenseHeaderStart(line)) {
                                    headerCandidate.add(line);
                                } else if (!headerCandidate.isEmpty()) {
                                    headerCandidate.add(line);
                                    if (isLicenseHeaderEnd(line)) {
                                        if (!isObeoEplHeader(headerCandidate)) {
                                            lineCount += countNonBlankContributionLines(headerCandidate);
                                        }
                                        headerCandidate.clear();
                                        lookingForNewFileHeader = false;
                                    }
                                } else {
                                    lookingForNewFileHeader = false;
                                    if (!isBlankContributionLine(line)) {
                                        lineCount++;
                                    }
                                }
                            } else if (!isBlankContributionLine(line)) {
                                lineCount++;
                            }
                        } else if (isRemoval && !isBlankContributionLine(line)) {
                            lineCount++;
                        }
                    }
                }

                line = reader.readLine();
            }

            lineCount += countNonBlankContributionLines(headerCandidate);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read git output", exception);
        }

        return lineCount;
    }

    private static boolean isBlankContributionLine(String line) {
        return line.length() <= 1 || line.substring(1).isBlank();
    }

    private static boolean isLicenseHeaderStart(String line) {
        return line.substring(1).stripLeading().startsWith("/*");
    }

    private static boolean isLicenseHeaderEnd(String line) {
        return line.substring(1).stripTrailing().endsWith("*/");
    }

    private static boolean isObeoEplHeader(List<String> lines) {
        var header = String.join("\n", lines);
        return header.matches("(?s).*Copyright \\(c\\) \\d{4} Obeo\\..*")
                && header.contains("Eclipse Public License v2.0")
                && header.contains("https://www.eclipse.org/legal/epl-2.0/")
                && header.contains("SPDX-License-Identifier: EPL-2.0");
    }

    private static long countNonBlankContributionLines(List<String> lines) {
        return lines.stream().filter(line -> !isBlankContributionLine(line)).count();
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
                ".project",
                "backend/application/capella-application/src/test",
                "backend/model/capella-model-transverse-services/src/test"
        ).stream().anyMatch(line::contains);
    }

    private record Options(String baseCommit, String headCommit, boolean help) {
    }
}
