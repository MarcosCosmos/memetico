package geometry;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;


import static javafx.application.Application.setUserAgentStylesheet;

public class ScalingOffsetTest extends Application {
    public void start(Stage stage) throws Exception {
        setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        BorderPane root = new BorderPane();
        Pane canvasWrapper = new Pane();
        Canvas canvas = new Canvas();
        canvasWrapper.getChildren().add(canvas);
        root.setCenter(canvasWrapper);
        TextFlow top = new TextFlow(new Text("Hi")), bottom = new TextFlow(new Text("Bye"));
        root.setTop(top);
        root.setBottom(bottom);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty().subtract(top.heightProperty()).subtract(bottom.heightProperty()));
        canvas.toFront();



        Scene scene = new Scene(root, 1280, 720);

        GraphicsContext gc = canvas.getGraphicsContext2D();      stage.setTitle("INSERT TITLE HERE");
        stage.setScene(scene);
        stage.show();
        Point2D originalPosition = new Point2D(300, 300);
        Point2D originalSize = new Point2D(300, 300);

        Point2D originalCenter = originalPosition.add(originalSize.multiply(1/2.0));

        Point2D newSize = originalSize.multiply(2);
        Point2D sizeGap = newSize.subtract(originalSize);
        Point2D offset = sizeGap.multiply(1/2.0);

//        Point2D newPosition = originalPosition.subtract(offset);
        Point2D newPosition = originalPosition.subtract(originalCenter).multiply(2).add(originalCenter);
        root.setBackground(new Background(new BackgroundFill(Color.RED,null,null)));

        gc.setLineWidth(3);
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, 1280, 720);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(originalPosition.getX(), originalPosition.getY(), originalSize.getX(), originalSize.getY());
        gc.setStroke(Color.BLUE);
        gc.strokeRect(newPosition.getX(), newPosition.getY(), newSize.getX(), newSize.getY());


    }
    public static void main(String[] args) {
        launch(args);
    }
}
