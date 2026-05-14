package gradeflow.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class HelpWindow {

    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.setTitle("GradeFlow — User Manual");
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);

        TextField searchField = new TextField();
        searchField.setPromptText("Search in manual…");
        searchField.setStyle(Styles.TEXT_FIELD);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("🔍");
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

        Label body = new Label(
            "GradeFlow is an integrated environment for managing and grading " +
            "programming assignments.\n\n" +
            "Use the menu bar to manage configurations and projects. " +
            "Open or create a project, add test cases, then click Run to grade all submissions automatically.\n\n" +
            "Results are shown in the Results tab with colour-coded status badges. " +
            "Click Details on any row to see the full compile, run, and diff logs."
        );
        body.setStyle("-fx-font-size:13px;-fx-text-fill:" + Styles.TEXT + ";");
        body.setWrapText(true);

        Label tip = new Label("Tip: Use TRIMMED comparison mode if students might have trailing newlines in their output.");
        tip.setStyle("-fx-background-color:#EFF6FF;-fx-text-fill:#1E40AF;" +
                     "-fx-background-radius:6;-fx-padding:10 14 10 14;-fx-font-size:12px;");
        tip.setWrapText(true);

        VBox contentArea = new VBox(12, heading, new Separator(), body, tip);
        contentArea.setPadding(new Insets(24));
        contentArea.setStyle("-fx-background-color:white;");

        
        tree.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null && sel.isLeaf()) {
                heading.setText(sel.getValue());
                body.setText("Full documentation for this topic will be added in Milestone 3.");
            }
        });

        ScrollPane contentScroll = new ScrollPane(contentArea);
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background-color:white;-fx-background:white;");

        SplitPane split = new SplitPane(tree, contentScroll);
        split.setDividerPositions(0.28);

        Label statusBar = new Label("GradeFlow User Manual — Version 1.0");
        statusBar.setStyle(Styles.LABEL_MUTED + "-fx-padding:5 12 5 12;-fx-border-color:" +
                           Styles.BORDER + ";-fx-border-width:1 0 0 0;");
        statusBar.setMaxWidth(Double.MAX_VALUE);

        VBox layout = new VBox(searchBar, split, statusBar);
        VBox.setVgrow(split, Priority.ALWAYS);

        stage.setScene(new Scene(layout, 780, 550));
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }
}
