
package gradeflow.manager;

import gradeflow.db.DatabaseManager;
import gradeflow.model.*;
import gradeflow.processor.AssignmentProcessor;

import java.sql.SQLException;
import java.util.List;

public class ProjectManager {

    private final DatabaseManager db = DatabaseManager.getInstance();
    private Project currentProject;

   
    public Project createProject(String name, String description,
                                  int configId, String submissionsDir,
                                  List<TestCase> testCases) throws SQLException {
        Project p = new Project(name, description, configId, submissionsDir);
        p.setTestCases(testCases);
        db.saveProject(p);
        p.setConfiguration(db.getConfigurationById(configId));
        this.currentProject = p;
        return p;
    }

    
    public Project openProject(int id) throws SQLException {
        Project p = db.getProjectById(id);
        if (p == null) throw new IllegalArgumentException("Project not found: " + id);
        p.setConfiguration(db.getConfigurationById(p.getConfigurationId()));
        p.setTestCases(db.getTestCasesByProject(id));
        p.setReports(db.getReportsByProject(id));
        this.currentProject = p;
        return p;
    }

    public List<Project> getAllProjects() throws SQLException {
        return db.getAllProjects();
    }

    public void saveProject(Project p) throws SQLException {
        db.updateProject(p);
    }

    public void deleteProject(int id) throws SQLException {
        db.deleteProject(id);
    }

    
    public void runProject(Project project, ProgressCallback cb) throws Exception {
        db.deleteReportsByProject(project.getId());
        project.getReports().clear();

        AssignmentProcessor processor = new AssignmentProcessor(project, cb);
        processor.processAll();

        for (StudentReport r : project.getReports()) {
            db.saveReport(r);
        }
    }

    public Project getCurrentProject()      { return currentProject; }
    public void    setCurrentProject(Project p) { this.currentProject = p; }

    
    @FunctionalInterface
    public interface ProgressCallback {
        void onStudent(String studentId, int done, int total, StudentReport report);
    }
}
