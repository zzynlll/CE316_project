
package gradeflow.db;

import gradeflow.model.*;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection conn;

    private DatabaseManager() {
        try {
            File dir = new File(System.getProperty("user.home") + File.separator + "GradeFlow");
            if (!dir.exists()) dir.mkdirs();

            String url = "jdbc:sqlite:" + dir.getAbsolutePath() + File.separator + "gradeflow.db";
            conn = DriverManager.getConnection(url);
            
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot open database: " + e.getMessage(), e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private void createTables() throws SQLException {
        Statement st = conn.createStatement();

        st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS configurations (" +
            "  id               INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name             TEXT NOT NULL UNIQUE," +
            "  language         TEXT NOT NULL," +
            "  compiler_path    TEXT," +
            "  compiler_args    TEXT," +
            "  source_file_name TEXT NOT NULL," +
            "  run_command      TEXT NOT NULL," +
            "  is_compiled      INTEGER NOT NULL DEFAULT 1," +
            "  comparison_method TEXT NOT NULL DEFAULT 'TRIMMED'" +
            ")"
        );

        st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS projects (" +
            "  id              INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  name            TEXT NOT NULL," +
            "  description     TEXT," +
            "  config_id       INTEGER NOT NULL REFERENCES configurations(id)," +
            "  submissions_dir TEXT NOT NULL," +
            "  created_at      TEXT NOT NULL" +
            ")"
        );

        st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS test_cases (" +
            "  id                   INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  project_id           INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE," +
            "  description          TEXT," +
            "  arguments            TEXT," +
            "  expected_output_path TEXT NOT NULL" +
            ")"
        );

        st.executeUpdate(
            "CREATE TABLE IF NOT EXISTS student_reports (" +
            "  id           INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  project_id   INTEGER NOT NULL REFERENCES projects(id) ON DELETE CASCADE," +
            "  student_id   TEXT NOT NULL," +
            "  status       TEXT NOT NULL DEFAULT 'PENDING'," +
            "  compile_log  TEXT," +
            "  run_log      TEXT," +
            "  diff_log     TEXT," +
            "  processed_at TEXT NOT NULL" +
            ")"
        );

        st.close();
    }

   

    public Configuration saveConfiguration(Configuration c) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO configurations (name, language, compiler_path, compiler_args," +
            " source_file_name, run_command, is_compiled, comparison_method)" +
            " VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, c.getName());
        ps.setString(2, c.getLanguage());
        ps.setString(3, c.getCompilerPath());
        ps.setString(4, c.getCompilerArgs());
        ps.setString(5, c.getSourceFileName());
        ps.setString(6, c.getRunCommand());
        ps.setInt(7, c.isCompiled() ? 1 : 0);
        ps.setString(8, c.getComparisonMethod().name());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) c.setId(keys.getInt(1));
        ps.close();
        return c;
    }

    public void updateConfiguration(Configuration c) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE configurations SET name=?, language=?, compiler_path=?, compiler_args=?," +
            " source_file_name=?, run_command=?, is_compiled=?, comparison_method=? WHERE id=?");
        ps.setString(1, c.getName());
        ps.setString(2, c.getLanguage());
        ps.setString(3, c.getCompilerPath());
        ps.setString(4, c.getCompilerArgs());
        ps.setString(5, c.getSourceFileName());
        ps.setString(6, c.getRunCommand());
        ps.setInt(7, c.isCompiled() ? 1 : 0);
        ps.setString(8, c.getComparisonMethod().name());
        ps.setInt(9, c.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void deleteConfiguration(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM configurations WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public int countProjectsByConfig(int configId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT COUNT(*) FROM projects WHERE config_id=?");
        ps.setInt(1, configId);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        ps.close();
        return count;
    }

    public List<Configuration> getAllConfigurations() throws SQLException {
        List<Configuration> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT * FROM configurations ORDER BY name");
        while (rs.next()) list.add(mapConfig(rs));
        rs.close();
        return list;
    }

    public Configuration getConfigurationById(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM configurations WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Configuration c = rs.next() ? mapConfig(rs) : null;
        ps.close();
        return c;
    }

    private Configuration mapConfig(ResultSet rs) throws SQLException {
        Configuration c = new Configuration();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setLanguage(rs.getString("language"));
        c.setCompilerPath(rs.getString("compiler_path"));
        c.setCompilerArgs(rs.getString("compiler_args"));
        c.setSourceFileName(rs.getString("source_file_name"));
        c.setRunCommand(rs.getString("run_command"));
        c.setCompiled(rs.getInt("is_compiled") == 1);
        c.setComparisonMethod(ComparisonMethod.valueOf(rs.getString("comparison_method")));
        return c;
    }

    

    public Project saveProject(Project p) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO projects (name, description, config_id, submissions_dir, created_at)" +
            " VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, p.getName());
        ps.setString(2, p.getDescription());
        ps.setInt(3, p.getConfigurationId());
        ps.setString(4, p.getSubmissionsDir());
        ps.setString(5, p.getCreatedAt().toString());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) p.setId(keys.getInt(1));
        ps.close();

        for (TestCase tc : p.getTestCases()) {
            tc.setProjectId(p.getId());
            saveTestCase(tc);
        }
        return p;
    }

    public void updateProject(Project p) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE projects SET name=?, description=?, config_id=?, submissions_dir=? WHERE id=?");
        ps.setString(1, p.getName());
        ps.setString(2, p.getDescription());
        ps.setInt(3, p.getConfigurationId());
        ps.setString(4, p.getSubmissionsDir());
        ps.setInt(5, p.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void deleteProject(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("DELETE FROM projects WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public List<Project> getAllProjects() throws SQLException {
        List<Project> list = new ArrayList<>();
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT * FROM projects ORDER BY created_at DESC");
        while (rs.next()) list.add(mapProject(rs));
        rs.close();
        return list;
    }

    public Project getProjectById(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM projects WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Project p = rs.next() ? mapProject(rs) : null;
        ps.close();
        return p;
    }

    private Project mapProject(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setConfigurationId(rs.getInt("config_id"));
        p.setSubmissionsDir(rs.getString("submissions_dir"));
        p.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return p;
    }

    

    public TestCase saveTestCase(TestCase tc) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO test_cases (project_id, description, arguments, expected_output_path)" +
            " VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, tc.getProjectId());
        ps.setString(2, tc.getDescription());
        ps.setString(3, tc.getArguments());
        ps.setString(4, tc.getExpectedOutputPath());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) tc.setId(keys.getInt(1));
        ps.close();
        return tc;
    }

    public void deleteTestCasesByProject(int projectId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "DELETE FROM test_cases WHERE project_id=?");
        ps.setInt(1, projectId);
        ps.executeUpdate();
        ps.close();
    }

    public List<TestCase> getTestCasesByProject(int projectId) throws SQLException {
        List<TestCase> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM test_cases WHERE project_id=? ORDER BY id");
        ps.setInt(1, projectId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            TestCase tc = new TestCase();
            tc.setId(rs.getInt("id"));
            tc.setProjectId(rs.getInt("project_id"));
            tc.setDescription(rs.getString("description"));
            tc.setArguments(rs.getString("arguments"));
            tc.setExpectedOutputPath(rs.getString("expected_output_path"));
            list.add(tc);
        }
        ps.close();
        return list;
    }

   

    public StudentReport saveReport(StudentReport r) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO student_reports (project_id, student_id, status, compile_log," +
            " run_log, diff_log, processed_at) VALUES (?,?,?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, r.getProjectId());
        ps.setString(2, r.getStudentId());
        ps.setString(3, r.getStatus().name());
        ps.setString(4, r.getCompileLog());
        ps.setString(5, r.getRunLog());
        ps.setString(6, r.getDiffLog());
        ps.setString(7, r.getProcessedAt().toString());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) r.setId(keys.getInt(1));
        ps.close();
        return r;
    }

    public void deleteReportsByProject(int projectId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "DELETE FROM student_reports WHERE project_id=?");
        ps.setInt(1, projectId);
        ps.executeUpdate();
        ps.close();
    }

    public List<StudentReport> getReportsByProject(int projectId) throws SQLException {
        List<StudentReport> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM student_reports WHERE project_id=? ORDER BY student_id");
        ps.setInt(1, projectId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            StudentReport r = new StudentReport();
            r.setId(rs.getInt("id"));
            r.setProjectId(rs.getInt("project_id"));
            r.setStudentId(rs.getString("student_id"));
            r.setStatus(ReportStatus.valueOf(rs.getString("status")));
            r.setCompileLog(rs.getString("compile_log"));
            r.setRunLog(rs.getString("run_log"));
            r.setDiffLog(rs.getString("diff_log"));
            r.setProcessedAt(LocalDateTime.parse(rs.getString("processed_at")));
            list.add(r);
        }
        ps.close();
        return list;
    }

    public void close() {
        try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
    }
}
