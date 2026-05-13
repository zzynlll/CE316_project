package gradeflow.model;

public enum ReportStatus {

    PASS,
    FAIL,
    COMPILE_ERROR,
    RUNTIME_ERROR,
    ZIP_ERROR,
    MISSING_FILE,
    TIMEOUT,
    PENDING
}