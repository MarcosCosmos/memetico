package org.marcos.uon.tspaidemo.gui.memetico.options;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.marcos.uon.tspaidemo.gui.memetico.MemeticoConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class OptionsBoxController implements Initializable {

    @FXML
    private ScrollPane memeticoOptionsBoxRoot;
    @FXML
    private Text txtMemeticoSelectedProblem, txtMemeticoSelectedTourSolution;
    @FXML
    private Button btnMemeticoSelectProblem;
    @FXML
    private CheckBox cbMemeticoToggleTarget;
    @FXML
    private VBox memeticoAgentOptionsWrapper;
    @FXML
    private TextField fldMemeticoSolutionCost;
    @FXML
    private ChoiceBox<String> cbMemeticoSolutionType;
    @FXML
    private GridPane gpMemeticoTourSolutionSelection;
    @FXML
    private GridPane gpMemeticoCostSolutionSelection;

    private IntegerProperty agentCount = new SimpleIntegerProperty(0);
    private ObservableList<BooleanProperty[]> solutionDisplayToggles = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<MemeticoConfiguration> memeticoConfiguration = new SimpleObjectProperty<>();

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
            //reset the solution information
            txtMemeticoSelectedTourSolution.setText("");
            fldMemeticoSolutionCost.setText("");

        }
    }

    public void openTourSelectionDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Tour File");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            txtMemeticoSelectedTourSolution.setText(selection.getPath());
        }
    }

    private void populateAgentOptions(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        List<Node> children = memeticoAgentOptionsWrapper.getChildren();
        try {
            if (newValue.intValue() < oldValue.intValue()) {
                //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                children.subList(newValue.intValue(), oldValue.intValue()).clear();
                solutionDisplayToggles.subList(newValue.intValue(), oldValue.intValue()).clear();
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
//                    if (oldValue.intValue() == 0 && newValue.intValue() != 0 && i == 0) {
//                        toggles[0].setValue(true);
//                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Parent getRoot() {
        return memeticoOptionsBoxRoot;
    }

    public BooleanProperty getTargetDisplayToggle() {
        return cbMemeticoToggleTarget.selectedProperty();
    }

    public ObservableList<BooleanProperty[]> getSolutionDisplayToggles() {
        return solutionDisplayToggles;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbMemeticoSolutionType.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    switch (newValue){
                        case "Tour":
                            gpMemeticoTourSolutionSelection.setVisible(true);
                            gpMemeticoCostSolutionSelection.setVisible(false);
                            break;
                        case "Cost":
                            gpMemeticoTourSolutionSelection.setVisible(false);
                            gpMemeticoCostSolutionSelection.setVisible(true);
                            break;
                    }
                }
        );
        agentCount.addListener(this::populateAgentOptions);
        fldMemeticoSolutionCost.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                fldMemeticoSolutionCost.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        memeticoConfiguration.addListener(
                (observable, oldValue, newValue) -> {
                    txtMemeticoSelectedProblem.setText(newValue.problemFile.getPath());
                    cbMemeticoSolutionType.setValue(newValue.solutionType.toString());
                    switch (newValue.solutionType) {
                        case TOUR:
                            txtMemeticoSelectedTourSolution.setText(newValue.tourFile.getPath());
                        case COST:
                            fldMemeticoSolutionCost.setText(String.valueOf(newValue.targetCost));
                            break;
                    }
                }
        );
//        cbMemeticoToggleTarget.selectedProperty().set(true);
    }

    public MemeticoConfiguration getMemeticoConfiguration() {
        return memeticoConfiguration.get();
    }

    public ObjectProperty<MemeticoConfiguration> memeticoConfigurationProperty() {
        return memeticoConfiguration;
    }

    public void setMemeticoConfiguration(MemeticoConfiguration config) {
        this.memeticoConfiguration.set(config);
    }

    public void applyConfiguration() {
        File problemFile = new File(txtMemeticoSelectedProblem.getText());
        switch(cbMemeticoSolutionType.getValue()) {
            case "Tour":
                File tourFile = new File(txtMemeticoSelectedTourSolution.getText());
                memeticoConfiguration.set(new MemeticoConfiguration(problemFile, tourFile));
                break;
            case "Cost":
                int targetCost = Integer.parseInt(fldMemeticoSolutionCost.getText());
                memeticoConfiguration.set(new MemeticoConfiguration(problemFile, targetCost));
        }

    }
}
