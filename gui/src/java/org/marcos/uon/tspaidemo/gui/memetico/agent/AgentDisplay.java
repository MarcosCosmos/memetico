package org.marcos.uon.tspaidemo.gui.memetico.agent;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import memetico.logging.MemeticoSnapshot;

import java.io.IOException;


public class AgentDisplay extends Pane {
    private final transient Text txtAgentId;
    private final transient Text txtPocketCost;
    private final transient Text txtCurrentCost;
    private final transient Label pocketColorSample, currentColorSample;

    private final ObjectProperty<MemeticoSnapshot.AgentSnapshot> snapShot = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> pocketColor = new SimpleObjectProperty<>(), currentColor = new SimpleObjectProperty<>();


    public AgentDisplay() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "/fxml/org/marcos/uon/tspaidemo/gui/memetico/agent/agent_display.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        getStylesheets().addAll(
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/memetico/content.css").toExternalForm(),
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/common.css").toExternalForm()
        );
        getStyleClass().add("root");
        txtAgentId = (Text) lookup(".txtAgentId");
        txtPocketCost = (Text) lookup(".txtPocketCost");
        txtCurrentCost = (Text) lookup(".txtCurrentCost");
        pocketColorSample = (Label) lookup(".pocketColorSample");
        currentColorSample = (Label) lookup(".currentColorSample");

        txtAgentId.textProperty().bind(Bindings.createStringBinding(() -> snapShot.get() == null ? "?" : String.valueOf(snapShot.get().id), snapShot));
        txtPocketCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(snapShot.get() == null ? "Unknown" : snapShot.get().pocket.cost),
                snapShot
        ));
        txtCurrentCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(snapShot.get() == null ? "Unknown" : snapShot.get().current.cost),
                snapShot
        ));

        pocketColorSample.backgroundProperty().bind(
                Bindings.createObjectBinding(
                    () -> new Background(new BackgroundFill(pocketColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)),
                    pocketColor
                )
        );

        currentColorSample.backgroundProperty().bind(
                Bindings.createObjectBinding(
                        () -> new Background(new BackgroundFill(currentColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY)),
                        currentColor
                )
        );
    }

    public MemeticoSnapshot.AgentSnapshot getSnapShot() {
        return snapShot.get();
    }

    public ObjectProperty<MemeticoSnapshot.AgentSnapshot> snapShotProperty() {
        return snapShot;
    }

    public void setSnapShot(MemeticoSnapshot.AgentSnapshot snapShot) {
        this.snapShot.set(snapShot);
    }

    public Color getPocketColor() {
        return pocketColor.get();
    }

    public ObjectProperty<Color> pocketColorProperty() {
        return pocketColor;
    }

    public void setPocketColor(Color pocketColor) {
        this.pocketColor.set(pocketColor);
    }

    public Color getCurrentColor() {
        return currentColor.get();
    }

    public ObjectProperty<Color> currentColorProperty() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor.set(currentColor);
    }
}
