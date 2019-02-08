package org.marcos.uon.tspaidemo.gui.memetico;

import com.fxgraph.graph.PannableCanvas;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import memetico.*;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.TSPLibTour;
import org.jorlib.io.tspLibReader.graph.DistanceTable;
import org.marcos.uon.tspaidemo.canvas.CanvasTSPGraph;
import org.marcos.uon.tspaidemo.fxgraph.Euc2DTSPFXGraph;
import org.marcos.uon.tspaidemo.gui.main.ContentController;
import org.marcos.uon.tspaidemo.gui.memetico.options.OptionsBoxController;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;
import org.marcos.uon.tspaidemo.util.log.ValidityFlag;
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

//    private final ObjectProperty<Duration> frameInterval = new SimpleObjectProperty<>(Duration.millis(100/60.0));

    public static final MemeticoConfiguration DEFAULT_CONFIGURATION = new MemeticoConfiguration(
            new File(MemeticoContentController.class.getClassLoader().getResource("a280.tsp").getFile()),
            new File(MemeticoContentController.class.getClassLoader().getResource("a280.opt.tour").getFile())
    );

    @FXML
    private VBox contentRoot;
    @FXML
    private Text txtGeneration, txtProblemName, txtTargetCost;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private BorderPane graphWrapper;

    private OptionsBoxController optionsBoxController;

    private transient ObjectProperty<PCAlgorithmState> state = new SimpleObjectProperty<>();
    private transient PCLogger logger;
    private transient BasicLogger<PCAlgorithmState>.View theView;
    private transient ReadOnlyIntegerWrapper numberOfFrames = new ReadOnlyIntegerWrapper(0);
    private transient IntegerProperty selectedFrameIndex = new SimpleIntegerProperty(0);

    private IntegerProperty generationValue = new SimpleIntegerProperty();
    private Map<String, TSPLibInstance> baseInstances = new HashMap<>();

    private String lastDrawnGraphName = null;
    private int lastDrawnFrameIndex = -1;
    private CanvasTSPGraph displayGraph;

