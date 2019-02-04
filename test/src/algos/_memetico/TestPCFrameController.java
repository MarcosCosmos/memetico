package algos._memetico;

import com.fxgraph.graph.PannableCanvas;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import memetico.logging.PCAlgorithmState;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.marcos.uon.tspaidemo.fxgraph.Euc2DTSPFXGraph;

import java.util.*;
import java.util.stream.Collectors;

public class TestPCFrameController {
    @FXML
    private HBox contentRoot;
    @FXML
    private Text generationText;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private BorderPane graphWrapper;

    private IntegerProperty generationValue = new SimpleIntegerProperty();
    private ObjectProperty<PCAlgorithmState> state;
    private Map<String, TSPLibInstance> baseInstances;

    private List<BooleanProperty[]> tourDisplayToggles = new ArrayList<>();

    private Euc2DTSPFXGraph fxGraph;

    private double lastScale = 0;
    private final ChangeListener<Number> autoSizeListener = (observable12, oldValue12, newValue12) -> {
        //reset to the old position
        PannableCanvas graphCanvas = fxGraph.getCanvas();
        BoundingBox canvasBounds = fxGraph.getLogicalBounds();

        double availableHeight = graphWrapper.getHeight();
        double padding = Math.max(canvasBounds.getMinX()*2, canvasBounds.getMinY()*2);
        double scaleHeight = availableHeight / (canvasBounds.getHeight() + padding);
        //technically this is effected by divider style, not sure how to compute that yet
        double availableWidth = newValue12.doubleValue();
        double scaleWidth = availableWidth / (canvasBounds.getWidth() + padding);
        double chosenScale = Math.min(scaleWidth, scaleHeight);
//        double newTranslate = ((availableWidth)*(chosenScale)/2 + canvasBounds.getMinX()*2);

        Scale scale = new Scale();
        scale.setPivotX(0);
        scale.setPivotY(0);
        scale.setX(chosenScale/lastScale);
        scale.setY(chosenScale/lastScale);
        graphCanvas.getTransforms().add(scale);
        lastScale = chosenScale;
    };

    private void updateTours() {
        //disable auto scaling
        if(!fxGraph.isEmpty()) {
            //clear and re-draw tours
            fxGraph.clearPredictions();

            for (int i = 0; i < tourDisplayToggles.size(); ++i) {
                BooleanProperty[] eachToggles = tourDisplayToggles.get(i);
                for (int k = 0; k < eachToggles.length; ++k) {
                    if (eachToggles[k].get()) {
                        fxGraph.addPredictionEdges(
                                Arrays.stream(
                                        (
                                                k == 0 ?
                                                    state.get()
                                                            .agents[i]
                                                            .pocket
                                                :
                                                    state.get()
                                                            .agents[i]
                                                            .current
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
            fxGraph.endUpdate();
        }
    }

    public void setup(Map<String, TSPLibInstance> baseInstances, ObjectProperty<PCAlgorithmState> state) {
        this.baseInstances = baseInstances;
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
            TSPLibInstance baseInstance = baseInstances.get(newValue.instanceName);
            ObservableList<Node> displayNodes = agentsGrid.getChildren();

            try {
                //for all the graphs we are going to keep, if the instance changed, switch to the new one.
                if (oldValue == null || !newValue.instanceName.equals(oldValue.instanceName)) {
                    fxGraph = new Euc2DTSPFXGraph(baseInstance);
                    graphWrapper.getChildren().clear();
                    if(!fxGraph.isEmpty()) {
                        graphWrapper.setCenter(fxGraph.getGraphic());
                        lastScale=1;
                        //enable auto sizing
                        graphWrapper.widthProperty().addListener(autoSizeListener);
                        autoSizeListener.changed(null, graphWrapper.getWidth(), graphWrapper.getWidth());
                    }


                }

                if (newValue.agents.length < displayNodes.size()) {
                    //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                    displayNodes.subList(newValue.agents.length, displayNodes.size()).clear();
                } else if (newValue.agents.length > displayNodes.size()) {
                    //add needed agent displays
                    for (int i = displayNodes.size(); i < newValue.agents.length; ++i) {
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
                        AgentDisplay newNode = new AgentDisplay(newId, newState, newToggles[0], newToggles[1]);

                        //position the node cheaply for now
                        GridPane.setRowIndex(newNode, i);

                        //add the data to the lists
                        displayNodes.add(newNode);
                        tourDisplayToggles.add(newToggles);
                    }

                }
            } catch ( InvalidArgumentException e) {
                e.printStackTrace();
            }
            updateTours();
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
