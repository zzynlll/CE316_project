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
import javafx.scene.control.cell.TextFieldTableCell;
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
        cfgBox.setStyle("-fx-font-size:13px;");

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

        TableView<TestCase> tcTable = buildTcTable(owner);

        Button addTcBtn = new Button("+ Add Test Case");
        addTcBtn.setStyle(Styles.BTN_SECONDARY);
        addTcBtn.setOnAction(e -> tcData.add(new TestCase()));

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

        dialog.setResultConverter(bt -> {
            if (bt != createBtn) return null;
            if (nameField.getText().isBlank())   { warn("Project name is required."); return null; }
            if (cfgBox.getValue() == null)        { warn("Please select a configuration."); return null; }
            if (dirField.getText().isBlank())     { warn("Please select a submissions directory."); return null; }

            try {
                List<TestCase> tcs = new ArrayList<>(tcData);
                return new ProjectManager().createProject(
                        nameField.getText().trim(),
                        descArea.getText().trim(),
                        cfgBox.getValue().getId(),
                        dirField.getText().trim(),
                        tcs);
            } catch (SQLException e) {
                warn("Save failed: " + e.getMessage());
                return null;
            }
        });

        return dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private TableView<TestCase> buildTcTable(Stage owner) {
        TableView<TestCase> t = new TableView<>(tcData);
        t.setEditable(true);
        t.setPrefHeight(150);
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("No test cases yet."));

        TableColumn<TestCase, String> desc = new TableColumn<>("Description");
        desc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        desc.setCellFactory(TextFieldTableCell.forTableColumn());
        desc.setOnEditCommit(e -> e.getRowValue().setDescription(e.getNewValue()));

        TableColumn<TestCase, String> args = new TableColumn<>("Arguments");
        args.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArguments()));
        args.setCellFactory(TextFieldTableCell.forTableColumn());
        args.setOnEditCommit(e -> e.getRowValue().setArguments(e.getNewValue()));

        TableColumn<TestCase, String> out = new TableColumn<>("Expected Output File");
        out.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getExpectedOutputPath()));
        out.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Browse…");
            {
                btn.setStyle("-fx-font-size:11px;-fx-padding:3 8 3 8;-fx-cursor:hand;");
                btn.setOnAction(e -> {
                    TestCase tc = getTableView().getItems().get(getIndex());
                    javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
                    File f = fc.showOpenDialog(owner);
                    if (f != null) { tc.setExpectedOutputPath(f.getAbsolutePath()); getTableView().refresh(); }
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                String name = (item != null && !item.isBlank()) ? new File(item).getName() : "—";
                setGraphic(new HBox(6, new Label(name), btn) {{ setAlignment(Pos.CENTER_LEFT); }});
            }
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

    private void warn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}
