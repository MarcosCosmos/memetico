package org.marcos.uon.tspaidemo.gui.memetico.options;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OptionsBoxController implements Initializable {
    @FXML
    private ScrollPane memeticoOptionsBoxRoot;
    @FXML
    private Text txtMemeticoSelectedProblem;
    @FXML
    private Button btnMemeticoSelectProblem;
    @FXML
    private CheckBox cbMemeticoToggleTarget;
    @FXML
    private VBox memeticoAgentOptionsWrapper;
    @FXML
    private TextField memeticoSolutionCostField;

    private IntegerProperty agentCount = new SimpleIntegerProperty(0);
    private ObservableList<BooleanProperty[]> solutionDisplayToggles = new SimpleListProperty<>(FXCollections.observableArrayList());

    public OptionsBoxController() {
        agentCount.addListener(this::populateAgentOptions);
    }

    public int getAgentCount() {
        return agentCount.get();
    }

    public IntegerProperty agentCountProperty() {
        return agentCount;
    }

    public void openProblemSelectionDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Problem File");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            txtMemeticoSelectedProblem.setText(selection.getPath());
        }
    }

    public void openTourSelectionDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Tour File");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            txtMemeticoSelectedProblem.setText(selection.getPath());
        }
    }

    private void populateAgentOptions(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        List<Node> children = memeticoAgentOptionsWrapper.getChildren();
        try {
            if (newValue.intValue() < oldValue.intValue()) {
                //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                children.subList(oldValue.intValue(), newValue.intValue()).clear();
                solutionDisplayToggles.subList(oldValue.intValue(), newValue.intValue()).clear();
            } else if (newValue.intValue() > oldValue.intValue()) {
                for (int i = oldValue.intValue(); i < newValue.intValue(); ++i) {
                    Node eachSubBox = null;

                    eachSubBox = FXMLLoader.load(getClass().getResource("agent_solution_toggles.fxml"));

                    ((Text)eachSubBox.lookup(".txtAgentId")).setText(String.valueOf(i));
                    BooleanProperty[] toggles = new BooleanProperty[]{
                            ((CheckBox)eachSubBox.lookup(".cbTogglePocket")).selectedProperty(),
                            ((CheckBox)eachSubBox.lookup(".cbToggleCurrent")).selectedProperty()
                    };
                    children.add(eachSubBox);
                    solutionDisplayToggles.add(toggles);
                    if (oldValue.intValue() == 0 && newValue.intValue() != 0 && i == 0) {
                        toggles[0].setValue(true);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Parent getRoot() {
        return memeticoOptionsBoxRoot;
    }

    public ObservableList<BooleanProperty[]> getSolutionDisplayToggles() {
        return solutionDisplayToggles;
    }

    public StringProperty selectedProblemProperty() {
        return txtMemeticoSelectedProblem.textProperty();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        memeticoSolutionCostField.textProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue.matches("\\d*")) {
//                memeticoSolutionCostField.setText(newValue.replaceAll("[^\\d]", ""));
//            }
//        });
    }

    //    List<BooleanProperty[]> getAgentToggles()
}
