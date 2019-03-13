package org.marcos.uon.tspaidemo.gui.memetico.options;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DisplayOptionsController implements Initializable {
    @FXML
    private ScrollPane memeticoDisplayOptionsRoot;

    @FXML
    private VBox memeticoAgentOptionsWrapper;

    @FXML
    private Label lblMemeticoToggleTarget;

    @FXML
    private CheckBox cbMemeticoToggleTarget, cbMemeticoToggleBest;

    private transient final ObservableList<BooleanProperty[]> solutionDisplayToggles = new SimpleListProperty<>(FXCollections.observableArrayList());
    private transient final BooleanProperty isTargetAvailable = new SimpleBooleanProperty(false);

    private Stage theStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memeticoDisplayOptionsRoot.getStyleClass().addAll(
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/memetico/options/options_box.css").toExternalForm(),
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/common.css").toExternalForm()
        );
        isTargetAvailable.addListener(
                (observable, oldValue, newValue) -> {
                    lblMemeticoToggleTarget.setVisible(newValue);
                    cbMemeticoToggleTarget.setVisible(newValue);
                }
        );
    }

    public void bindIsTargetAvailable(ObservableValue<Boolean> isTargetAvailable) {
        this.isTargetAvailable.bind(isTargetAvailable);
    }

    public void unbindTargetIsAvailable() {
        isTargetAvailable.unbind();
    }

    public void adjustAgentOptionsDisplay(int oldCount, int newCount) {
        List<Node> children = memeticoAgentOptionsWrapper.getChildren();
        try {
            if (newCount < oldCount) {
                //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                children.subList(newCount, oldCount).clear();
                solutionDisplayToggles.subList(newCount, oldCount).clear();
            } else if (newCount > oldCount) {
                for (int i = oldCount; i < newCount; ++i) {
                    Node eachSubBox;

                    eachSubBox = FXMLLoader.load(getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/memetico/options/agent_solution_toggles.fxml"));

                    ((Text)eachSubBox.lookup(".txtAgentId")).setText(String.valueOf(i));
                    BooleanProperty[] toggles = new BooleanProperty[]{
                            ((CheckBox)eachSubBox.lookup(".cbTogglePocket")).selectedProperty(),
                            ((CheckBox)eachSubBox.lookup(".cbToggleCurrent")).selectedProperty()
                    };
                    children.add(eachSubBox);
                    solutionDisplayToggles.add(toggles);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        theStage = new Stage();
        Scene newScane = new Scene(memeticoDisplayOptionsRoot, 300, 200);
        theStage.setScene(newScane);
    }

    public BooleanProperty getTargetDisplayToggle() {
        return cbMemeticoToggleTarget.selectedProperty();
    }

    public BooleanProperty getBestDisplayToggle() {
        return cbMemeticoToggleBest.selectedProperty();
    }
    public ObservableList<BooleanProperty[]> getSolutionDisplayToggles() {
        return solutionDisplayToggles;
    }

    public void showAllPredictions() {
        for (BooleanProperty[] eachToggles : solutionDisplayToggles) {
            for (BooleanProperty eachToggle : eachToggles) {
                eachToggle.set(true);
            }
        }
    }

    public void hideAllPredictions() {
        for (BooleanProperty[] eachToggles : solutionDisplayToggles) {
            for (BooleanProperty eachToggle : eachToggles) {
                eachToggle.set(false);
            }
        }
    }

    public void showAllPockets() {
        for (BooleanProperty[] eachToggles : solutionDisplayToggles) {
            eachToggles[0].set(true);
        }
    }

    public void hideAllPockets() {
        for (BooleanProperty[] eachToggles : solutionDisplayToggles) {
            eachToggles[0].set(false);
        }
    }

    public void showAllCurrents() {
        for (BooleanProperty[] eachToggles : solutionDisplayToggles) {
            eachToggles[1].set(true);
        }
    }

    public void hideAllCurrents() {
        for (BooleanProperty[] eachToggles : solutionDisplayToggles) {
            eachToggles[1].set(false);
        }
    }

    public void open() {
        theStage.show();
    }

    public void close() {
        theStage.close();
    }

}