//    private double lastScale = 0;
    private boolean toursOutdated = false;
    ValidityFlag.Synchronised currentMemeticoContinuePermission;
    Thread memeticoThread = null;

    private void autoSizeListener(ObservableValue<? extends Number> observable12, Number oldValue12, Number newValue12){
        BoundingBox canvasBounds = displayGraph.getLogicalBounds();

        double availableHeight = graphWrapper.getHeight();
        double padding = Math.max(canvasBounds.getMinX() * 2, canvasBounds.getMinY() * 2);
        double scaleHeight = availableHeight / (canvasBounds.getHeight() + padding);
        //technically this is effected by divider style, not sure how to compute that yet
        double availableWidth = graphWrapper.getWidth();
        double scaleWidth = availableWidth / (canvasBounds.getWidth() + padding);
        double chosenScale = Math.min(scaleWidth, scaleHeight);
        displayGraph.setScale(chosenScale);
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

        } catch (InterruptedException | IOException e) {
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
        txtGeneration.textProperty()
                .bind(generationValue.asString());
        ObjectProperty<MemeticoConfiguration> configProp = optionsBoxController.memeticoConfigurationProperty();
        txtProblemName.textProperty().bind(
                Bindings.createStringBinding(
                        () -> state.get() == null ? "None" : state.get().instanceName,
                        state
                )
        );
        txtTargetCost.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            MemeticoConfiguration config = configProp.get();
                            if (config == null) {
                                return "Unknown";
                            } else {
                                switch (config.solutionType) {
                                    case TOUR:
                                        if (state.get() == null) {
                                            return "Unknown";
                                        } else {
                                            TSPLibInstance theInstance = baseInstances.get(state.get().instanceName);
                                            return String.valueOf((int)theInstance.getTours().get(theInstance.getTours().size() - 1).distance(theInstance));
                                        }
                                    case COST:
                                        return String.valueOf(config.targetCost);
                                    default:
                                        return "ERROR";
                                }
                            }
                        },
                        state
                )
        );

        optionsBoxController.getTargetDisplayToggle().addListener((e,o,n) -> toursOutdated = true);
        optionsBoxController.getTargetDisplayToggle().set(true);

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
//        unbindSelectedFrameIndex();
        selectedFrameIndex.bind(source);
    }

    public void unbindSelectedFrameIndex() {
        selectedFrameIndex.unbind();
    }

    private void updateTours() {
        //disable auto scaling
        if (!displayGraph.isEmpty()) {
            //reset and re-draw tours
            displayGraph.clearTargets();
            displayGraph.clearPredictions();

            TSPLibInstance theInstance = baseInstances.get(state.get().instanceName);
            if(optionsBoxController.getTargetDisplayToggle().get()) {
                for(TSPLibTour eachTour : theInstance.getTours()) {
                    List<int[]> edges = eachTour.toEdges()
                            .stream()
                            .map(each -> new int[]{each.getId1(), each.getId2()})
                            .collect(Collectors.toList());
                    displayGraph.addTargetEdges(
                        edges
                    );
                }
            }

            PCAlgorithmState theState = state.get();
            List<BooleanProperty[]> toggles = optionsBoxController.getSolutionDisplayToggles();
            for (int i = 0; i < toggles.size(); ++i) {
                BooleanProperty[] eachToggles = toggles.get(i);
                for (int k = 0; k < eachToggles.length; ++k) {
                    if (eachToggles[k].get()) {
                        PCAlgorithmState.LightDiCycle eachSolution = (
                                k == 0 ?
                                        theState
                                                .agents[i]
                                                .pocket
                                        :
                                        theState
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
                        displayGraph.addPredictionEdges(
                                Arrays.asList(edgesToAdd)
                        );
                    }
                }
            }
        }
        displayGraph.draw();
        toursOutdated = false;
    }

    public void frameCountUpdate() {
        try {
            theView.update();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        numberOfFrames.set(theView.size());
    }

    public void contentUpdate() {
        PCAlgorithmState currentValue = state.get();
        //only check the complex logic if we can draw a state
        if(selectedFrameIndex.get() != lastDrawnFrameIndex && state.get() != null) {
            toursOutdated = true;
            lastDrawnFrameIndex = selectedFrameIndex.get();
            TSPLibInstance baseInstance = baseInstances.get(currentValue.instanceName);
            ObservableList<Node> agentNodes = agentsGrid.getChildren();

            try {
                //for all the graphs we are going to keep, if the instance changed, switch to the new one.
                if (!currentValue.instanceName.equals(lastDrawnGraphName)) {
                    displayGraph = new CanvasTSPGraph(baseInstance);
                    graphWrapper.getChildren().clear();
                    if (!displayGraph.isEmpty()) {
                        graphWrapper.setCenter(displayGraph.getGraphic());
                        //enable auto sizing
                        graphWrapper.widthProperty().addListener(this::autoSizeListener);
                        graphWrapper.heightProperty().addListener(this::autoSizeListener);
                        autoSizeListener(null, -1, -1);
                    }
                    lastDrawnGraphName = currentValue.instanceName;
                }

                boolean listUpdated = false;
                int oldCount = optionsBoxController.getSolutionDisplayToggles().size(), newCount = currentValue.agents.length;
                optionsBoxController.adjustAgentOptionsDisplay(oldCount, newCount);

                if (newCount < oldCount) {
                    //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                    agentNodes.subList(newCount, agentNodes.size()).clear();
                    listUpdated = true;
                } else if (newCount > oldCount) {
                    //add needed agent displays
                    for (int i = oldCount; i < newCount; ++i) {
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
//                        if(!displayGraph.isEmpty()) {
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
                    List<BooleanProperty[]> displayToggles = optionsBoxController.getSolutionDisplayToggles();
                    //addListeners for all the display toggles so that we still get graph display updates even when the playback is paused
                    displayToggles.subList(oldCount, newCount).forEach(each -> {
                        for (BooleanProperty eachToggle : each) {
                            eachToggle.addListener((e, o, n) -> toursOutdated = true);
                        }
                    });

                    if(oldCount == 0 && newCount != 0) {
                        //pre-highlight the best found so far
                        displayToggles.get(0)[0].set(true);
                    }


                    listUpdated = true;
                }
                //if the list was updated, the re-arrange the cells in the grid
                if (listUpdated) {
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
                                if (eachData.id != currentValue.agents.length - 1 && eachNode.getParent().getChildren().indexOf(eachNode) == eachNode.getParent().getChildren().size() - 1) {
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

                    for (GridPositionData eachData : arrangementInstructions) {
                        if (eachData.id == -1) {
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
            } catch (InvalidArgumentException | IOException e) {
                e.printStackTrace();
            }

        }
        if(toursOutdated) {
            updateTours();
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

//    public Duration getFrameInterval() {
//        return frameInterval.get();
//    }
//
//    public ObjectProperty<Duration> frameIntervalProperty() {
//        return frameInterval;
//    }
//
//    public void setFrameInterval(Duration frameInterval) {
//        this.frameInterval.set(frameInterval);
//    }

    /**
     * {@inheritDoc}
     */
    public Parent getRoot() {
        return contentRoot;
    }
}


