package org.marcos.uon.tspaidemo.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TestGraphicalLoggingApp extends Application {
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main/visualisation.fxml"));

        Scene scene = new Scene(root, 1280, 720);

        stage.setTitle("INSERT TITLE HERE");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
