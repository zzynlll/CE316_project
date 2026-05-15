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

        // Step 4 - run with test case arguments
        // TODO: loop over all test cases; using first one for the prototype
        List<TestCase> tcs = project.getTestCases();
        if (tcs.isEmpty()) {
            report.setResult(true, "No test cases defined.");
            return;
        }

        TestCase tc = tcs.get(0);
        List<String> runCmd = buildRunCmd(studentDir, tc.getArguments());
        CommandExecutor.Result rr;
        try {
            rr = cmdExec.run(runCmd, studentDir, CommandExecutor.RUN_TIMEOUT);
        } catch (Exception e) {
            report.markRuntimeError("Could not start process: " + e.getMessage());
            return;
        }

        if (rr.timedOut) { report.markTimeout(); return; }

        report.setRunLog(rr.combined());

        if (rr.exitCode != 0 && rr.stdout.isBlank()) {
            report.markRuntimeError(rr.combined());
            return;
        }

        // Step 5 - compare output
        try {
            OutputComparator.CompareResult cmp = comparator.compare(
                    rr.stdout, Path.of(tc.getExpectedOutputPath()), cfg.getComparisonMethod());
            report.setResult(cmp.matched, cmp.diff);
        } catch (Exception e) {
            report.setDiffLog("Could not read expected output file: " + e.getMessage());
            report.setResult(false, report.getDiffLog());
        }
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
        List<String> cmd = new ArrayList<>(
                Arrays.asList(cfg.getRunCommand().trim().split("\\s+")));
        if (args != null && !args.isBlank())
            cmd.addAll(Arrays.asList(args.trim().split("\\s+")));
        return cmd;
    }
}
