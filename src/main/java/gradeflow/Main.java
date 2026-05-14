package gradeflow;

import gradeflow.db.DatabaseManager;
import gradeflow.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.getInstance(); // initialise DB and create tables on first run
        new MainWindow().show(primaryStage);
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
