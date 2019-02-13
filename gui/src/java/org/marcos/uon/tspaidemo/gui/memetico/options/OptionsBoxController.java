package org.marcos.uon.tspaidemo.gui.memetico.options;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import memetico.Population;
import memetico.util.CrossoverOpName;
import memetico.util.LocalSearchOpName;
import memetico.util.RestartOpName;
import org.marcos.uon.tspaidemo.gui.memetico.MemeticoConfiguration;
import org.marcos.uon.tspaidemo.gui.memetico.ProblemConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OptionsBoxController implements Initializable {
    private Stage theStage;

    @FXML
    private ScrollPane memeticoOptionsBoxRoot;
    @FXML
    private Button btnMemeticoSelectProblem, btnMemeticoSelectTour;
    @FXML
    private CheckBox cbMemeticoToggleTarget;
    @FXML
    private VBox memeticoAgentOptionsWrapper;
    @FXML
    private TextField fldMemeticoTourCost, fldMemeticoPopDepth, fldMemeticoMutRate,fldMemeticoMaxGen;
    @FXML
    private ChoiceBox<String> choiceMemeticoSolutionType, choiceMemeticoLocalSearch, choiceMemeticoCrossover, choiceMemeticoRestart;

    @FXML
    private Label lblMemeticoProblemFile, lblMemeticoTourFile, lblMemeticoTourFileDesc, lblMemeticoTourCost, lblMemeticoToggleTarget;

    private ObservableList<BooleanProperty[]> solutionDisplayToggles = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<ProblemConfiguration> problemConfiguration = new SimpleObjectProperty<>();

    private final ObjectProperty<MemeticoConfiguration> memeticoConfiguration = new SimpleObjectProperty<>();


    //called when the user wants to apply their selected configuration
    private Runnable applyConfigFunc = () -> {};

    public void openProblemSelectionDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Problem File");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            lblMemeticoProblemFile.setText(selection.getPath());
            //reset the solution information
            lblMemeticoTourFile.setText("");
            fldMemeticoTourCost.setText("");
        }
    }

    public void openTourSelectionDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Tour File");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            lblMemeticoTourFile.setText(selection.getPath());
        }
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
                    Node eachSubBox = null;

                    eachSubBox = FXMLLoader.load(getClass().getResource("agent_solution_toggles.fxml"));

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
    }

    public BooleanProperty getTargetDisplayToggle() {
        return cbMemeticoToggleTarget.selectedProperty();
    }

    public ObservableList<BooleanProperty[]> getSolutionDisplayToggles() {
        return solutionDisplayToggles;
    }

    public ProblemConfiguration getProblemConfiguration() {
        return problemConfiguration.get();
    }

    public ObjectProperty<ProblemConfiguration> problemConfigurationProperty() {
        return problemConfiguration;
    }

    public void setProblemConfiguration(ProblemConfiguration problemConfiguration) {
        this.problemConfiguration.set(problemConfiguration);
    }

    private static ChangeListener<String> generateIntFieldFixer(TextField target) {
        return (observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                target.setText(newValue.replaceAll("[^\\d]", ""));
            }
        };
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        memeticoOptionsBoxRoot.getStylesheets().add(getClass().getResource("options_box.css").toExternalForm());
        memeticoOptionsBoxRoot.getStylesheets().add(getClass().getResource("../../main/common.css").toExternalForm());
        choiceMemeticoSolutionType.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    switch (newValue){
                        case "Tour":
                            lblMemeticoTourFileDesc.setVisible(true);
                            lblMemeticoTourFile.setVisible(true);
                            btnMemeticoSelectTour.setVisible(true);
                            lblMemeticoTourCost.setVisible(false);
                            fldMemeticoTourCost.setVisible(false);
                            break;
                        case "Cost":
                            lblMemeticoTourFileDesc.setVisible(false);
                            lblMemeticoTourFile.setVisible(false);
                            btnMemeticoSelectTour.setVisible(false);
                            lblMemeticoTourCost.setVisible(true);
                            fldMemeticoTourCost.setVisible(true);
                            break;
                    }
                }
        );

        fldMemeticoTourCost.textProperty().addListener(generateIntFieldFixer(fldMemeticoTourCost));
        fldMemeticoPopDepth.textProperty().addListener(generateIntFieldFixer(fldMemeticoPopDepth));
        fldMemeticoMutRate.textProperty().addListener(generateIntFieldFixer(fldMemeticoMutRate));
        fldMemeticoMaxGen.textProperty().addListener(generateIntFieldFixer(fldMemeticoMaxGen));



        choiceMemeticoLocalSearch.getItems().addAll(
                Arrays.stream(LocalSearchOpName.values())
                        .map(Object::toString)
                        .collect(Collectors.toList())
        );

        choiceMemeticoCrossover.getItems().addAll(
                Arrays.stream(CrossoverOpName.values())
                        .map(Object::toString)
                        .collect(Collectors.toList())
        );

        choiceMemeticoRestart.getItems().addAll(
                Arrays.stream(RestartOpName.values())
                    .map(Object::toString)
                    .collect(Collectors.toList())
        );

        //bind the displayed fields etc to the actual problem via listeners
        problemConfiguration.addListener(
                (observable, oldValue, newValue) -> {
                    lblMemeticoProblemFile.setText(newValue.problemFile.getPath());
                    choiceMemeticoSolutionType.setValue(newValue.solutionType.toString());
                    switch (newValue.solutionType) {
                        case TOUR:
                            lblMemeticoTourFile.setText(newValue.tourFile.getPath());
                            lblMemeticoToggleTarget.setVisible(true);
                            cbMemeticoToggleTarget.setVisible(true);
                            break;
                        case COST:
                            fldMemeticoTourCost.setText(String.valueOf(newValue.targetCost));
                            lblMemeticoToggleTarget.setVisible(false);
                            cbMemeticoToggleTarget.setVisible(false);
                            break;
                    }
                }
        );
        memeticoConfiguration.addListener((observable, oldValue, newValue) -> {
            int popSize = newValue.populationSize;
            int popDepth = (int)Math.ceil(
                    (Math.log(
                        ( (Population.DEFAULT_N_ARY-1) * popSize ) + 1
                    ) / Math.log(Population.DEFAULT_N_ARY)) - 1
            );
            fldMemeticoPopDepth.setText(String.valueOf(popDepth));
            fldMemeticoMutRate.setText(String.valueOf(newValue.mutationRate));
            fldMemeticoMaxGen.setText(String.valueOf(newValue.maxGenerations));
            choiceMemeticoLocalSearch.setValue(newValue.localSearchOp);
            choiceMemeticoCrossover.setValue(newValue.crossoverOp);
            choiceMemeticoRestart.setValue(newValue.restartOp);
        });

        theStage = new Stage();
        Scene newScane = new Scene(memeticoOptionsBoxRoot, 300, 200);
        theStage.setScene(newScane);
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
        File problemFile = new File(lblMemeticoProblemFile.getText());
        switch(choiceMemeticoSolutionType.getValue()) {
            case "Tour":
                File tourFile = new File(lblMemeticoTourFile.getText());
                problemConfiguration.set(new ProblemConfiguration(problemFile, tourFile));
                break;
            case "Cost":
                int targetCost = Integer.parseInt(fldMemeticoTourCost.getText());
                problemConfiguration.set(new ProblemConfiguration(problemFile, targetCost));
        }
        int populationHeight = Integer.parseInt(fldMemeticoPopDepth.textProperty().get());
        int populationSize = (int)((Math.pow(3, populationHeight+1)-1)/2.0);
        int mutationRate = Integer.parseInt(fldMemeticoMutRate.textProperty().get());
        int maxGenerations = Integer.parseInt(fldMemeticoMaxGen.textProperty().get());
        memeticoConfiguration.set(new MemeticoConfiguration(populationSize, mutationRate, choiceMemeticoLocalSearch.getValue(), choiceMemeticoCrossover.getValue(), choiceMemeticoRestart.getValue(), maxGenerations));
        applyConfigFunc.run();
    }

    /**
     * Set an external function to be called when the user wants to apply their selected configuration
     * @param applyConfigFunc
     */
    public void setApplyConfigFunc(Runnable applyConfigFunc) {
        this.applyConfigFunc = applyConfigFunc;
    }

    public void open() {
        theStage.show();
    }

    public void close() {
        theStage.close();
    }
}
