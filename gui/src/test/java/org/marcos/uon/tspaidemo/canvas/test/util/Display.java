package org.marcos.uon.tspaidemo.canvas.test.util;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marcos.uon.tspaidemo.canvas.CanvasTSPGraph;
import org.marcos.uon.tspaidemo.canvas.LayeredCanvas;
import org.marcos.uon.tspaidemo.canvas.TransformationContext;
import org.marcos.uon.tspaidemo.canvas.layer.*;

import java.util.List;

public class Display {
    /**
     * Shows the context (may be either an initial or a result) on top of some other, perhaps more meaningful layers
     */
    public static void displayContext(Stage stageToUse, TransformationContext context, List<Layer> additionalLayers, double sceneWidth, double sceneHeight) {
        int currentTopLayer = additionalLayers.stream().map(Layer::getPriority).max(Integer::compareTo).orElse(0);
        LayeredCanvas theCanvas = new LayeredCanvas();
        theCanvas.getLayers().addAll(additionalLayers);
        theCanvas.getLayers().addAll(new LogicalBounds(currentTopLayer+100), new BoundsInLocal(currentTopLayer+200), new BoundsInCanvas(currentTopLayer+300), new CanvasBounds(currentTopLayer+400));

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(25));
        root.setCenter(theCanvas);
        theCanvas.prefWidthProperty().bind(root.widthProperty());
        theCanvas.prefHeightProperty().bind(root.heightProperty());
        theCanvas.getTransformationContext().setState(context);
        theCanvas.setBackgroundColor(CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR);
        root.setBackground(new Background(new BackgroundFill(CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR, null, null)));

        //print out the full state.
        System.out.printf("MinScale:                  %s%n", context.getMinScale());
        System.out.printf("MaxScale:                  %s%n", context.getMaxScale());
        System.out.printf("MinDecorationScale:        %s%n", context.getMinDecorationScale());
        System.out.printf("MaxDecorationScale:        %s%n", context.getMaxDecorationScale());
        System.out.printf("MouseAnchorX:              %s%n", context.getMouseAnchorX());
        System.out.printf("MouseAnchorY:              %s%n", context.getMouseAnchorY());
        System.out.printf("TranslationX:              %s%n", context.getTranslationX());
        System.out.printf("TranslationY:              %s%n", context.getTranslationY());
        System.out.printf("Scale:                     %s%n", context.getScale());
        System.out.printf("decorationScale:           %s%n", context.getScale());
        System.out.printf("AutoTranslationX:          %s%n", context.getAutoTranslationX());
        System.out.printf("AutoTranslationY:          %s%n", context.getAutoTranslationY());
        System.out.printf("AutoScale:                 %s%n", context.getAutoScale());
        System.out.printf("DecorationPaddingInLocalTop: %s%n", context.getDecorationPaddingInLocal().getTop());
        System.out.printf("DecorationPaddingInLocalBottom: %s%n", context.getDecorationPaddingInLocal().getBottom());
        System.out.printf("DecorationPaddingInLocalLeft: %s%n", context.getDecorationPaddingInLocal().getLeft());
        System.out.printf("DecorationPaddingInLocalRight: %s%n", context.getDecorationPaddingInLocal().getRight());
        System.out.printf("DecorationPaddingInCanvasTop: %s%n", context.getDecorationPaddingInCanvas().getTop());
        System.out.printf("DecorationPaddingInCanvasBottom: %s%n", context.getDecorationPaddingInCanvas().getBottom());
        System.out.printf("DecorationPaddingInCanvasLeft: %s%n", context.getDecorationPaddingInCanvas().getLeft());
        System.out.printf("DecorationPaddingInCanvasRight: %s%n", context.getDecorationPaddingInCanvas().getRight());
        System.out.printf("LogicalBoundsX:            %s%n", context.getLogicalBounds().getMinX());
        System.out.printf("LogicalBoundsY:            %s%n", context.getLogicalBounds().getMinY());
        System.out.printf("LogicalBoundsWidth:        %s%n", context.getLogicalBounds().getWidth());
        System.out.printf("LogicalBoundsHeight:       %s%n", context.getLogicalBounds().getHeight());
        System.out.printf("BoundsInLocalX:            %s%n", context.getBoundsInLocal().getMinX());
        System.out.printf("BoundsInLocalY:            %s%n", context.getBoundsInLocal().getMinY());
        System.out.printf("BoundsInLocalWidth:        %s%n", context.getBoundsInLocal().getWidth());
        System.out.printf("BoundsInLocalHeight:       %s%n", context.getBoundsInLocal().getHeight());
        System.out.printf("BoundsInCanvasX:           %s%n", context.getBoundsInCanvas().getMinX());
        System.out.printf("BoundsInCanvasY:           %s%n", context.getBoundsInCanvas().getMinY());
        System.out.printf("BoundsInCanvasWidth:       %s%n", context.getBoundsInCanvas().getWidth());
        System.out.printf("BoundsInCanvasHeight:      %s%n", context.getBoundsInCanvas().getHeight());
        System.out.printf("CanvasBoundsX:             %s%n", context.getCanvasBounds().getMinX());
        System.out.printf("CanvasBoundsY:             %s%n", context.getCanvasBounds().getMinY());
        System.out.printf("CanvasBoundsWidth:         %s%n", context.getCanvasBounds().getWidth());
        System.out.printf("CanvasBoundsHeight:        %s%n", context.getCanvasBounds().getHeight());

        stageToUse.setTitle("Debug display");
        stageToUse.setScene(new Scene(root, sceneWidth, sceneHeight));
        stageToUse.show();
        theCanvas.draw(); //draw must come after showing the stage
    }

    public static void displayContext(Stage stageToUse, TransformationContext context, List<Layer> additionalLayers) {
        Bounds canvasBounds = context.getCanvasBounds();
        displayContext(stageToUse, context, additionalLayers, canvasBounds.getWidth() + 50, canvasBounds.getHeight() + 50);
    }
}
