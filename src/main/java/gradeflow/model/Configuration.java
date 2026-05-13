package gradeflow.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Configuration {

    private int              id;
    private String           name;
    private String           language;
    private String           compilerPath;
    private String           compilerArgs;
    private String           sourceFileName;
    private String           runCommand;
    private boolean          isCompiled;
    private ComparisonMethod comparisonMethod;

    public Configuration() {
        this.isCompiled        = true;
        this.comparisonMethod  = ComparisonMethod.TRIMMED;
    }

    public Configuration(String name, String language, String compilerPath,
                         String compilerArgs, String sourceFileName, String runCommand,
                         boolean isCompiled, ComparisonMethod comparisonMethod) {
        this.name             = name;
        this.language         = language;
        this.compilerPath     = compilerPath;
        this.compilerArgs     = compilerArgs;
        this.sourceFileName   = sourceFileName;
        this.runCommand       = runCommand;
        this.isCompiled       = isCompiled;
        this.comparisonMethod = comparisonMethod;
    }

   
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String toJson() {
        return gson.toJson(this);
    }

    public static Configuration fromJson(String json) {
        return gson.fromJson(json, Configuration.class);
    }

    public int              getId()                    { return id; }
    public void             setId(int id)              { this.id = id; }

    public String           getName()                  { return name; }
    public void             setName(String n)          { this.name = n; }

    public String           getLanguage()              { return language; }
    public void             setLanguage(String l)      { this.language = l; }

    public String           getCompilerPath()          { return compilerPath; }
    public void             setCompilerPath(String p)  { this.compilerPath = p; }

    public String           getCompilerArgs()          { return compilerArgs; }
    public void             setCompilerArgs(String a)  { this.compilerArgs = a; }

    public String           getSourceFileName()        { return sourceFileName; }
    public void             setSourceFileName(String s){ this.sourceFileName = s; }

    public String           getRunCommand()            { return runCommand; }
    public void             setRunCommand(String r)    { this.runCommand = r; }

    public boolean          isCompiled()               { return isCompiled; }
    public void             setCompiled(boolean c)     { this.isCompiled = c; }

    public ComparisonMethod getComparisonMethod()      { return comparisonMethod; }
    public void             setComparisonMethod(ComparisonMethod m) { this.comparisonMethod = m; }

    @Override public String toString() { return name + " (" + language + ")"; }
}
