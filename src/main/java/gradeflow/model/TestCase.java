
package gradeflow.model;

public class TestCase {

    private int    id;
    private int    projectId;
    private String description;
    private String arguments;
    private String expectedOutputPath;

    public TestCase() {}

    public TestCase(int projectId, String description, String arguments, String expectedOutputPath) {
        this.projectId          = projectId;
        this.description        = description;
        this.arguments          = arguments;
        this.expectedOutputPath = expectedOutputPath;
    }

    public int    getId()                  { return id; }
    public void   setId(int id)            { this.id = id; }

    public int    getProjectId()           { return projectId; }
    public void   setProjectId(int pid)    { this.projectId = pid; }

    public String getDescription()         { return description; }
    public void   setDescription(String d) { this.description = d; }

    public String getArguments()           { return arguments; }
    public void   setArguments(String a)   { this.arguments = a; }

    public String getExpectedOutputPath()          { return expectedOutputPath; }
    public void   setExpectedOutputPath(String p)  { this.expectedOutputPath = p; }

    @Override
    public String toString() {
        return (description != null && !description.isBlank()) ? description : arguments;
    }
}
