package gradeflow.ui;

import gradeflow.manager.ConfigurationManager;
import gradeflow.model.ComparisonMethod;
import gradeflow.model.Configuration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.util.Optional;

public class ConfigurationDialog {

    public Optional<Configuration> show(Stage owner, Configuration existing) {
        Dialog<Configuration> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New Configuration" : "Edit Configuration");
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);

        TextField nameField     = field("e.g. C – GCC");
        TextField langField     = field("e.g. C, Java, Python");
        TextField compPathField = field("e.g. gcc  or  C:/MinGW/bin/gcc.exe");
        TextField compArgsField = field("e.g. -o main");
        TextField srcFileField  = field("e.g. main.c");
        TextField runCmdField   = field("e.g. ./main  or  python3 main.py");

        RadioButton compiledRb    = new RadioButton("Compiled");
        RadioButton interpretedRb = new RadioButton("Interpreted");
        ToggleGroup tg            = new ToggleGroup();
        compiledRb.setToggleGroup(tg);
        interpretedRb.setToggleGroup(tg);
        compiledRb.setSelected(true);

        ComboBox<ComparisonMethod> cmpBox = new ComboBox<>();
        cmpBox.getItems().addAll(ComparisonMethod.values());
        cmpBox.setValue(ComparisonMethod.TRIMMED);
        cmpBox.setMaxWidth(Double.MAX_VALUE);

        Button browseBtn = new Button("Browse…");
        browseBtn.setStyle(Styles.BTN_SECONDARY);
        browseBtn.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Select Compiler Executable");
            java.io.File f = fc.showOpenDialog(owner);
            if (f != null) compPathField.setText(f.getAbsolutePath());
        });

       
        interpretedRb.selectedProperty().addListener((obs, old, isInterp) -> {
            compPathField.setDisable(isInterp);
            compArgsField.setDisable(isInterp);
            browseBtn.setDisable(isInterp);
            if (isInterp) { compPathField.clear(); compArgsField.clear(); }
        });

        
        if (existing != null) {
            nameField.setText(existing.getName());
            langField.setText(existing.getLanguage());
            compPathField.setText(existing.getCompilerPath());
            compArgsField.setText(existing.getCompilerArgs());
            srcFileField.setText(existing.getSourceFileName());
            runCmdField.setText(existing.getRunCommand());
            cmpBox.setValue(existing.getComparisonMethod());
            if (existing.isCompiled()) compiledRb.setSelected(true);
            else                       interpretedRb.setSelected(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color:" + Styles.BG + ";");

        int r = 0;
        addRow(grid, r++, "Configuration Name *", nameField);
        addRow(grid, r++, "Language *",            langField);
        addRow(grid, r++, "Compiler Path",
               new HBox(8, compPathField, browseBtn) {{ setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(compPathField, Priority.ALWAYS); }});
        addRow(grid, r++, "Compiler Arguments",    compArgsField);
        addRow(grid, r++, "Source File Name *",    srcFileField);
        addRow(grid, r++, "Run Command *",         runCmdField);
        addRow(grid, r++, "Comparison Method",     cmpBox);
        addRow(grid, r++, "Language Type",
               new HBox(16, compiledRb, interpretedRb) {{ setAlignment(Pos.CENTER_LEFT); }});

        Label hint = new Label("* Required. Leave Compiler Path empty for interpreted languages.");
        hint.setStyle(Styles.LABEL_MUTED + "-fx-font-size:11px;");
        hint.setWrapText(true);
        grid.add(hint, 0, r, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");

        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().setStyle("-fx-background-color:" + Styles.BG + ";");

        ButtonType saveBtn = new ButtonType("Save Configuration", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt != saveBtn) return null;
            if (nameField.getText().isBlank() || srcFileField.getText().isBlank()
                    || runCmdField.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING,
                        "Please fill all required fields (*).", ButtonType.OK).showAndWait();
                return null;
            }
            Configuration c = (existing != null) ? existing : new Configuration();
            c.setName(nameField.getText().trim());
            c.setLanguage(langField.getText().trim());
            c.setCompilerPath(compPathField.getText().trim());
            c.setCompilerArgs(compArgsField.getText().trim());
            c.setSourceFileName(srcFileField.getText().trim());
            c.setRunCommand(runCmdField.getText().trim());
            c.setCompiled(compiledRb.isSelected());
            c.setComparisonMethod(cmpBox.getValue());

            try {
                ConfigurationManager mgr = new ConfigurationManager();
                if (existing == null) mgr.create(c);
                else                  mgr.update(c);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Save failed: " + e.getMessage(),
                        ButtonType.OK).showAndWait();
                return null;
            }
            return c;
        });

        return dialog.showAndWait();
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
        l.setMinWidth(170);
        GridPane.setHgrow(control, Priority.ALWAYS);
        grid.add(l, 0, row);
        grid.add(control, 1, row);
    }
}
