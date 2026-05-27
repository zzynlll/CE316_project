package gradeflow.ui;

import gradeflow.manager.ConfigurationManager;
import gradeflow.manager.ProjectManager;
import gradeflow.model.Configuration;
import gradeflow.model.Project;
import gradeflow.model.TestCase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NewProjectDialog {

    private final ObservableList<TestCase> tcData = FXCollections.observableArrayList();

    public Optional<Project> show(Stage owner) {
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle("New Project");
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);

        TextField nameField = field("Project name");
        TextArea  descArea  = new TextArea();
        descArea.setPromptText("Optional description");
        descArea.setPrefRowCount(2);
        descArea.setStyle(Styles.TEXT_AREA);

        ComboBox<Configuration> cfgBox = new ComboBox<>();
        cfgBox.setMaxWidth(Double.MAX_VALUE);
        cfgBox.setStyle(Styles.COMBO_BOX);

        TextField dirField = field("Path to submissions folder");
        dirField.setEditable(false);

        Button browseDir = new Button("Browse…");
        browseDir.setStyle(Styles.BTN_SECONDARY);
        browseDir.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Select Submissions Directory");
            File dir = dc.showDialog(owner);
            if (dir != null) dirField.setText(dir.getAbsolutePath());
        });

        Button newCfgBtn = new Button("+ New Config");
        newCfgBtn.setStyle(Styles.BTN_SECONDARY);
        newCfgBtn.setOnAction(e -> {
            new ConfigurationDialog().show(owner, null);
            loadConfigs(cfgBox);
        });

        loadConfigs(cfgBox);

        // test case table - read only display, editing via dialog
        TableView<TestCase> tcTable = buildTcTable();

        Button addTcBtn = new Button("+ Add Test Case");
        addTcBtn.setStyle(Styles.BTN_SECONDARY);
        addTcBtn.setOnAction(e -> {
            TestCase tc = showTestCaseDialog(owner, null);
            if (tc != null) tcData.add(tc);
        });

        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color:" + Styles.BG + ";");

        form.getChildren().addAll(
            group("Project Name *", nameField),
            group("Description", descArea),
            group("Configuration *",
                  new HBox(8, cfgBox, newCfgBtn) {{ setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(cfgBox, Priority.ALWAYS); }}),
            group("Submissions Directory *",
                  new HBox(8, dirField, browseDir) {{ setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(dirField, Priority.ALWAYS); }}),
            new Separator(),
            group("Test Cases", new VBox(8, tcTable, addTcBtn))
        );

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(500);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(560);
        dialog.getDialogPane().setStyle("-fx-background-color:" + Styles.BG + ";");

        ButtonType createBtn = new ButtonType("Create Project", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel",          ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, cancelBtn);

        // Validate before allowing the dialog to close — consuming the event keeps it open.
        Button createButton = (Button) dialog.getDialogPane().lookupButton(createBtn);
        createButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().isBlank()) {
                warn("Project name is required."); event.consume();
            } else if (cfgBox.getValue() == null) {
                warn("Please select a configuration."); event.consume();
            } else if (dirField.getText().isBlank()) {
                warn("Please select a submissions directory."); event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != createBtn) return null;
            try {
                return new ProjectManager().createProject(
                        nameField.getText().trim(),
                        descArea.getText().trim(),
                        cfgBox.getValue().getId(),
                        dirField.getText().trim(),
                        new ArrayList<>(tcData));
            } catch (SQLException e) {
                warn("Save failed: " + e.getMessage());
                return null;
            }
        });

        return dialog.showAndWait();
    }

    // Opens a proper form dialog for adding/editing a test case.
    // Replaces the confusing inline TableView editing.
    private TestCase showTestCaseDialog(Stage owner, TestCase existing) {
        Dialog<TestCase> d = new Dialog<>();
        d.setTitle(existing == null ? "Add Test Case" : "Edit Test Case");
        d.initOwner(owner);
        d.initModality(Modality.WINDOW_MODAL);

        TextField descField = field("Optional label, e.g. Basic sum test");
        TextField argsField = field("e.g. 3 5 2 10");
        TextField outField  = field("Path to expected output file");
        outField.setEditable(false);

        Button browseOut = new Button("Browse…");
        browseOut.setStyle(Styles.BTN_SECONDARY);
        browseOut.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Expected Output File");
            File f = fc.showOpenDialog(owner);
            if (f != null) outField.setText(f.getAbsolutePath());
        });

        if (existing != null) {
            descField.setText(existing.getDescription() != null ? existing.getDescription() : "");
            argsField.setText(existing.getArguments()   != null ? existing.getArguments()   : "");
            outField.setText(existing.getExpectedOutputPath() != null ? existing.getExpectedOutputPath() : "");
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color:" + Styles.BG + ";");

        addRow(grid, 0, "Description",          descField);
        addRow(grid, 1, "Arguments",             argsField);
        addRow(grid, 2, "Expected Output File *",
               new HBox(8, outField, browseOut) {{ setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(outField, Priority.ALWAYS); }});

        Label hint = new Label("Arguments: the exact command-line arguments your program receives, e.g. \"3 5 2 10\"");
        hint.setStyle(Styles.LABEL_MUTED + "-fx-font-size:11px;");
        hint.setWrapText(true);
        grid.add(hint, 0, 3, 2, 1);

        d.getDialogPane().setContent(grid);
        d.getDialogPane().setPrefWidth(460);
        d.getDialogPane().setStyle("-fx-background-color:" + Styles.BG + ";");

        ButtonType saveBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        Button okButton = (Button) d.getDialogPane().lookupButton(saveBtn);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (outField.getText().isBlank()) {
                warn("Expected output file is required.");
                event.consume();
            }
        });

        d.setResultConverter(bt -> {
            if (bt != saveBtn) return null;
            TestCase tc = existing != null ? existing : new TestCase();
            tc.setDescription(descField.getText().trim());
            tc.setArguments(argsField.getText().trim());
            tc.setExpectedOutputPath(outField.getText().trim());
            return tc;
        });

        return d.showAndWait().orElse(null);
    }

    @SuppressWarnings("unchecked")
    private TableView<TestCase> buildTcTable() {
        TableView<TestCase> t = new TableView<>(tcData);
        t.setPrefHeight(150);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("No test cases yet. Click '+ Add Test Case'."));

        TableColumn<TestCase, String> desc = new TableColumn<>("Description");
        desc.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDescription() != null ? c.getValue().getDescription() : ""));

        TableColumn<TestCase, String> args = new TableColumn<>("Arguments");
        args.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getArguments() != null ? c.getValue().getArguments() : ""));

        TableColumn<TestCase, String> out = new TableColumn<>("Expected Output File");
        out.setCellValueFactory(c -> {
            String path = c.getValue().getExpectedOutputPath();
            return new SimpleStringProperty(path != null ? new File(path).getName() : "");
        });

        TableColumn<TestCase, Void> del = new TableColumn<>("");
        del.setPrefWidth(40);
        del.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#EF4444;-fx-cursor:hand;");
                btn.setOnAction(e -> tcData.remove(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        t.getColumns().addAll(desc, args, out, del);
        return t;
    }

    private void loadConfigs(ComboBox<Configuration> box) {
        try {
            List<Configuration> all = new ConfigurationManager().getAll();
            box.getItems().setAll(all);
            if (!all.isEmpty() && box.getValue() == null) box.setValue(all.get(0));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load configurations: " + e.getMessage(),
                    ButtonType.OK).showAndWait();
        }
    }

    private VBox group(String label, javafx.scene.Node control) {
        Label l = new Label(label);
        l.setStyle(Styles.LABEL_MUTED + "-fx-font-weight:bold;");
        VBox g = new VBox(5, l, control);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        return g;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(Styles.TEXT_FIELD);
        return tf;
    }

    private void addRow(GridPane grid, int row, String label, javafx.scene.Node control) {
        Label l = new Label(label);
        l.setStyle(Styles.LABEL_MUTED);
        l.setMinWidth(160);
        GridPane.setHgrow(control, Priority.ALWAYS);
        grid.add(l, 0, row);
        grid.add(control, 1, row);
    }

    private void warn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}
