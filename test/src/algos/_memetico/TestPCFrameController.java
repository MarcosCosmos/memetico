package algos._memetico;

import com.fxgraph.graph.PannableCanvas;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import memetico.logging.PCAlgorithmState;
import org.jorlib.io.tspLibReader.TSPInstance;
import org.marcos.uon.tspaidemo.fxgraph.Euc2DTSPFXGraph;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TestPCFrameController {
    @FXML
    private VBox contentRoot;
    @FXML
    private Text generationText;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private SplitPane splitPane;

    private IntegerProperty generationValue = new SimpleIntegerProperty();
    private ObjectProperty<PCAlgorithmState> state;
    private Map<String, TSPInstance> baseInstances;

    private List<BooleanProperty[]> tourDisplayToggles = new ArrayList<>();

    private Euc2DTSPFXGraph fxGraph;

    private void updateTours() {
        if(!fxGraph.isEmpty()) {
            //clear and re-draw tours
            fxGraph.clearTours();

            for (int i = 0; i < tourDisplayToggles.size(); ++i) {
                BooleanProperty[] eachToggles = tourDisplayToggles.get(i);
                for (int k = 0; k < eachToggles.length; ++k) {
                    if (eachToggles[k].get()) {
                        fxGraph.addTour(
                                Arrays.stream(
                                        (
                                                (k == 0 ? state.get().agents[i].pocket : state.get().agents[i].current)
                                        ).arcArray
                                )
                                        .map(
                                                each -> new int[]{each.from, each.tip}
                                        )
                                        .collect(Collectors.toList())
                        );
                    }
                }
            }
        }
    }

    public void setup(ObjectProperty<PCAlgorithmState> state) {
        splitPane.prefHeightProperty().bind(contentRoot.heightProperty());

        baseInstances = new HashMap<>();
        this.state = state;
        generationValue.bind(
                Bindings.createIntegerBinding(
                    () -> {
                        PCAlgorithmState curState = state.get();
                        return curState == null ? 0 : curState.generation;
                    } ,
                    state
                )
        );
        generationText.textProperty()
                .bind(generationValue.asString());

        //use a listener to dynamically create/destroy agent displays
        state.addListener(((observable, oldValue, newValue) -> {
            TSPInstance baseInstance = baseInstances.computeIfAbsent(newValue.instanceName, (file) -> {
                TSPInstance result = new TSPInstance();
                try {
                    result.load(new File(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            });
            ObservableList<Node> displayNodes = agentsGrid.getChildren();

            try {
                //for all the graphs we are going to keep, if the instance changed, switch to the new one.
                if (oldValue == null || !newValue.instanceName.equals(oldValue.instanceName)) {
                    fxGraph = new Euc2DTSPFXGraph(baseInstance);
                    if(!fxGraph.isEmpty()) {
                        PannableCanvas graphCanvas = fxGraph.getCanvas();

                        splitPane.getItems().set(1, graphCanvas);

                        BoundingBox canvasBounds = fxGraph.getLogicalBounds();

                        double availableHeight = splitPane.getHeight();
                        double scaleHeight = availableHeight / (canvasBounds.getHeight() + canvasBounds.getMinY());
                        //technically this is effected by divider style, not sure how to compute that yet
                        double availableWidth = (splitPane.getWidth()) * (1.0 - splitPane.getDividerPositions()[0]);
                        double scaleWidth = availableWidth / (canvasBounds.getWidth() + canvasBounds.getMinX()*3);
                        double chosenScale = Math.min(scaleWidth, scaleHeight);
//                    SplitPane.setResizableWithParent(graphCanvas, false);

                        graphCanvas.setPivot((availableWidth)*(chosenScale) + canvasBounds.getMinX()*(1/chosenScale)/2, 0);
                        graphCanvas.setScale(chosenScale);
                    } else {
                        splitPane.getItems().set(1, new Pane());
                    }


                }

                if (newValue.agents.length < displayNodes.size()) {
                    //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                    displayNodes.subList(newValue.agents.length, displayNodes.size()).clear();
                } else if (newValue.agents.length > displayNodes.size()) {
                    //add needed agent displays
                    for (int i = displayNodes.size(); i < newValue.agents.length; ++i) {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource(
                                        "test_agent_display.fxml"
                                )
                        );
                        //load the node
                        Pane newNode = loader.load();
                        //retrieve the controller
                        AgentDisplayController newController = loader.<AgentDisplayController>getController();
                        //create an observable reference to an agent state
                        ObjectProperty<PCAlgorithmState.AgentState> newState = new SimpleObjectProperty<>();
                        //create an observable property for the id
                        IntegerProperty newId = new SimpleIntegerProperty(i); //just use it's index in the lists as the id for now

                        //bind the agent state to the algorithm state+id since it doubles as the index
                        newState.bind(
                                Bindings.createObjectBinding(
                                        () -> state.get().agents[newId.get()],
                                        state, newId
                                )
                        );
//
//                        if(!fxGraph.isEmpty()) {
//                        } else {
//                            newController.setup(newId, newState);
//                        }


                        BooleanProperty[] newToggles = {new SimpleBooleanProperty(false), new SimpleBooleanProperty(false)};
                        for (BooleanProperty eachProp : newToggles) {
                            eachProp.addListener((observable1, oldValue1, newValue1) -> updateTours());
                        }
                        //give the state to the controller
                        newController.setup(newId, newState, newToggles[0], newToggles[1]);

                        //position the node cheaply for now
                        GridPane.setRowIndex(newNode, i);

                        //add the data to the lists
                        displayNodes.add(newNode);
                        tourDisplayToggles.add(newToggles);
                    }

//                    updateTours();

                    System.gc();
                }
            } catch (IOException | InvalidArgumentException e) {
                e.printStackTrace();
            }
//0
//        1
//                4
//                5
//                6
//        2
//                7
//                8
//                9
//        3
//                10
//                11
//                12
        }));

    }
}
