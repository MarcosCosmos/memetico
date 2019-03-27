package org.marcos.uon.tspaidemo.gui.memetico;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.marcos.uon.tspaidemo.gui.main.VisualisationController;

public class MemeticoVisualisationApp extends Application {
    private MemeticoContentController contentController;
    private VisualisationController visualisationController;
    @Override
    public void start(Stage primaryStage) throws Exception {
        //load the content
        FXMLLoader loader;
        loader = new FXMLLoader(
                getClass().getResource(
                        "/fxml/org/marcos/uon/tspaidemo/gui/memetico/content.fxml"
                )
        );
        loader.load();
        contentController = loader.getController();

        //load the visualisation container
        loader = new FXMLLoader(
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/visualisation.fxml")
        );

        setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        Parent root = loader.load();
        visualisationController = loader.getController();
        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setTitle("AI Process Visualisation");
        primaryStage.setScene(scene);
        primaryStage.show();
        visualisationController.setup(contentController);



    }

    public static void main(String[] args) {
        launch(args);
    }
}
