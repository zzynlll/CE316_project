package gradeflow.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Project {

    private int           id;
    private String        name;
    private String        description;
    private int           configurationId;
    private String        submissionsDir;
    private LocalDateTime createdAt;

    private Configuration       configuration;
    private List<TestCase>      testCases = new ArrayList<>();
    private List<StudentReport> reports   = new ArrayList<>();

    public Project() {
        this.createdAt = LocalDateTime.now();
    }

    public Project(String name, String description, int configurationId, String submissionsDir) {
        this.name            = name;
        this.description     = description;
        this.configurationId = configurationId;
        this.submissionsDir  = submissionsDir;
        this.createdAt       = LocalDateTime.now();
    }

    public void addReport(StudentReport r) { reports.add(r); }

    public int           getId()                   { return id; }
    public void          setId(int id)             { this.id = id; }

    public String        getName()                 { return name; }
    public void          setName(String n)         { this.name = n; }

    public String        getDescription()          { return description; }
    public void          setDescription(String d)  { this.description = d; }

    public int           getConfigurationId()      { return configurationId; }
    public void          setConfigurationId(int c) { this.configurationId = c; }

    public String        getSubmissionsDir()        { return submissionsDir; }
    public void          setSubmissionsDir(String s){ this.submissionsDir = s; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void          setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    public Configuration       getConfiguration()          { return configuration; }
    public void                setConfiguration(Configuration c) { this.configuration = c; }

    public List<TestCase>      getTestCases()              { return testCases; }
    public void                setTestCases(List<TestCase> tc) { this.testCases = tc; }

    public List<StudentReport> getReports()                { return reports; }
    public void                setReports(List<StudentReport> r) { this.reports = r; }

    @Override public String toString() { return name; }
}
