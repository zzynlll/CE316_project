package gradeflow.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class HelpWindow {

    private static final Map<String, String> CONTENT = new LinkedHashMap<>();

    static {
        CONTENT.put("What is GradeFlow?",
            "GradeFlow is an integrated assignment grading environment for programming courses.\n\n" +
            "It automates the process of compiling (or interpreting), running, and evaluating student " +
            "code submissions. You define a Configuration that describes how to build and run programs " +
            "for a particular language, then create a Project that links that configuration to a folder " +
            "of student ZIP files and a set of test cases.\n\n" +
            "With a single click on Run, GradeFlow processes every submission in the folder, compares " +
            "each program's output against your expected output files, and stores colour-coded results " +
            "that you can review at any time, even after restarting the application.");

        CONTENT.put("Installation",
            "GradeFlow requires Java 17 or later.\n\n" +
            "If you received an installer, double-click the setup file and follow the on-screen prompts. " +
            "The installer places a shortcut on your desktop and deploys all required libraries, " +
            "including the JavaFX runtime, so no separate installation is needed.\n\n" +
            "GradeFlow stores its database and temporary working files in a folder called GradeFlow " +
            "inside your home directory (for example C:\\Users\\YourName\\GradeFlow on Windows). " +
            "This folder is created automatically on first run.\n\n" +
            "To uninstall, use the system uninstaller (Add/Remove Programs on Windows) and optionally " +
            "delete the GradeFlow folder from your home directory to remove all stored data.");

        CONTENT.put("Creating a Configuration",
            "A Configuration tells GradeFlow how to compile and run programs written in a specific language.\n\n" +
            "To create one:\n" +
            "  1. Open the Configurations menu and choose New Configuration.\n" +
            "  2. Fill in the required fields:\n\n" +
            "     Configuration Name: A label for your own reference (e.g. \"C – GCC 13\").\n" +
            "     Language: the programming language name (e.g. C, Java, Python).\n" +
            "     Source File Name: the exact filename students must submit inside their ZIP\n" +
            "       (e.g. main.c, Solution.java, homework.py).\n" +
            "     Run Command: the command used to execute the program after compilation\n" +
            "       (e.g. ./main  or  java Solution  or  python homework.py).\n\n" +
            "  3. For compiled languages (C, Java), also provide:\n" +
            "     Compiler Path: full path to the compiler executable\n" +
            "       (e.g. C:\\MinGW\\bin\\gcc.exe  or just gcc if it is on the system PATH).\n" +
            "     Compiler Arguments: flags passed to the compiler (e.g. -o main).\n\n" +
            "  4. Select the Language Type: Compiled or Interpreted. Choosing Interpreted\n" +
            "     disables the compiler fields.\n" +
            "  5. Choose a Comparison Method: EXACT (character-perfect match) or TRIMMED\n" +
            "     (ignores leading/trailing whitespace and blank lines).\n" +
            "  6. Click Save Configuration.");

        CONTENT.put("Editing",
            "To edit an existing configuration:\n\n" +
            "  1. Open Configurations → Edit Configuration.\n" +
            "  2. Choose the configuration you want to modify from the list.\n" +
            "  3. Update the relevant fields in the dialog.\n" +
            "  4. Click Save Configuration.\n\n" +
            "Important: editing a configuration affects all projects that use it. " +
            "If you have already graded a project, you should re-run grading after changing " +
            "the configuration to ensure results reflect the updated settings.");

        CONTENT.put("Import & Export",
            "Configurations can be exported to JSON files for backup or sharing with colleagues.\n\n" +
            "To export:\n" +
            "  1. Open Configurations → Export to JSON.\n" +
            "  2. Choose the configuration to export.\n" +
            "  3. Select a destination file in the save dialog.\n\n" +
            "To import:\n" +
            "  1. Open Configurations → Import from JSON.\n" +
            "  2. Browse to the previously exported JSON file.\n" +
            "  3. GradeFlow imports the configuration into the database.\n\n" +
            "If the imported configuration has the same name as an existing one, the suffix " +
            "\" (imported)\" is appended automatically to avoid conflicts.");

        CONTENT.put("Creating a Project",
            "A Project ties together a configuration, a set of test cases, and a directory " +
            "of student ZIP submissions.\n\n" +
            "To create a project:\n" +
            "  1. Choose File → New Project (or click + New Project in the sidebar).\n" +
            "  2. Enter a Project Name (required) and an optional Description.\n" +
            "  3. Select a Configuration from the dropdown. If none exist yet, click\n" +
            "     + New Config to create one inline.\n" +
            "  4. Click Browse next to Submissions Directory and navigate to the folder\n" +
            "     that contains the student ZIP files.\n" +
            "  5. Add at least one test case (see Adding Test Cases below).\n" +
            "  6. Click Create Project.\n\n" +
            "The project is saved to the database and appears in the sidebar. " +
            "It can be opened at any future session via the sidebar or File → Open Project.");

        CONTENT.put("Adding Test Cases",
            "A Test Case specifies:\n" +
            "  • Arguments: the command-line arguments passed to the student's program.\n" +
            "  • Expected Output File: a plain-text file containing the correct output.\n" +
            "  • Description: an optional label (e.g. \"Edge case: empty input\").\n\n" +
            "To add test cases when creating a new project:\n" +
            "  Click + Add Test Case in the New Project dialog, fill in the fields,\n" +
            "  and click Add. Repeat for each test case.\n\n" +
            "To add, edit, or remove test cases for an existing project:\n" +
            "  1. Load the project (click it in the sidebar).\n" +
            "  2. Select the Test Cases tab in the main window.\n" +
            "  3. Use the + Add Test Case button at the bottom to add a new one.\n" +
            "  4. Click Edit on any row to modify it.\n" +
            "  5. Click Delete on any row to remove it.\n\n" +
            "Changes take effect immediately for the next Run. " +
            "GradeFlow runs all test cases for every student and reports a combined result.");

        CONTENT.put("Running Grading",
            "Once a project is open and has at least one test case, click the Run button " +
            "(or Project → Run).\n\n" +
            "GradeFlow will:\n" +
            "  1. Scan the submissions directory for .zip files.\n" +
            "     Each file's name (without .zip) becomes the Student ID.\n" +
            "  2. Extract each ZIP to a temporary working directory.\n" +
            "  3. Verify that the expected source file is present.\n" +
            "  4. Compile the source (for compiled languages) using the configuration.\n" +
            "  5. Run the program once for each test case, capturing stdout and stderr.\n" +
            "  6. Compare the program's output to the expected output file.\n" +
            "  7. Record the result (PASS, FAIL, COMPILE_ERROR, etc.) in the database.\n\n" +
            "Progress is shown in the status bar at the bottom. When complete, the Results " +
            "tab is selected automatically.\n\n" +
            "Re-grading: clicking Run again replaces all previous results for the project.");

        CONTENT.put("Understanding Statuses",
            "Each student submission receives one of the following status badges:\n\n" +
            "  PASS \n" +
            "    The program compiled successfully and produced the correct output for\n" +
            "    all test cases.\n\n" +
            "  FAIL \n" +
            "    The program ran but produced incorrect output for one or more test cases.\n" +
            "    Open Details → Diff Log to see exactly what differed.\n\n" +
            "  COMPILE_ERROR \n" +
            "    The compiler rejected the source code. Open Details → Compile Log for\n" +
            "    the full error message.\n\n" +
            "  RUNTIME_ERROR \n" +
            "    The program crashed or exited with a non-zero code while producing no\n" +
            "    output. See Details → Run Log for stderr.\n\n" +
            "  MISSING_FILE \n" +
            "    The expected source file (as named in the configuration) was not found\n" +
            "    inside the extracted ZIP.");

        CONTENT.put("Viewing Logs",
            "Click the Details button on any row in the Results tab to open the detail view.\n\n" +
            "The detail view contains three log tabs:\n\n" +
            "  Compile Log\n" +
            "    The combined stdout and stderr from the compiler. Useful for diagnosing\n" +
            "    COMPILE_ERROR status. Empty for interpreted languages.\n\n" +
            "  Run Log\n" +
            "    The combined stdout and stderr captured while running the program.\n" +
            "    When multiple test cases are defined, each test case's output is shown\n" +
            "    under its own label.\n\n" +
            "  Diff Log\n" +
            "    A comparison between the expected and actual output for each test case.\n" +
            "    Lines prefixed with < are the expected lines; lines prefixed with >\n" +
            "    are the actual output. Test cases that passed show [PASS].\n\n" +
            "The dialog opens on the most relevant tab for the status: Compile Log for\n" +
            "COMPILE_ERROR, Run Log for RUNTIME_ERROR, Diff Log otherwise.");

        CONTENT.put("Common Errors",
            "COMPILE_ERROR\n" +
            "  • The compiler path in the configuration is wrong or the compiler is not\n" +
            "    on the PATH.\n" +
            "  • The student's source file has syntax errors. Read the Compile Log.\n" +
            "  • The Compiler Arguments are incorrect (e.g. wrong output flag for the\n" +
            "    language).\n\n" +
            "MISSING_FILE\n" +
            "  • The student submitted a file with a different name than what is specified\n" +
            "    in the configuration's Source File Name field.\n" +
            "  • The student zipped a subfolder instead of placing the file at the root\n" +
            "    of the ZIP. GradeFlow searches recursively, but a common pattern is\n" +
            "    submitting project/src/main.c — make sure the Source File Name matches\n" +
            "    the relative path (or ask students to flatten their ZIP).\n\n" +
            "ZIP_ERROR\n" +
            "  • The ZIP file is corrupt or password-protected. Inspect it manually.\n\n" +
            "TIMEOUT\n" +
            "  • The student's program contains an infinite loop or is waiting for input.\n" +
            "  • Make sure the Arguments field in the test case matches what the program\n" +
            "    expects — if the program reads from stdin and no piped input is provided,\n" +
            "    it will block and time out.\n\n" +
            "Could not start process\n" +
            "  • The Run Command is incorrect or the compiled binary was not produced.\n" +
            "  • Verify that the Compiler Arguments produce the expected output filename\n" +
            "    and that the Run Command matches it (e.g. -o main → ./main).");

        CONTENT.put("FAQ",
            "Q: Can I use GradeFlow with Python?\n" +
            "A: Yes. In the configuration dialog, select Interpreted, leave the Compiler Path\n" +
            "   blank, and set Run Command to e.g.  python main.py  or  python3 main.py.\n\n" +
            "Q: What format should the expected output file be in?\n" +
            "A: Plain text (.txt). GradeFlow compares the program's standard output to this\n" +
            "   file. Use TRIMMED comparison mode to ignore trailing whitespace or extra\n" +
            "   blank lines at the end of the output.\n\n" +
            "Q: Can I re-grade after fixing a configuration or test case?\n" +
            "A: Yes. Simply click Run again. Previous reports for the project are replaced.\n\n" +
            "Q: Can I have multiple test cases per project?\n" +
            "A: Yes. Add as many test cases as needed in the Test Cases tab. GradeFlow runs\n" +
            "   every test case for every student. The final status is PASS only if all\n" +
            "   test cases pass.\n\n" +
            "Q: Where is my data stored?\n" +
            "A: All projects, configurations, and results are stored in  gradeflow.db  inside\n" +
            "   the GradeFlow folder in your home directory. Back up this file to preserve\n" +
            "   your data.\n\n" +
            "Q: Can I share a configuration with a colleague?\n" +
            "A: Yes. Use Configurations → Export to JSON to save the configuration to a file,\n" +
            "   then send that file to your colleague, who can import it via\n" +
            "   Configurations → Import from JSON.");
    }

    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("GradeFlow — User Manual");
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);

        TextField searchField = new TextField();
        searchField.setPromptText("Search in manual…");
        searchField.setStyle(Styles.TEXT_FIELD);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("Search");
        searchBtn.setStyle(Styles.BTN_PRIMARY);

        HBox searchBar = new HBox(8, searchField, searchBtn);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(10, 12, 10, 12));
        searchBar.setStyle("-fx-background-color:" + Styles.SURFACE + ";-fx-border-color:" +
                           Styles.BORDER + ";-fx-border-width:0 0 1 0;");

        TreeItem<String> root = new TreeItem<>("Contents");
        root.setExpanded(true);

        String[][] sections = {
            {"Getting Started",  "What is GradeFlow?", "Installation"},
            {"Configurations",   "Creating a Configuration", "Editing", "Import & Export"},
            {"Projects",         "Creating a Project", "Adding Test Cases", "Running Grading"},
            {"Results",          "Understanding Statuses", "Viewing Logs"},
            {"Troubleshooting",  "Common Errors", "FAQ"},
        };

        for (String[] sec : sections) {
            TreeItem<String> parent = new TreeItem<>(sec[0]);
            parent.setExpanded(true);
            for (int i = 1; i < sec.length; i++)
                parent.getChildren().add(new TreeItem<>(sec[i]));
            root.getChildren().add(parent);
        }

        TreeView<String> tree = new TreeView<>(root);
        tree.setShowRoot(false);
        tree.setStyle("-fx-background-color:" + Styles.BG + ";");
        tree.setPrefWidth(200);

        Label heading = new Label("Welcome to GradeFlow");
        heading.setStyle(Styles.LABEL_BOLD + "-fx-font-size:18px;");

        TextArea body = new TextArea(
            "GradeFlow is an integrated environment for managing and grading " +
            "programming assignments.\n\n" +
            "Use the tree on the left to navigate the manual, or type a keyword " +
            "in the search bar above and click Search.\n\n" +
            "Quick start:\n" +
            "  1. Create a Configuration (Configurations menu).\n" +
            "  2. Create a Project (File → New Project) and add test cases.\n" +
            "  3. Click Run to grade all submissions in the project's directory.\n" +
            "  4. Review colour-coded results in the Results tab."
        );
        body.setEditable(false);
        body.setWrapText(true);
        body.setStyle("-fx-font-size:13px;-fx-text-fill:" + Styles.TEXT + ";" +
                      "-fx-background-color:white;-fx-control-inner-background:white;" +
                      "-fx-background-radius:0;-fx-border-color:transparent;");
        VBox.setVgrow(body, Priority.ALWAYS);

        Label tip = new Label("Tip: Use TRIMMED comparison mode if students might have trailing newlines or extra spaces in their output.");
        tip.setStyle("-fx-background-color:#EFF6FF;-fx-text-fill:#1E40AF;" +
                     "-fx-background-radius:6;-fx-padding:10 14 10 14;-fx-font-size:12px;");
        tip.setWrapText(true);

        VBox contentArea = new VBox(12, heading, new Separator(), body, tip);
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color:white;");
        VBox.setVgrow(body, Priority.ALWAYS);

        tree.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null || !sel.isLeaf()) return;
            String topic = sel.getValue();
            heading.setText(topic);
            String text = CONTENT.get(topic);
            body.setText(text != null ? text : "(No content available for this topic.)");
            tip.setVisible(false);
            tip.setManaged(false);
        });

        searchBtn.setOnAction(e -> {
            String query = searchField.getText().trim().toLowerCase();
            if (query.isEmpty()) return;
            StringBuilder found = new StringBuilder();
            for (Map.Entry<String, String> entry : CONTENT.entrySet()) {
                if (entry.getKey().toLowerCase().contains(query) ||
                    entry.getValue().toLowerCase().contains(query)) {
                    found.append("=== ").append(entry.getKey()).append(" ===\n\n");
                    found.append(entry.getValue()).append("\n\n\n");
                }
            }
            heading.setText("Search results for: \"" + searchField.getText().trim() + "\"");
            body.setText(found.length() > 0 ? found.toString().trim()
                    : "No results found for: " + searchField.getText().trim());
            tip.setVisible(false);
            tip.setManaged(false);
            tree.getSelectionModel().clearSelection();
        });

        searchField.setOnAction(e -> searchBtn.fire());

        ScrollPane contentScroll = new ScrollPane(contentArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setFitToHeight(true);
        contentScroll.setStyle("-fx-background-color:white;-fx-background:white;");

        SplitPane split = new SplitPane(tree, contentScroll);
        split.setDividerPositions(0.28);
        VBox.setVgrow(split, Priority.ALWAYS);

        Label statusBar = new Label("GradeFlow User Manual — Version 1.0");
        statusBar.setStyle(Styles.LABEL_MUTED + "-fx-padding:5 12 5 12;-fx-border-color:" +
                           Styles.BORDER + ";-fx-border-width:1 0 0 0;");
        statusBar.setMaxWidth(Double.MAX_VALUE);

        VBox layout = new VBox(searchBar, split, statusBar);
        VBox.setVgrow(split, Priority.ALWAYS);

        stage.setScene(new Scene(layout, 820, 580));
        stage.setMinWidth(640);
        stage.setMinHeight(440);
        stage.show();
    }
}
