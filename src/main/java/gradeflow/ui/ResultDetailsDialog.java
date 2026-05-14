package gradeflow.ui;

import gradeflow.model.StudentReport;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class ResultDetailsDialog {

    public void show(Stage owner, StudentReport r) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Result Details — " + r.getStudentId());
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(true);

        // summary strip at the top
        Label badge = new Label(r.getStatus().name());
        badge.setStyle(Styles.badgeFor(r.getStatus().name()));

        String ts = r.getProcessedAt() != null ? r.getProcessedAt().toString() : "—";
        Label  tsLabel = new Label("Processed: " + ts);
        tsLabel.setStyle(Styles.LABEL_MUTED);

        HBox summary = new HBox(12,
            new Label("Status:") {{ setStyle(Styles.LABEL_MUTED); }},
            badge,
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            tsLabel
        );
        summary.setAlignment(Pos.CENTER_LEFT);
        summary.setPadding(new Insets(10, 16, 10, 16));
        summary.setStyle("-fx-background-color:" + Styles.BG + ";-fx-border-color:" +
                         Styles.BORDER + ";-fx-border-width:0 0 1 0;");

        
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
            logTab("Compile Log", r.getCompileLog()),
            logTab("Run Log",     r.getRunLog()),
            logTab("Diff Log",    r.getDiffLog())
        );

        int defaultTab = switch (r.getStatus()) {
            case COMPILE_ERROR, MISSING_FILE -> 0;
            case RUNTIME_ERROR, TIMEOUT      -> 1;
            default                          -> 2;
        };
        tabs.getSelectionModel().select(defaultTab);

        VBox content = new VBox(summary, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(640, 440);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private Tab logTab(String title, String text) {
        TextArea area = new TextArea(text != null && !text.isBlank() ? text : "(empty)");
        area.setEditable(false);
        area.setWrapText(false);
        area.setStyle(
            "-fx-font-family:'Consolas','Courier New',monospace;-fx-font-size:12px;" +
            "-fx-background-color:#0F172A;-fx-text-fill:#E2E8F0;" +
            "-fx-control-inner-background:#0F172A;");
        area.setPrefHeight(360);
        VBox.setVgrow(area, Priority.ALWAYS);
        return new Tab(title, area);
    }
}
