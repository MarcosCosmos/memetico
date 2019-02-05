package org.marcos.uon.tspaidemo.gui.memetico;

import com.fxgraph.graph.PannableCanvas;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import memetico.ATSPInstance;
import memetico.GraphInstance;
import memetico.Instance;
import memetico.Memetico;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.graph.DistanceTable;
import org.marcos.uon.tspaidemo.fxgraph.Euc2DTSPFXGraph;
import org.marcos.uon.tspaidemo.gui.main.ContentController;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;
import org.marcos.uon.tspaidemo.util.tree.TreeNode;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MemeticoContentController implements ContentController {
    //used during agent display arrangement
    private static class GridPositionData {
        public int id = -1;
        public int row = -1;
        public int column = -1;
    }

    //todo: possibly make this a parameter instead
    /**
     * Measured as checks per second
     */
    public static double LOG_POLL_RATE = 60;

    @FXML
    private HBox contentRoot;
    @FXML
    private Text generationText;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private BorderPane graphWrapper;

    private transient ObjectProperty<PCAlgorithmState> state = new SimpleObjectProperty<>();
    private transient PCLogger logger;
    private transient BasicLogger<PCAlgorithmState>.View theView;
    private final transient Timeline updateCheckingTimeline = new Timeline();
    private transient ReadOnlyIntegerWrapper numberOfFrames = new ReadOnlyIntegerWrapper(0);
    private transient IntegerProperty selectedFrameIndex = new SimpleIntegerProperty(0);

    private IntegerProperty generationValue = new SimpleIntegerProperty();
    private Map<String, TSPLibInstance> baseInstances = new HashMap<>();

    private List<BooleanProperty[]> tourDisplayToggles = new ArrayList<>();

    private Euc2DTSPFXGraph fxGraph;

    private double lastScale = 0;
    private final ChangeListener<Number> autoSizeListener = (observable12, oldValue12, newValue12) -> {
        //reset to the old position
        PannableCanvas graphCanvas = fxGraph.getCanvas();
        BoundingBox canvasBounds = fxGraph.getLogicalBounds();

        double availableHeight = graphWrapper.getHeight();
        double padding = Math.max(canvasBounds.getMinX() * 2, canvasBounds.getMinY() * 2);
        double scaleHeight = availableHeight / (canvasBounds.getHeight() + padding);
        //technically this is effected by divider style, not sure how to compute that yet
        double availableWidth = newValue12.doubleValue();
        double scaleWidth = availableWidth / (canvasBounds.getWidth() + padding);
        double chosenScale = Math.min(scaleWidth, scaleHeight);
//        double newTranslate = ((availableWidth)*(chosenScale)/2 + canvasBounds.getMinX()*2);

        Scale scale = new Scale();
        scale.setPivotX(0);
        scale.setPivotY(0);
        scale.setX(chosenScale / lastScale);
        scale.setY(chosenScale / lastScale);
        graphCanvas.getTransforms().add(scale);
        lastScale = chosenScale;
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentRoot.getStylesheets().add(getClass().getResource("content.css").toExternalForm());
        //create the logger and get a view
        logger = new PCLogger(10);
        try {
            theView = logger.newView();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        state.bind(
                Bindings.createObjectBinding(
                        () -> theView.isEmpty() ? null : theView.get(selectedFrameIndex.get()),
                        selectedFrameIndex
                )
        );

        selectedFrameIndex.addListener((a,b,c) -> {
            int i=0;
        });

        generationValue.bind(
                Bindings.createIntegerBinding(
                        () -> {
                            PCAlgorithmState curState = state.get();
                            return curState == null ? 0 : curState.generation;
                        },
                        state
                )
        );
        generationText.textProperty()
                .bind(generationValue.asString());

        //use a listener to dynamically create/destroy agent displays
        state.addListener(this::handleStateChange);


        //setup a timeline to poll for log updates, and update the number of frames accordingly
        updateCheckingTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(100 / LOG_POLL_RATE), (event) -> {
                    try {
                        theView.update();
                        numberOfFrames.set(theView.size());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                })
        );
        updateCheckingTimeline.setCycleCount(Animation.INDEFINITE);
        updateCheckingTimeline.play();

        launchMemetico();
    }

    public int getNumberOfFrames() {
        return numberOfFrames.get();
    }

    /**
     * Used by the playback controller
     *
     * @return
     */
    public ReadOnlyIntegerProperty numberOfFramesProperty() {
        return numberOfFrames.getReadOnlyProperty();
    }


    /**
     * Allows something else (i.e. the playback controller) to control which frame to show.
     */
    public void bindSelectedFrameIndex(ObservableValue<Number> source) {
        selectedFrameIndex.bind(source);
    }

    private void updateTours() {
        //disable auto scaling
        if (!fxGraph.isEmpty()) {
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

    private void handleStateChange(ObservableValue<? extends PCAlgorithmState> observable, PCAlgorithmState oldValue, PCAlgorithmState newValue) {
        TSPLibInstance baseInstance = baseInstances.get(newValue.instanceName);
        ObservableList<Node> agentNodes = agentsGrid.getChildren();

        try {
            //for all the graphs we are going to keep, if the instance changed, switch to the new one.
            if (oldValue == null || !newValue.instanceName.equals(oldValue.instanceName)) {
                fxGraph = new Euc2DTSPFXGraph(baseInstance);
                graphWrapper.getChildren().clear();
                if (!fxGraph.isEmpty()) {
                    graphWrapper.setCenter(fxGraph.getGraphic());
                    lastScale = 1;
                    //enable auto sizing
                    graphWrapper.widthProperty().addListener(autoSizeListener);
                    autoSizeListener.changed(null, graphWrapper.getWidth(), graphWrapper.getWidth());
                }


            }

            boolean listUpdated = false;

            if (newValue.agents.length < agentNodes.size()) {
                //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                agentNodes.subList(newValue.agents.length, agentNodes.size()).clear();
                listUpdated = true;
            } else if (newValue.agents.length > agentNodes.size()) {
                //add needed agent displays
                for (int i = agentNodes.size(); i < newValue.agents.length; ++i) {
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

//                    //position the node cheaply for now
//                    GridPane.setRowIndex(newNode, i);

                    //add the data to the lists
                    agentNodes.add(newNode);
                    tourDisplayToggles.add(newToggles);
                }
                listUpdated = true;
            }
                //if the list was updated, the re-arrange the cells in the grid
                if(listUpdated) {
                    //use a tree structure for ease of understanding and debugging; populate using known structure from memetico
                    int columnsAllocated = 0;

                    List<GridPositionData> arrangementInstructions = new ArrayList<>(newValue.agents.length); //unordered list of instructions which can be used to assign grid positions

                    //the following implements vertical-then-horizontal arrangement (root is at the center-top)
                    {
                        GridPositionData rootData = new GridPositionData();

                        rootData.id = 0;
                        rootData.row = 0;
                        TreeNode<GridPositionData> root = new TreeNode<>(rootData);
                        //construct the tree by having each node create and attach their children.
                        Queue<TreeNode<GridPositionData>> creationQueue = new ArrayDeque<>();
                        creationQueue.add(root);

                        Stack<TreeNode<GridPositionData>> arrangementStack = new Stack<>();
                        while (!creationQueue.isEmpty()) {
                            TreeNode<GridPositionData> eachNode = creationQueue.remove();
                            GridPositionData eachData = eachNode.getData();
                            int firstChildId = newValue.nAry * eachData.id + 1;
                            int pastChildId = Math.min(firstChildId + newValue.nAry, newValue.agents.length); //value past the end of the to-create list.
                            for (int i = firstChildId; i < pastChildId; ++i) {
                                GridPositionData newData = new GridPositionData();
                                newData.id = i;
                                newData.row = eachData.row + 1;
                                TreeNode<GridPositionData> newNode = new TreeNode<>(newData);
                                eachNode.attach(newNode);
                                creationQueue.add(newNode);
                            }
                            //if it's not an orphan, add it to the stack to column-positioned later;
                            //if it is an orphan, we can position it now; (this ensures the left-most branch is full if the tree were to be unbalanced)
                            if (eachNode.isLeaf()) {
                                eachData.column = columnsAllocated++;
                                arrangementInstructions.add(eachData);
                            } else {
                                arrangementStack.push(eachNode);
                            }
                        }

                        //we'll reuse the queue at the end for setting the actual params; it's slightly less efficient but separating it from the

                        //all lowest-level nodes have been positioned, so we can safely position progressive parents
                        while (!arrangementStack.isEmpty()) {
                            TreeNode<GridPositionData> eachNode = arrangementStack.pop();
                            GridPositionData eachData = eachNode.getData();
                            List<TreeNode<GridPositionData>> eachChildren = eachNode.getChildren();
                            //use the middle for odd-numbers and for evens, base it on which side we are on within the parents
                            int childToAlignTo;
                            if (eachChildren.size() % 2 == 1) {
                                //uneven is the easy option
                                childToAlignTo = eachChildren.size() / 2;
                            } else if (!eachNode.isRoot()) {
                                List<TreeNode<GridPositionData>> eachSiblings = eachNode.getParent().getChildren();
                                if (eachSiblings.indexOf(eachNode) >= eachSiblings.size() / 2) {
                                    childToAlignTo = eachChildren.size() / 2 - 1;
                                } else {
                                    childToAlignTo = eachChildren.size() / 2;
                                }
                            } else {

                                childToAlignTo = eachChildren.size() / 2 - 1;
                            }
                            eachData.column = eachNode.getChildren().get(childToAlignTo).getData().column;
                            arrangementInstructions.add(eachData);
                        }
                    }

                    for(GridPositionData eachData : arrangementInstructions) {
                        Node eachAgent = agentNodes.get(eachData.id);
                        GridPane.setRowIndex(eachAgent, eachData.row);
                        GridPane.setColumnIndex(eachAgent, eachData.column);
                    }
                }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

//todo: try storing them like this. (that is, [0,1,4-6], [0,2,7-9])

//
//            1=0*3+1+0
//            4=(
//                    (
//                            (0*3+1)+a
//                    )*3+1+b
//            )+c
//            7=((((0*3+1)+1)*3)+1)+0
//            10=((((0*3+1)+2)*3)+1)+0
    }

    private void launchMemetico() {
        //launch memetico
        Thread theThread = new Thread(() -> {
            String MetodoConstrutivo = "Nearest Neighbour";
            String BuscaLocal = "Recursive base.Arc Insertion";//Recursive base.Arc Insertion";
            String SingleorDouble = "Double";
            String OPCrossover = "Strategic base.Arc Crossover - SAX",
                    OPReStart = "base.RestartInsertion",
                    OPMutacao = "base.MutationInsertion";
            String structSol = "base.DiCycle";
            String structPop = "Ternary Tree";
            long MaxTime = 100, MaxGenNum;
            int PopSize = 13, mutationRate = 5;


            try {
                File probFile = new File(getClass().getClassLoader().getResource("att532.tsp").getFile());

                TSPLibInstance tspLibInstance = new TSPLibInstance(probFile);

                baseInstances.put(tspLibInstance.getName(), tspLibInstance);

                Instance memeticoInstance;

                switch (tspLibInstance.getDataType()) {
//                        case TSP:
//                            memeticoInstance = new TSPInstance();
//                            break;
                    case ATSP:
                    default:
                        //atsp should be safe (ish) even if it is in fact tsp
                        memeticoInstance = new ATSPInstance();
                }

                //give the memeticoInstance the required data
                memeticoInstance.setDimension(tspLibInstance.getDimension());
                {
                    DistanceTable distanceTable = tspLibInstance.getDistanceTable();
                    double[][] memeticoMat = ((GraphInstance) memeticoInstance).getMatDist();
                    for (int i = 0; i < memeticoInstance.getDimension(); ++i) {
                        for (int k = 0; k < memeticoInstance.getDimension(); ++k) {
                            memeticoMat[i][k] = distanceTable.getDistanceBetween(i, k);
                        }
                    }
                }

                MaxGenNum = (int) (5 * 13 * Math.log(13) * Math.sqrt(((GraphInstance) memeticoInstance).getDimension()));

                FileOutputStream dataOut = null;
                dataOut = new FileOutputStream("result.txt");
                DataOutputStream fileOut = new DataOutputStream(dataOut);

                FileOutputStream compact_dataOut = new FileOutputStream("result_fim.txt");
                DataOutputStream compact_fileOut = new DataOutputStream(compact_dataOut);

                long targetCost; //for letting the solver know when it's found the optimal (if known)
                //a280: this one has a tour
//                    {
//                        File tourFile = new File(getClass().getClassLoader().getResource("a280.opt.tour").getFile());
//                        tspLibInstance.addTour(tourFile);
//                        targetCost = (long) tspLibInstance.getTours().get(0).distance(tspLibInstance);
////                        tspLibInstance.getTours().clear();
//                    }
                //att532: this one just has known cost
                targetCost = 27686;

                Memetico meme = new Memetico(logger, memeticoInstance, structSol, structPop, MetodoConstrutivo,
                        PopSize, mutationRate, BuscaLocal, OPCrossover, OPReStart, OPMutacao,
                        MaxTime, MaxGenNum, tspLibInstance.getName(), targetCost, fileOut,
                        compact_fileOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        theThread.start();
    }

}


