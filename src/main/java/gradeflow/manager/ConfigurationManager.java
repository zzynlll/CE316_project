package gradeflow.manager;

import gradeflow.db.DatabaseManager;
import gradeflow.model.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class ConfigurationManager {

    
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Configuration create(Configuration c) throws SQLException {
        return db.saveConfiguration(c);
    }

    public void update(Configuration c) throws SQLException {
        db.updateConfiguration(c);
    }

    public void delete(int id) throws SQLException {
        db.deleteConfiguration(id);
    }

    public List<Configuration> getAll() throws SQLException {
        return db.getAllConfigurations();
    }

    public Configuration getById(int id) throws SQLException {
        return db.getConfigurationById(id);
    }

    
    public void exportToFile(int id, Path dest) throws SQLException, IOException {
        Configuration c = db.getConfigurationById(id);
        if (c == null) throw new IllegalArgumentException("Configuration not found.");
        Files.writeString(dest, c.toJson());
    }

    
    public Configuration importFromFile(Path src) throws IOException, SQLException {
        String json = Files.readString(src);
        Configuration c = Configuration.fromJson(json);
        c.setId(0);

        boolean nameTaken = db.getAllConfigurations().stream()
                .anyMatch(e -> e.getName().equalsIgnoreCase(c.getName()));
        if (nameTaken) {
            c.setName(c.getName() + " (imported)");
        }
        return db.saveConfiguration(c);
    }
}
