
package gradeflow.model;

import java.time.LocalDateTime;

public class StudentReport {

    private int           id;
    private int           projectId;
    private String        studentId;
    private ReportStatus  status;
    private String        compileLog;
    private String        runLog;
    private String        diffLog;
    private LocalDateTime processedAt;

    public StudentReport() {
        this.status      = ReportStatus.PENDING;
        this.processedAt = LocalDateTime.now();
    }

    public StudentReport(int projectId, String studentId) {
        this.projectId   = projectId;
        this.studentId   = studentId;
        this.status      = ReportStatus.PENDING;
        this.processedAt = LocalDateTime.now();
    }

    public void markCompileError(String log) {
        this.status     = ReportStatus.COMPILE_ERROR;
        this.compileLog = log;
    }

    public void markRuntimeError(String log) {
        this.status = ReportStatus.RUNTIME_ERROR;
        this.runLog  = log;
    }

    public void markTimeout() {
        this.status = ReportStatus.TIMEOUT;
        this.runLog  = "Process killed after exceeding time limit.";
    }

    public void markZipError(String msg) {
        this.status     = ReportStatus.ZIP_ERROR;
        this.compileLog = msg;
    }

    public void markMissingFile(String fname) {
        this.status     = ReportStatus.MISSING_FILE;
        this.compileLog = "Required file not found in submission: " + fname;
    }

    public void setResult(boolean passed, String diff) {
        this.status  = passed ? ReportStatus.PASS : ReportStatus.FAIL;
        this.diffLog = diff;
    }

    public int           getId()                   { return id; }
    public void          setId(int id)             { this.id = id; }

    public int           getProjectId()            { return projectId; }
    public void          setProjectId(int pid)     { this.projectId = pid; }

    public String        getStudentId()            { return studentId; }
    public void          setStudentId(String sid)  { this.studentId = sid; }

    public ReportStatus  getStatus()               { return status; }
    public void          setStatus(ReportStatus s) { this.status = s; }

    public String        getCompileLog()           { return compileLog; }
    public void          setCompileLog(String l)   { this.compileLog = l; }

    public String        getRunLog()               { return runLog; }
    public void          setRunLog(String l)       { this.runLog = l; }

    public String        getDiffLog()              { return diffLog; }
    public void          setDiffLog(String l)      { this.diffLog = l; }

    public LocalDateTime getProcessedAt()          { return processedAt; }
    public void          setProcessedAt(LocalDateTime t) { this.processedAt = t; }
}
