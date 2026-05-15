package gradeflow.ui;

import gradeflow.manager.ConfigurationManager;
import gradeflow.manager.ProjectManager;
import gradeflow.model.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainWindow {

    private final ProjectManager pm = new ProjectManager();

    private Stage    stage;
    private Project  currentProject;

    private VBox     projectListBox;
    private Label    projectNameLabel;
    private Button   runButton;
    private Label    statusLabel;
    private ProgressBar progressBar;
    private TabPane  tabPane;

    private final ObservableList<StudentReport> reportData   = FXCollections.observableArrayList();
    private final ObservableList<TestCase>      testCaseData = FXCollections.observableArrayList();

    public void show(Stage primaryStage) {
        this.stage = primaryStage;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:" + Styles.BG + ";");
        root.setTop(buildMenuBar());
        root.setLeft(buildSidebar());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());

        primaryStage.setScene(new Scene(root, 1200, 720));
        primaryStage.setTitle("GradeFlow");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        refreshSidebar();
    }

    // Menu bar 

    private MenuBar buildMenuBar() {
        MenuBar bar = new MenuBar();
        bar.setStyle("-fx-background-color:" + Styles.SURFACE + ";-fx-border-color:" +
                     Styles.BORDER + ";-fx-border-width:0 0 1 0;");

        Menu fileMenu = new Menu("File");
        MenuItem newProj  = new MenuItem("New Project…");
        MenuItem openProj = new MenuItem("Open Project");
        MenuItem exitItem = new MenuItem("Exit");
        newProj.setOnAction(e  -> openNewProjectDialog());
        openProj.setOnAction(e -> openPickProjectDialog());
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(newProj, openProj, new SeparatorMenuItem(), exitItem);

        Menu cfgMenu = new Menu("Configurations");
        MenuItem newCfg    = new MenuItem("New Configuration…");
        MenuItem editCfg   = new MenuItem("Edit Configuration…");
        MenuItem deleteCfg = new MenuItem("Delete Configuration…");
        MenuItem importCfg = new MenuItem("Import from JSON…");
        MenuItem exportCfg = new MenuItem("Export to JSON…");
        newCfg.setOnAction(e    -> new ConfigurationDialog().show(stage, null));
        editCfg.setOnAction(e   -> pickAndEditConfig());
        deleteCfg.setOnAction(e -> pickAndDeleteConfig());
        importCfg.setOnAction(e -> importConfig());
        exportCfg.setOnAction(e -> exportConfig());
        cfgMenu.getItems().addAll(newCfg, editCfg, deleteCfg,
                new SeparatorMenuItem(), importCfg, exportCfg);

        Menu projectMenu = new Menu("Project");
        MenuItem runItem     = new MenuItem("Run");
        MenuItem resultsItem = new MenuItem("View Results");
        MenuItem editProj    = new MenuItem("Edit Project…");
        MenuItem deleteProj  = new MenuItem("Delete Project…");
        runItem.setOnAction(e     -> runPipeline());
        resultsItem.setOnAction(e -> tabPane.getSelectionModel().select(0));
        editProj.setOnAction(e   -> openEditProjectDialog());
        deleteProj.setOnAction(e -> deleteCurrentProject());
        projectMenu.getItems().addAll(runItem, resultsItem,
                new SeparatorMenuItem(), editProj, deleteProj);

        Menu helpMenu  = new Menu("Help");
        MenuItem manual = new MenuItem("User Manual");
        manual.setOnAction(e -> new HelpWindow().show(stage));
        helpMenu.getItems().add(manual);

        bar.getMenus().addAll(fileMenu, cfgMenu, projectMenu, helpMenu);
        return bar;
    }

    // Sidebar 

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.setStyle("-fx-background-color:" + Styles.SIDEBAR_BG + ";-fx-min-width:230;-fx-max-width:230;");

        VBox header = new VBox(4);
        header.setPadding(new Insets(24, 16, 20, 16));
        header.setStyle("-fx-border-color:#334155;-fx-border-width:0 0 1 0;");

        Label appName = new Label("GradeFlow");
        appName.setStyle("-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;");
        Label tagline = new Label("Assignment Environment");
        tagline.setStyle("-fx-text-fill:" + Styles.TEXT_MUTED + ";-fx-font-size:11px;");
        header.getChildren().addAll(appName, tagline);

        Label sectionLabel = new Label("PROJECTS");
        sectionLabel.setStyle("-fx-text-fill:#475569;-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:16 16 6 16;");

        projectListBox = new VBox(2);
        projectListBox.setPadding(new Insets(0, 8, 0, 8));

        ScrollPane scroll = new ScrollPane(projectListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox bottom = new VBox(8);
        bottom.setPadding(new Insets(12, 8, 16, 8));
        bottom.setStyle("-fx-border-color:#334155;-fx-border-width:1 0 0 0;");

        Button btnNew  = sidebarBtn("＋  New Project");
        Button btnOpen = sidebarBtn("📂  Open Project");
        btnNew.setOnAction(e  -> openNewProjectDialog());
        btnOpen.setOnAction(e -> openPickProjectDialog());
        bottom.getChildren().addAll(btnNew, btnOpen);

        sidebar.getChildren().addAll(header, sectionLabel, scroll, bottom);
        return sidebar;
    }

    private Button sidebarBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle(Styles.BTN_SIDEBAR);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnMouseEntered(e -> btn.setStyle(Styles.BTN_SIDEBAR_HOVER));
        btn.setOnMouseExited(e  -> btn.setStyle(Styles.BTN_SIDEBAR));
        return btn;
    }

    private void refreshSidebar() {
        projectListBox.getChildren().clear();
        try {
            for (Project p : pm.getAllProjects()) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(8, 12, 8, 12));
                row.setStyle("-fx-background-radius:6;-fx-cursor:hand;");

                Circle dot = new Circle(4, Color.web(Styles.PRIMARY));
                Label  lbl = new Label(p.getName());
                lbl.setStyle("-fx-text-fill:#CBD5E1;-fx-font-size:13px;");
                lbl.setMaxWidth(160);

                row.getChildren().addAll(dot, lbl);
                row.setOnMouseEntered(e -> row.setStyle(
                        "-fx-background-color:#334155;-fx-background-radius:6;-fx-cursor:hand;"));
                row.setOnMouseExited(e  -> row.setStyle(
                        "-fx-background-radius:6;-fx-cursor:hand;"));
                row.setOnMouseClicked(e -> loadProject(p.getId()));
                projectListBox.getChildren().add(row);
            }
        } catch (SQLException e) {
            showError("Failed to load projects: " + e.getMessage());
        }
    }

    // Main content

    private VBox buildCenter() {
        VBox center = new VBox();
        center.setStyle("-fx-background-color:" + Styles.BG + ";");

        
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 16, 10, 16));
        toolbar.setStyle("-fx-background-color:" + Styles.SURFACE + ";-fx-border-color:" +
                         Styles.BORDER + ";-fx-border-width:0 0 1 0;");

        Label currLabel = new Label("Current Project:");
        currLabel.setStyle(Styles.LABEL_MUTED);

        projectNameLabel = new Label("No project open");
        projectNameLabel.setStyle(Styles.LABEL_BOLD);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        runButton = new Button("▶  Run");
        runButton.setStyle(Styles.BTN_PRIMARY);
        runButton.setDisable(true);
        runButton.setOnMouseEntered(e -> { if (!runButton.isDisabled()) runButton.setStyle(Styles.BTN_PRIMARY_HOVER); });
        runButton.setOnMouseExited(e  -> { if (!runButton.isDisabled()) runButton.setStyle(Styles.BTN_PRIMARY); });
        runButton.setOnAction(e -> runPipeline());

        toolbar.getChildren().addAll(currLabel, projectNameLabel, spacer, runButton);

        
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        tabPane.getTabs().addAll(
            new Tab("Results",    buildResultsTab()),
            new Tab("Test Cases", buildTestCasesTab())
        );

        center.getChildren().addAll(toolbar, tabPane);
        return center;
    }

    @SuppressWarnings("unchecked")
    private VBox buildResultsTab() {
        TableView<StudentReport> table = new TableView<>(reportData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No results yet. Open a project and click Run."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<StudentReport, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill:" + Styles.TEXT + ";");
            }
        });

        TableColumn<StudentReport, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().name()));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                badge.setStyle(Styles.badgeFor(item));
                setGraphic(badge);
            }
        });

        TableColumn<StudentReport, String> logCol = new TableColumn<>("Compile Log");
        logCol.setCellValueFactory(c -> {
            String log = c.getValue().getCompileLog();
            if (log == null || log.isBlank()) return new SimpleStringProperty("—");
            String first = log.lines().findFirst().orElse("");
            return new SimpleStringProperty(first.length() > 55 ? first.substring(0, 55) + "…" : first);
        });
        logCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill:" + Styles.TEXT + ";");
            }
        });

        TableColumn<StudentReport, Void> detailsCol = new TableColumn<>("");
        detailsCol.setPrefWidth(90);
        detailsCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Details");
            {
                btn.setStyle("-fx-background-color:" + Styles.PRIMARY + ";-fx-text-fill:white;" +
                             "-fx-background-radius:5;-fx-font-size:11px;-fx-padding:4 10 4 10;" +
                             "-fx-cursor:hand;");
                btn.setOnAction(e -> {
                    StudentReport r = getTableView().getItems().get(getIndex());
                    new ResultDetailsDialog().show(stage, r);
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(idCol, statusCol, logCol, detailsCol);

        VBox wrap = new VBox(table);
        wrap.setPadding(new Insets(14));
        wrap.setStyle("-fx-background-color:" + Styles.BG + ";");
        VBox.setVgrow(wrap, Priority.ALWAYS);
        return wrap;
    }

    @SuppressWarnings("unchecked")
    private VBox buildTestCasesTab() {
        TableView<TestCase> table = new TableView<>(testCaseData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No test cases for this project."));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<TestCase, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        descCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill:" + Styles.TEXT + ";");
            }
        });

        TableColumn<TestCase, String> argsCol = new TableColumn<>("Arguments");
        argsCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArguments()));
        argsCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill:" + Styles.TEXT + ";");
            }
        });

        TableColumn<TestCase, String> outCol = new TableColumn<>("Expected Output File");
        outCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpectedOutputPath()));
        outCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle("-fx-text-fill:" + Styles.TEXT + ";");
            }
        });

        table.getColumns().addAll(descCol, argsCol, outCol);

        VBox wrap = new VBox(table);
        wrap.setPadding(new Insets(14));
        wrap.setStyle("-fx-background-color:" + Styles.BG + ";");
        VBox.setVgrow(wrap, Priority.ALWAYS);
        return wrap;
    }

    // Status bar 

    private HBox buildStatusBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 16, 6, 16));
        bar.setStyle("-fx-background-color:" + Styles.SURFACE + ";-fx-border-color:" +
                     Styles.BORDER + ";-fx-border-width:1 0 0 0;");

        statusLabel = new Label("Ready");
        statusLabel.setStyle(Styles.LABEL_MUTED);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(180);
        progressBar.setStyle("-fx-accent:" + Styles.PRIMARY + ";");
        progressBar.setVisible(false);

        bar.getChildren().addAll(statusLabel, sp, progressBar);
        return bar;
    }

    // Actions 
    private void loadProject(int id) {
        try {
            currentProject = pm.openProject(id);
            projectNameLabel.setText(currentProject.getName());
            runButton.setDisable(false);
            reportData.setAll(currentProject.getReports());
            testCaseData.setAll(currentProject.getTestCases());
            setStatus("Opened: " + currentProject.getName());
        } catch (SQLException e) {
            showError("Failed to open project: " + e.getMessage());
        }
    }

    private void runPipeline() {
        if (currentProject == null) return;
        runButton.setDisable(true);
        reportData.clear();
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        setStatus("Running…");

        final int[] counter = {0, 0};

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                pm.runProject(currentProject, (sid, done, total, report) -> {
                    counter[0] = done; counter[1] = total;
                    Platform.runLater(() -> {
                        reportData.add(report);
                        progressBar.setProgress((double) done / total);
                        setStatus("Processing " + done + "/" + total + " — " + sid);
                    });
                });
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            runButton.setDisable(false);
            progressBar.setVisible(false);   // hide the bar when done
            setStatus("Done — " + counter[0] + " submission(s) processed.");
            tabPane.getSelectionModel().select(0);
        });

        task.setOnFailed(e -> {
            runButton.setDisable(false);
            progressBar.setVisible(false);
            showError("Pipeline error: " + task.getException().getMessage());
            setStatus("Run failed.");
        });

        new Thread(task, "GradeFlow-Pipeline").start();
    }

    private void openNewProjectDialog() {
        Optional<Project> result = new NewProjectDialog().show(stage);
        result.ifPresent(p -> {
            refreshSidebar();
            loadProject(p.getId());
        });
    }

    private void openPickProjectDialog() {
        try {
            List<Project> all = pm.getAllProjects();
            if (all.isEmpty()) { showInfo("No saved projects found."); return; }
            ChoiceDialog<Project> d = new ChoiceDialog<>(all.get(0), all);
            d.setTitle("Open Project");
            d.setHeaderText("Select a project:");
            d.initOwner(stage);
            d.showAndWait().ifPresent(p -> loadProject(p.getId()));
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void openEditProjectDialog() {
        if (currentProject == null) {
            showInfo("Open a project first to edit it.");
            return;
        }

        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle("Edit Project");
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);

        TextField nameField = new TextField(currentProject.getName());
        nameField.setPromptText("Project name");
        nameField.setStyle(Styles.TEXT_FIELD);

        TextArea descArea = new TextArea(currentProject.getDescription());
        descArea.setPromptText("Optional description");
        descArea.setPrefRowCount(2);
        descArea.setStyle(Styles.TEXT_AREA);

        ComboBox<Configuration> cfgBox = new ComboBox<>();
        cfgBox.setMaxWidth(Double.MAX_VALUE);
        cfgBox.setStyle("-fx-font-size:13px;");
        try {
            loadConfigs(cfgBox);
            if (currentProject.getConfiguration() != null) {
                cfgBox.setValue(currentProject.getConfiguration());
            } else {
                for (Configuration c : cfgBox.getItems()) {
                    if (c.getId() == currentProject.getConfigurationId()) {
                        cfgBox.setValue(c);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            showError("Failed to load configurations: " + e.getMessage());
            return;
        }

        TextField dirField = new TextField(currentProject.getSubmissionsDir());
        dirField.setEditable(false);
        dirField.setStyle(Styles.TEXT_FIELD);

        Button browseDir = new Button("Browse…");
        browseDir.setStyle(Styles.BTN_SECONDARY);
        browseDir.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Submissions Directory");
            File dir = dc.showDialog(stage);
            if (dir != null) dirField.setText(dir.getAbsolutePath());
        });

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color:" + Styles.BG + ";");

        form.getChildren().addAll(
                group("Project Name *", nameField),
                group("Description", descArea),
                group("Configuration *", cfgBox),
                group("Submissions Directory *",
                        new HBox(8, dirField, browseDir) {{ setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(dirField, Priority.ALWAYS); }}));

        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(560);
        dialog.getDialogPane().setStyle("-fx-background-color:" + Styles.BG + ";");

        ButtonType saveBtn = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt != saveBtn) return null;
            if (nameField.getText().isBlank()) { warn("Project name is required."); return null; }
            if (cfgBox.getValue() == null) { warn("Please select a configuration."); return null; }
            if (dirField.getText().isBlank()) { warn("Please select a submissions directory."); return null; }

            currentProject.setName(nameField.getText().trim());
            currentProject.setDescription(descArea.getText().trim());
            currentProject.setConfigurationId(cfgBox.getValue().getId());
            currentProject.setConfiguration(cfgBox.getValue());
            currentProject.setSubmissionsDir(dirField.getText().trim());
            return currentProject;
        });

        dialog.showAndWait().ifPresent(p -> {
            try {
                pm.saveProject(p);
                refreshSidebar();
                loadProject(p.getId());
                setStatus("Project updated: " + p.getName());
            } catch (SQLException ex) {
                showError("Failed to save project: " + ex.getMessage());
            }
        });
    }

    private void deleteCurrentProject() {
        if (currentProject == null) {
            showInfo("Open a project first to delete it.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete project \"" + currentProject.getName() + "\"? This cannot be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete Project");
        confirm.initOwner(stage);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                pm.deleteProject(currentProject.getId());
                currentProject = null;
                projectNameLabel.setText("No project open");
                runButton.setDisable(true);
                reportData.clear();
                testCaseData.clear();
                refreshSidebar();
                setStatus("Project deleted.");
            } catch (SQLException ex) {
                showError("Failed to delete project: " + ex.getMessage());
            }
        });
    }

    private void loadConfigs(ComboBox<Configuration> box) throws SQLException {
        List<Configuration> all = new ConfigurationManager().getAll();
        box.getItems().setAll(all);
        if (!all.isEmpty() && box.getValue() == null) box.setValue(all.get(0));
    }

    private void pickAndEditConfig() {
        try {
            List<Configuration> all = new ConfigurationManager().getAll();
            if (all.isEmpty()) { showInfo("No configurations found."); return; }
            ChoiceDialog<Configuration> d = new ChoiceDialog<>(all.get(0), all);
            d.setTitle("Edit Configuration"); d.setHeaderText("Choose one to edit:");
            d.initOwner(stage);
            d.showAndWait().ifPresent(c -> new ConfigurationDialog().show(stage, c));
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void pickAndDeleteConfig() {
        try {
            ConfigurationManager mgr = new ConfigurationManager();
            List<Configuration> all = mgr.getAll();
            if (all.isEmpty()) { showInfo("No configurations to delete."); return; }
            ChoiceDialog<Configuration> d = new ChoiceDialog<>(all.get(0), all);
            d.setTitle("Delete Configuration"); d.setHeaderText("Choose one to delete:");
            d.initOwner(stage);
            d.showAndWait().ifPresent(c -> {
                Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                        "Delete \"" + c.getName() + "\"?", ButtonType.YES, ButtonType.NO);
                conf.initOwner(stage);
                conf.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
                    try { mgr.delete(c.getId()); }
                    catch (SQLException ex) { showError(ex.getMessage()); }
                });
            });
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void importConfig() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Import Configuration");
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("JSON", "*.json"));
        java.io.File f = fc.showOpenDialog(stage);
        if (f == null) return;
        try {
            new ConfigurationManager().importFromFile(f.toPath());
            showInfo("Configuration imported.");
        } catch (Exception e) { showError("Import failed: " + e.getMessage()); }
    }

    private void exportConfig() {
        try {
            ConfigurationManager mgr = new ConfigurationManager();
            List<Configuration> all = mgr.getAll();
            if (all.isEmpty()) { showInfo("No configurations to export."); return; }
            ChoiceDialog<Configuration> d = new ChoiceDialog<>(all.get(0), all);
            d.setTitle("Export Configuration"); d.setHeaderText("Choose one to export:");
            d.initOwner(stage);
            d.showAndWait().ifPresent(c -> {
                javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                fc.setTitle("Save As");
                fc.setInitialFileName(c.getName() + ".json");
                fc.getExtensionFilters().add(
                        new javafx.stage.FileChooser.ExtensionFilter("JSON", "*.json"));
                java.io.File f = fc.showSaveDialog(stage);
                if (f == null) return;
                try { mgr.exportToFile(c.getId(), f.toPath()); showInfo("Exported."); }
                catch (Exception ex) { showError(ex.getMessage()); }
            });
        } catch (SQLException e) { showError(e.getMessage()); }
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle("Error"); a.initOwner(stage); a.showAndWait();
    }

    private void warn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle("Warning"); a.initOwner(stage); a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle("Info"); a.initOwner(stage); a.showAndWait();
    }
}
