package algos._memetico;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TestGraphicalLoggingApp extends Application {
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("test_graphical_logging_app.fxml"));

        Scene scene = new Scene(root, 300, 275);

        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
