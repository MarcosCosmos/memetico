package org.marcos.uon.tspaidemo.gui.memetico;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import memetico.logging.PCAlgorithmState;

import java.io.IOException;


public class AgentDisplay extends Pane {
//    private final transient Label lblAgentId;
//    private final transient Label lblPocketCost;
//private final transient Label lblCurrentCost;
    private final transient Text txtAgentId;
    private final transient Text txtPocketCost;
    private final transient Text txtCurrentCost;
    private final transient Label pocketColorSample, currentColorSample;
//    private final transient CheckBox cbTogglePocket, cbToggleCurrent;

    private final ObjectProperty<PCAlgorithmState.AgentState> state = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> pocketColor = new SimpleObjectProperty<>(), currentColor = new SimpleObjectProperty<>();


    public AgentDisplay() {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "agent_display.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        txtAgentId = (Text) lookup(".txtAgentId");
        txtPocketCost = (Text) lookup(".txtPocketCost");
        txtCurrentCost = (Text) lookup(".txtCurrentCost");
        pocketColorSample = (Label) lookup(".pocketColorSample");
        currentColorSample = (Label) lookup(".currentColorSample");

        txtAgentId.textProperty().bind(Bindings.createStringBinding(() -> state.get() == null ? "?" : String.valueOf(state.get().id), state));
        txtPocketCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(state.get() == null ? "Uknown" : state.get().pocket.cost),
                state
        ));
        txtCurrentCost.textProperty().bind(Bindings.createStringBinding(
                () -> String.valueOf(state.get() == null ? "Uknown" : state.get().current.cost),
                state
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

    public PCAlgorithmState.AgentState getState() {
        return state.get();
    }

    public ObjectProperty<PCAlgorithmState.AgentState> stateProperty() {
        return state;
    }

    public void setState(PCAlgorithmState.AgentState state) {
        this.state.set(state);
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
