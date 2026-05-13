package gradeflow.processor;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandExecutor {

    public static final int COMPILE_TIMEOUT = 30;
    public static final int RUN_TIMEOUT     = 10;

    public Result run(List<String> cmd, File workDir, int timeoutSec)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir);
        pb.redirectErrorStream(false);

        Process proc = pb.start();


        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();

        Thread tOut = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) out.append(line).append('\n');
            } catch (IOException ignored) {}
        });
        Thread tErr = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(proc.getErrorStream()))) {
                String line;
                while ((line = r.readLine()) != null) err.append(line).append('\n');
            } catch (IOException ignored) {}
        });

        tOut.start();
        tErr.start();

        boolean finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS);

        if (!finished) {
            proc.destroyForcibly();
            tOut.join(1000);
            tErr.join(1000);
            return new Result(-1, out.toString(), err.toString(), true);
        }

        tOut.join(2000);
        tErr.join(2000);

        return new Result(proc.exitValue(), out.toString(), err.toString(), false);
    }

    public static class Result {
        public final int     exitCode;
        public final String  stdout;
        public final String  stderr;
        public final boolean timedOut;

        public Result(int exitCode, String stdout, String stderr, boolean timedOut) {
            this.exitCode = exitCode;
            this.stdout   = stdout;
            this.stderr   = stderr;
            this.timedOut = timedOut;
        }

        public boolean ok()      { return !timedOut && exitCode == 0; }

        public String combined() {
            StringBuilder sb = new StringBuilder();
            if (!stderr.isBlank()) sb.append("[stderr]\n").append(stderr);
            if (!stdout.isBlank()) sb.append("[stdout]\n").append(stdout);
            return sb.toString();
        }
    }
}
