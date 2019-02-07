package org.marcos.uon.tspaidemo.gui.memetico;

import com.fxgraph.graph.PannableCanvas;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;
import memetico.*;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.graph.DistanceTable;
import org.marcos.uon.tspaidemo.fxgraph.Euc2DTSPFXGraph;
import org.marcos.uon.tspaidemo.gui.main.ContentController;
import org.marcos.uon.tspaidemo.gui.memetico.options.OptionsBoxController;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;
import org.marcos.uon.tspaidemo.util.log.ValidityFlag;
import org.marcos.uon.tspaidemo.util.tree.TreeNode;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MemeticoContentController implements ContentController {
    //used during agent display arrangement
    private static class GridPositionData {
        public int id = -1;
        public int row = -1;
        public int column = -1;
    }

    //todo: possibly make this a parameter instead
//    /**
//     * Measured as checks per second
//     */
//    public static final double LOG_POLL_RATE = 60;

    private final ObjectProperty<Duration> frameInterval = new SimpleObjectProperty<>(Duration.millis(100/60.0));

    public static final MemeticoConfiguration DEFAULT_CONFIGURATION = new MemeticoConfiguration(
            new File(MemeticoContentController.class.getClassLoader().getResource("a280.tsp").getFile()),
            new File(MemeticoContentController.class.getClassLoader().getResource("a280.opt.tour").getFile())
    );

    @FXML
    private VBox contentRoot;
    @FXML
    private Text generationText;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private BorderPane graphWrapper;

    private OptionsBoxController optionsBoxController;

    private transient ObjectProperty<PCAlgorithmState> state = new SimpleObjectProperty<>();
    private transient PCLogger logger;
    private transient BasicLogger<PCAlgorithmState>.View theView;
    private final transient Timeline redrawTimeline = new Timeline();
    private transient ReadOnlyIntegerWrapper numberOfFrames = new ReadOnlyIntegerWrapper(0);
    private transient IntegerProperty selectedFrameIndex = new SimpleIntegerProperty(0);

    private IntegerProperty generationValue = new SimpleIntegerProperty();
    private Map<String, TSPLibInstance> baseInstances = new HashMap<>();

    private String lastDrawnGraphName = null;
    private int lastDrawnFrameIndex = -1;
    private Euc2DTSPFXGraph fxGraph;

    private double lastScale = 0;
    ValidityFlag.Synchronised currentMemeticoContinuePermission;
    Thread memeticoThread = null;

    private void autoSizeListener(ObservableValue<? extends Number> observable12, Number oldValue12, Number newValue12){
        //reset to the old position
        PannableCanvas graphCanvas = fxGraph.getCanvas();
        BoundingBox canvasBounds = fxGraph.getLogicalBounds();

        double availableHeight = graphWrapper.getHeight();
        double padding = Math.max(canvasBounds.getMinX() * 2, canvasBounds.getMinY() * 2);
        double scaleHeight = availableHeight / (canvasBounds.getHeight() + padding);
        //technically this is effected by divider style, not sure how to compute that yet
        double availableWidth = graphWrapper.getWidth();
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
        logger = new PCLogger(1);
        try {
            theView = logger.newView();
                //set up the content display with an observable reference to the current state to display.
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "options/options_box.fxml"
                        )
                );
                loader.load();
                optionsBoxController = loader.getController();
                optionsBoxController.agentCountProperty().bind(
                        Bindings.createIntegerBinding(
                                () -> (state.isNull().get() ? 0 : state.get().agents.length),
                                state
                        )
                );

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

        //todo: possibly collect this into common base class or leave frameupdate to only be called externally by a containing controller?
        frameInterval.addListener(
                (observable, oldValue, newValue) -> {
                    redrawTimeline.stop();
                    ObservableList<KeyFrame> frames = redrawTimeline.getKeyFrames();
                    frames.clear();
                    frames.add(new KeyFrame(frameInterval.get(), (e) -> frameUpdate()));
                    redrawTimeline.play();
                }
        );

        //setup a timeline to poll for log updates, and update the number of frames accordingly

        redrawTimeline.getKeyFrames().add(new KeyFrame(frameInterval.get(), (e) -> frameUpdate()));
        redrawTimeline.setCycleCount(Animation.INDEFINITE);
        redrawTimeline.play();

        optionsBoxController.memeticoConfigurationProperty().addListener( (observable, oldValue, newValue) -> launchMemetico());

        //load the default configuration
        optionsBoxController.setMemeticoConfiguration(DEFAULT_CONFIGURATION);
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
            //reset and re-draw tours
            fxGraph.clearPredictions();

            TSPLibInstance theInstance = baseInstances.get(state.get().instanceName);
            List<BooleanProperty[]> toggles = optionsBoxController.getSolutionDisplayToggles();
            for (int i = 0; i < toggles.size(); ++i) {
                BooleanProperty[] eachToggles = toggles.get(i);
                for (int k = 0; k < eachToggles.length; ++k) {
                    if (eachToggles[k].get()) {
                        PCAlgorithmState.LightDiCycle eachSolution = (
                                k == 0 ?
                                        state.get()
                                                .agents[i]
                                                .pocket
                                        :
                                        state.get()
                                                .agents[i]
                                                .current
                        );
                        int nextCity, city = 0;
                        int[][] edgesToAdd = new int[theInstance.getDimension()][];
                        for(int j = 0; j<theInstance.getDimension(); ++j) {
                            nextCity = eachSolution.arcArray[city].tip;
                            edgesToAdd[j] = new int[]{city, nextCity};
                            city = nextCity;
                        }
                        fxGraph.addPredictionEdges(
                                Arrays.asList(edgesToAdd)
                        );
                    }
                }
            }
            fxGraph.endUpdate();
        }
    }

    private void frameUpdate() {
        try {
            theView.update();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        numberOfFrames.set(theView.size());
        PCAlgorithmState currentValue = state.get();
        if(selectedFrameIndex.get() == lastDrawnFrameIndex || state.get() == null) {
            return; //cancel the update
        }

        lastDrawnFrameIndex = selectedFrameIndex.get();
        TSPLibInstance baseInstance = baseInstances.get(currentValue.instanceName);
        ObservableList<Node> agentNodes = agentsGrid.getChildren();

        try {
            //for all the graphs we are going to keep, if the instance changed, switch to the new one.
            if (!currentValue.instanceName.equals(lastDrawnGraphName)) {
                fxGraph = new Euc2DTSPFXGraph(baseInstance);
                graphWrapper.getChildren().clear();
                if (!fxGraph.isEmpty()) {
                    graphWrapper.setCenter(fxGraph.getGraphic());
                    lastScale = 1;
                    //enable auto sizing
                    graphWrapper.widthProperty().addListener(this::autoSizeListener);
                    graphWrapper.heightProperty().addListener(this::autoSizeListener);
                    autoSizeListener(null, -1, -1);
                }
                lastDrawnGraphName = currentValue.instanceName;
            }

            boolean listUpdated = false;

            if (currentValue.agents.length < agentNodes.size()) {
                //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                agentNodes.subList(currentValue.agents.length, agentNodes.size()).clear();
                listUpdated = true;
            } else if (currentValue.agents.length > agentNodes.size()) {
                //add needed agent displays
                for (int i = agentNodes.size(); i < currentValue.agents.length; ++i) {
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

                    //give the state to the controller
                    AgentDisplay newNode = new AgentDisplay(newId, newState);

//                    //position the node cheaply for now
//                    GridPane.setRowIndex(newNode, i);

                    //add the data to the lists
                    agentNodes.add(newNode);
                }
                listUpdated = true;
            }
                //if the list was updated, the re-arrange the cells in the grid
                if(listUpdated) {
                    //use a tree structure for ease of understanding and debugging; populate using known structure from memetico
                    int columnsAllocated = 0;

                    List<GridPositionData> arrangementInstructions = new ArrayList<>(currentValue.agents.length); //unordered list of instructions which can be used to assign grid positions

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
                            int firstChildId = currentValue.nAry * eachData.id + 1;
                            int pastChildId = Math.min(firstChildId + currentValue.nAry, currentValue.agents.length); //value past the end of the to-create list.
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

                                //we want placeholders to leave empty columns between subpopulations visually
                                if(eachData.id != currentValue.agents.length-1 && eachNode.getParent().getChildren().indexOf(eachNode) == eachNode.getParent().getChildren().size()-1) {
                                    GridPositionData placeholderData = new GridPositionData();
                                    placeholderData.id = -1;
                                    placeholderData.row = eachData.row;
                                    placeholderData.column = columnsAllocated++;
                                    arrangementInstructions.add(placeholderData);
                                }
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
                        if(eachData.id == -1) {
                            //create a placeholder
                            Pane placeHolder = new Pane();
                            placeHolder.setMinWidth(25);
                            agentNodes.add(placeHolder);
                            GridPane.setRowIndex(placeHolder, eachData.row);
                            GridPane.setColumnIndex(placeHolder, eachData.column);
                            GridPane.setColumnSpan(placeHolder, 1);
                        } else {
                            Node eachAgent = agentNodes.get(eachData.id);
                            GridPane.setRowIndex(eachAgent, eachData.row);
                            GridPane.setColumnIndex(eachAgent, eachData.column);
                        }
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
        if(memeticoThread != null && memeticoThread.isAlive()) {
            //tell memetico to stop, then wait for that to happen safely
            currentMemeticoContinuePermission.invalidate();
            try {
                memeticoThread.join();
                logger.reset();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        currentMemeticoContinuePermission = new ValidityFlag.Synchronised();
        final MemeticoConfiguration finalizedConfig = optionsBoxController.getMemeticoConfiguration();
        final ValidityFlag.ReadOnly finalizedContinuePermission = currentMemeticoContinuePermission.getReadOnly();
        //launch memetico
        memeticoThread = new Thread(() -> {
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
            int numReplications = 1;

            try {
                TSPLibInstance tspLibInstance = new TSPLibInstance(finalizedConfig.problemFile);

                baseInstances.put(tspLibInstance.getName(), tspLibInstance);

                Instance memeticoInstance;

                switch (tspLibInstance.getDataType()) {
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

                MaxGenNum = (int) (5 * 13 * Math.log(13) * Math.sqrt(memeticoInstance.getDimension()));

                FileOutputStream dataOut = null;
                dataOut = new FileOutputStream("result.txt");
                DataOutputStream fileOut = new DataOutputStream(dataOut);

                FileOutputStream compact_dataOut = new FileOutputStream("result_fim.txt");
                DataOutputStream compact_fileOut = new DataOutputStream(compact_dataOut);

                long targetCost; //for letting the solver know when it's found the optimal (if known)
                switch (finalizedConfig.solutionType) {
                    case TOUR:
                        tspLibInstance.addTour(finalizedConfig.tourFile);
                        targetCost = (long) tspLibInstance.getTours().get(tspLibInstance.getTours().size()-1).distance(tspLibInstance);
                        break;
                    case COST:
                        targetCost = finalizedConfig.targetCost;
                    default:
                        targetCost = -1;

                }


//                File probFile = new File(getClass().getClassLoader().getResource("att532.tsp").getFile());
//
//                TSPLibInstance tspLibInstance = new TSPLibInstance(probFile);
//
//                baseInstances.put(tspLibInstance.getName(), tspLibInstance);
//
//                Instance memeticoInstance;
//
//                switch (tspLibInstance.getDataType()) {
////                        case TSP:
////                            memeticoInstance = new TSPInstance();
////                            break;
//                    case ATSP:
//                    default:
//                        //atsp should be safe (ish) even if it is in fact tsp
//                        memeticoInstance = new ATSPInstance();
//                }
//
//                //give the memeticoInstance the required data
//                memeticoInstance.setDimension(tspLibInstance.getDimension());
//                {
//                    DistanceTable distanceTable = tspLibInstance.getDistanceTable();
//                    double[][] memeticoMat = ((GraphInstance) memeticoInstance).getMatDist();
//                    for (int i = 0; i < memeticoInstance.getDimension(); ++i) {
//                        for (int k = 0; k < memeticoInstance.getDimension(); ++k) {
//                            memeticoMat[i][k] = distanceTable.getDistanceBetween(i, k);
//                        }
//                    }
//                }
//
//                MaxGenNum = (int) (5 * 13 * Math.log(13) * Math.sqrt(((GraphInstance) memeticoInstance).getDimension()));
//
//                FileOutputStream dataOut = null;
//                dataOut = new FileOutputStream("result.txt");
//                DataOutputStream fileOut = new DataOutputStream(dataOut);
//
//                FileOutputStream compact_dataOut = new FileOutputStream("result_fim.txt");
//                DataOutputStream compact_fileOut = new DataOutputStream(compact_dataOut);
//
//                long targetCost; //for letting the solver know when it's found the optimal (if known)
//                //a280: this one has a tour
////                    {
////                        File tourFile = new File(getClass().getClassLoader().getResource("a280.opt.tour").getFile());
////                        tspLibInstance.addTour(tourFile);
////                        targetCost = (long) tspLibInstance.getTours().get(0).distance(tspLibInstance);
//////                        tspLibInstance.getTours().reset();
////                    }
//                //att532: this one just has known cost
//                targetCost = 27686;

                Memetico meme = new Memetico(logger, finalizedContinuePermission, memeticoInstance, structSol, structPop, MetodoConstrutivo,
                        PopSize, mutationRate, BuscaLocal, OPCrossover, OPReStart, OPMutacao,
                        MaxTime, MaxGenNum, numReplications, tspLibInstance.getName(), targetCost, fileOut,
                        compact_fileOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        memeticoThread.start();
    }

    public void showOptionsBox() {
        final Stage optionsStage = new Stage();
        Scene newScane = new Scene(optionsBoxController.getRoot(), 300, 200);
        optionsStage.setScene(newScane);
        optionsStage.show();
    }

    public Duration getFrameInterval() {
        return frameInterval.get();
    }

    public ObjectProperty<Duration> frameIntervalProperty() {
        return frameInterval;
    }

    public void setFrameInterval(Duration frameInterval) {
        this.frameInterval.set(frameInterval);
    }


}


