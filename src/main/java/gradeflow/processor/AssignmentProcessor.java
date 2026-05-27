package gradeflow.processor;

import gradeflow.manager.ProjectManager.ProgressCallback;
import gradeflow.model.*;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssignmentProcessor {

    private final Project          project;
    private final Configuration    cfg;
    private final ProgressCallback callback;

    private final ZipHandler       zipHandler  = new ZipHandler();
    private final CommandExecutor  cmdExec     = new CommandExecutor();
    private final OutputComparator comparator  = new OutputComparator();


    private final File workDir;

    public AssignmentProcessor(Project project, ProgressCallback callback) {
        this.project  = project;
        this.cfg      = project.getConfiguration();
        this.callback = callback;
        this.workDir  = new File(System.getProperty("user.home")
                + File.separator + "GradeFlow"
                + File.separator + "work"
                + File.separator + project.getId());
        this.workDir.mkdirs();
    }

    public void processAll() {
        File dir  = new File(project.getSubmissionsDir());
        File[] zips = dir.listFiles(f -> f.isFile() &&
                f.getName().toLowerCase().endsWith(".zip"));

        if (zips == null || zips.length == 0) return;

        int total = zips.length, done = 0;

        for (File zip : zips) {
            String studentId = zip.getName().replaceFirst("\\.zip$", "");
            StudentReport report = new StudentReport(project.getId(), studentId);

            try {
                runPipeline(zip, report);
            } catch (Exception e) {
                report.markZipError("Unexpected error: " + e.getMessage());
            }

            project.addReport(report);
            done++;
            if (callback != null)
                callback.onStudent(studentId, done, total, report);
        }
    }

    private void runPipeline(File zipFile, StudentReport report) {
 // Step 1 - extract
        File studentDir;
        try {
            studentDir = zipHandler.extract(zipFile, workDir);
        } catch (Exception e) {
            report.markZipError("ZIP extraction failed: " + e.getMessage());
            return;
        }

        // Step 2 - check source file exists (applies to BOTH compiled and interpreted)
        // Must be before compile/run so we get MISSING_FILE instead of a confusing error
        File srcFile = new File(studentDir, cfg.getSourceFileName());
        if (!srcFile.exists()) {
            report.markMissingFile(cfg.getSourceFileName());
            return;
        }

        // Step 3 - compile (skip for interpreted languages)
        if (cfg.isCompiled()) {
            List<String> compileCmd = buildCompileCmd(srcFile);
            CommandExecutor.Result cr;
            try {
                cr = cmdExec.run(compileCmd, studentDir, CommandExecutor.COMPILE_TIMEOUT);
            } catch (Exception e) {
                report.markCompileError("Could not start compiler: " + e.getMessage());
                return;
            }

            report.setCompileLog(cr.combined());

            if (cr.timedOut) { report.markTimeout(); return; }
            if (!cr.ok())    { report.markCompileError(cr.combined()); return; }
        }

        // Step 4 - run with all test cases and compare output
        List<TestCase> tcs = project.getTestCases();
        if (tcs.isEmpty()) {
            report.setResult(true, "No test cases defined.");
            return;
        }

        StringBuilder runLogBuilder  = new StringBuilder();
        StringBuilder diffLogBuilder = new StringBuilder();
        int passCount = 0;

        for (int i = 0; i < tcs.size(); i++) {
            TestCase tc = tcs.get(i);
            String label = "Test Case " + (i + 1) +
                    (tc.getDescription() != null && !tc.getDescription().isBlank()
                            ? " (" + tc.getDescription() + ")" : "");

            List<String> runCmd = buildRunCmd(studentDir, tc.getArguments());
            CommandExecutor.Result rr;
            try {
                rr = cmdExec.run(runCmd, studentDir, CommandExecutor.RUN_TIMEOUT);
            } catch (Exception e) {
                report.setRunLog(runLogBuilder.toString());
                report.markRuntimeError("Could not start process: " + e.getMessage());
                return;
            }

            if (rr.timedOut) {
                report.setRunLog(runLogBuilder.toString());
                report.markTimeout();
                return;
            }

            if (runLogBuilder.length() > 0) runLogBuilder.append("\n\n");
            runLogBuilder.append("[").append(label).append("]\n").append(rr.combined());

            // Step 5 - compare output for this test case
            if (rr.exitCode != 0 && rr.stdout.isBlank()) {
                diffLogBuilder.append("[").append(label).append(": RUNTIME ERROR]\n")
                        .append(rr.combined()).append("\n\n");
                continue;
            }

            try {
                OutputComparator.CompareResult cmp = comparator.compare(
                        rr.stdout, Path.of(tc.getExpectedOutputPath()), cfg.getComparisonMethod());
                if (cmp.matched) {
                    passCount++;
                    diffLogBuilder.append("[").append(label).append(": PASS]\n\n");
                } else {
                    diffLogBuilder.append("[").append(label).append(": FAIL]\n")
                            .append(cmp.diff).append("\n\n");
                }
            } catch (Exception e) {
                diffLogBuilder.append("[").append(label).append(": ERROR — ")
                        .append(e.getMessage()).append("]\n\n");
            }
        }

        report.setRunLog(runLogBuilder.toString());
        report.setResult(passCount == tcs.size(), diffLogBuilder.toString());
    }

    private List<String> buildCompileCmd(File sourceFile) {
        List<String> cmd = new ArrayList<>();
        cmd.add(cfg.getCompilerPath());
        if (cfg.getCompilerArgs() != null && !cfg.getCompilerArgs().isBlank())
            cmd.addAll(Arrays.asList(cfg.getCompilerArgs().trim().split("\\s+")));
        cmd.add(sourceFile.getAbsolutePath());
        return cmd;
    }

    private List<String> buildRunCmd(File studentDir, String args) {
        List<String> cmd = new ArrayList<>();

        String[] parts = cfg.getRunCommand().trim().split("\\s+");

        if (parts.length > 0) {
            File runFile = new File(studentDir, parts[0]);

            if (runFile.exists()) {
                cmd.add(runFile.getAbsolutePath());
            } else {
                cmd.add(parts[0]);
            }

            for (int i = 1; i < parts.length; i++) {
                cmd.add(parts[i]);
            }
        }

        if (args != null && !args.isBlank()) {
            cmd.addAll(Arrays.asList(args.trim().split("\\s+")));
        }

        return cmd;
    }
}
