package org.marcos.uon.tspaidemo.gui.memetico;

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
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import memetico.*;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import memetico.util.CrossoverOpName;
import memetico.util.LocalSearchOpName;
import memetico.util.RestartOpName;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.TSPLibTour;
import org.jorlib.io.tspLibReader.graph.DistanceTable;
import org.marcos.uon.tspaidemo.canvas.CanvasTSPGraph;
import org.marcos.uon.tspaidemo.gui.main.ContentController;
import org.marcos.uon.tspaidemo.gui.memetico.options.OptionsBoxController;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;
import org.marcos.uon.tspaidemo.util.log.ValidityFlag;
import org.marcos.uon.tspaidemo.util.tree.TreeNode;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MemeticoContentController implements ContentController {
    //used during agent display arrangement
    private static class GridPositionData {
        public int id = -1;
        public int row = -1;
        public int column = -1;
    }

//    private final ObjectProperty<Duration> frameInterval = new SimpleObjectProperty<>(Duration.millis(100/60.0));

    public static final ProblemConfiguration DEFAULT_PROBLEM = new ProblemConfiguration(
            new File(MemeticoContentController.class.getClassLoader().getResource("a280.tsp").getFile()),
            new File(MemeticoContentController.class.getClassLoader().getResource("a280.opt.tour").getFile())
    );

    public static final MemeticoConfiguration DEFAULT_CONFIG = new MemeticoConfiguration(13, 5, LocalSearchOpName.RAI.toString(), CrossoverOpName.SAX.toString(), RestartOpName.INSERTION.toString());

    @FXML
    private ScrollPane infoPane;
    @FXML
    private VBox infoBox;
    @FXML
    private HBox contentRoot;
    @FXML
    private Text txtGeneration, txtProblemName, txtTargetCost;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private BorderPane titleBar, graphContainer;

//    @FXML
//    private TreeView<PCAlgorithmState.AgentState> agentsTree;

    private List<AgentDisplay> agentControllers = new ArrayList<>();

    private List<TreeItem<PCAlgorithmState.AgentState>> agentItems = new ArrayList<>();

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
    private ValidityFlag.Synchronised currentMemeticoContinuePermission;
    private Thread memeticoThread = null;

    private void autoSizeListener(ObservableValue<? extends Number> observable12, Number oldValue12, Number newValue12){
        BoundingBox canvasBounds = displayGraph.getLogicalBounds();

        double availableHeight = graphContainer.getHeight();
        double padding = Math.max(canvasBounds.getMinX() * 2, canvasBounds.getMinY() * 2);
        double scaleHeight = availableHeight / (canvasBounds.getHeight() + padding);
        //technically this is effected by divider style, not sure how to compute that yet
        double availableWidth = graphContainer.getWidth();
        double scaleWidth = availableWidth / (canvasBounds.getWidth() + padding);
        double chosenScale = Math.min(scaleWidth, scaleHeight);
        displayGraph.setScale(chosenScale);

        displayGraph.getGraphic().setMaxSize(canvasBounds.getWidth()*chosenScale, canvasBounds.getHeight()*chosenScale);
        toursOutdated = true;
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        agentsTree.setEditable(false);
        contentRoot.getStylesheets().add(getClass().getResource("content.css").toExternalForm());
        contentRoot.getStylesheets().add(getClass().getResource("../main/common.css").toExternalForm());

        infoPane.prefViewportHeightProperty().bind(infoBox.heightProperty());
        infoPane.prefViewportWidthProperty().bind(infoBox.widthProperty());
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
//        ObjectProperty<MemeticoConfiguration> configProp = optionsBoxController.memeticoConfigurationProperty();
        ObjectProperty<ProblemConfiguration> problemProp = optionsBoxController.problemConfigurationProperty();
        txtProblemName.textProperty().bind(
                Bindings.createStringBinding(
                        () -> state.get() == null ? "None" : state.get().instanceName,
                        state
                )
        );
        txtTargetCost.textProperty().bind(
                Bindings.createStringBinding(
                        () -> {
                            ProblemConfiguration config = problemProp.get();
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

        //set the default configuration
        optionsBoxController.setMemeticoConfiguration(DEFAULT_CONFIG);
        optionsBoxController.setProblemConfiguration(DEFAULT_PROBLEM);


        //
//        optionsBoxController.memeticoConfigurationProperty().addListener( (observable, oldValue, newValue) -> launchMemetico());
//        optionsBoxController.problemConfigurationProperty().addListener( (observable, oldValue, newValue) -> launchMemetico());
        optionsBoxController.setApplyConfigFunc(this::launchMemetico);
        //launch memetico with defaults
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
//        agentsTree.setCellFactory(p -> new AgentTreeCell());
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
                    graphContainer.getChildren().clear();
                    if (!displayGraph.isEmpty()) {
                        graphContainer.setCenter(displayGraph.getGraphic());
                        //enable auto sizing
                        graphContainer.widthProperty().addListener(this::autoSizeListener);
                        graphContainer.heightProperty().addListener(this::autoSizeListener);
                        autoSizeListener(null, -1, -1);
                    }
                    lastDrawnGraphName = currentValue.instanceName;
                }

                boolean listUpdated = false;
                int oldCount = optionsBoxController.getSolutionDisplayToggles().size(), newCount = currentValue.agents.length;
                optionsBoxController.adjustAgentOptionsDisplay(oldCount, newCount);
                if(newCount != oldCount) {
                    listUpdated = true;
                }
                //for manual layouts
                if (newCount < oldCount) {
                    //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                    agentControllers.subList(newCount, agentControllers.size()).clear();
                    agentNodes.subList(newCount, agentNodes.size()).clear();
                } else if (newCount > oldCount) {
                    //add needed agent displays
                    for (int i = oldCount; i < newCount; ++i) {
                        //give the state to the controller
                        AgentDisplay newNode = new AgentDisplay();

                        agentControllers.add(newNode);
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
                }
                //if the list was updated, the re-arrange the cells in the grid
                if (listUpdated) {
                    //use a tree structure for ease of understanding and debugging; populate using known structure from memetico

                    List<GridPositionData> arrangementInstructions = new ArrayList<>(currentValue.agents.length); //unordered list of instructions which can be used to assign grid positions

//                    //the following implements a vertical-then-horizontal arrangement (root is at the center-top)
//                    {
//                        GridPositionData rootData = new GridPositionData();
//
//                        rootData.id = 0;
//                        rootData.row = 0;
//                        TreeNode<GridPositionData> root = new TreeNode<>(rootData);
//                        //construct the tree by having each node create and attach their children.
//                        Queue<TreeNode<GridPositionData>> creationQueue = new ArrayDeque<>();
//                        creationQueue.add(root);
//
//                        Stack<TreeNode<GridPositionData>> arrangementStack = new Stack<>();
//                        while (!creationQueue.isEmpty()) {
//                            TreeNode<GridPositionData> eachNode = creationQueue.remove();
//                            GridPositionData eachData = eachNode.getData();
//                            int firstChildId = currentValue.nAry * eachData.id + 1;
//                            int pastChildId = Math.min(firstChildId + currentValue.nAry, currentValue.agents.length); //value past the end of the to-create list.
//                            for (int i = firstChildId; i < pastChildId; ++i) {
//                                GridPositionData newData = new GridPositionData();
//                                newData.id = i;
//                                newData.row = eachData.row + 1;
//                                TreeNode<GridPositionData> newNode = new TreeNode<>(newData);
//                                eachNode.attach(newNode);
//                                creationQueue.add(newNode);
//                            }
//                            //if it's not an orphan, add it to the stack to column-positioned later;
//                            //if it is an orphan, we can position it now; (this ensures the left-most branch is full if the tree were to be unbalanced)
//                            if (eachNode.isLeaf()) {
//                                eachData.column = columnsAllocated++;
//
//                                //we want placeholders to leave empty columns between subpopulations visually
//                                if (eachData.id != currentValue.agents.length - 1 && eachNode.parent().children().indexOf(eachNode) == eachNode.parent().children().size() - 1) {
//                                    GridPositionData placeholderData = new GridPositionData();
//                                    placeholderData.id = -1;
//                                    placeholderData.row = eachData.row;
//                                    placeholderData.column = columnsAllocated++;
//                                    arrangementInstructions.add(placeholderData);
//                                }
//                                arrangementInstructions.add(eachData);
//                            } else {
//                                arrangementStack.push(eachNode);
//                            }
//                        }

                    //the following implements a horizontal-then-vertical arrangement (root is top-left)
                    {
                        GridPositionData rootData = new GridPositionData();

                        rootData.id = 0;
                        rootData.column = 0;
                        TreeNode<GridPositionData> root = new TreeNode<>(rootData);

                        int seen = 0;
                        //construct the tree by having each node create and attach their children.
                        Stack<TreeNode<GridPositionData>> creationStack = new Stack<>();

                        creationStack.push(root);
                        while (!creationStack.isEmpty()) {
                            TreeNode<GridPositionData> eachNode = creationStack.pop();
                            GridPositionData eachData = eachNode.getData();
                            int firstChildId = currentValue.nAry * eachData.id + 1;
                            int pastChildId = Math.min(firstChildId + currentValue.nAry, currentValue.agents.length); //value past the end of the to-create list.
                            for (int i = firstChildId; i < pastChildId; ++i) {
                                GridPositionData newData = new GridPositionData();
                                newData.id = i;
                                newData.column = eachData.column + 1;
                                TreeNode<GridPositionData> newNode = new TreeNode<>(newData);
                                eachNode.attach(newNode);
                                creationStack.push(newNode);
                            }
                        }

                        //now walk the root
                        TreeNode<GridPositionData> current = root;
                        do {
                            GridPositionData curData = current.getData();
                            //if unseen, (row == -1), drill down
                            if (curData.row == -1) {
                                curData.row = seen++;
                                arrangementInstructions.add(curData);
                                if (!current.isLeaf()) {
                                    current = current.children().get(0);
                                    continue;
                                }
                            }
                            if (current.hasNextSibling()) {
                                current = current.nextSibling();
                            } else {
                                //if we can't drill down further, drill up until we can
                                while (!current.isRoot() && current.getData().row != -1) {
                                    current = current.parent();
                                    if (current.hasNextSibling()) {
                                        current = current.nextSibling();
                                    }
                                }
                            }
                        } while (current != root);
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

//                    //the following populates the treeview
//                    {
//                        agentItems.clear();
//                        agentItems.addAll(Stream.generate(() -> (TreeItem<PCAlgorithmState.AgentState>)null).limit(newCount).collect(Collectors.toList()));
//                        TreeItem<PCAlgorithmState.AgentState> rootItem = new TreeItem<>();
//                        agentItems.set(0, rootItem);
//                        agentsTree.setRoot(rootItem);
//                        Stack<Integer> creationStack = new Stack<>();
//
//                        creationStack.push(0);
//                        while (!creationStack.isEmpty()) {
//                            int eachNode = creationStack.pop();
//                            TreeItem<PCAlgorithmState.AgentState> eachItem = agentItems.get(eachNode);
//                            eachItem.setExpanded(true);
//                            int firstChildId = currentValue.nAry * eachNode + 1;
//                            int pastChildId = Math.min(firstChildId + currentValue.nAry, currentValue.agents.length); //value past the end of the to-create list.
//                            for (int i = firstChildId; i < pastChildId; ++i) {
//                                TreeItem<PCAlgorithmState.AgentState> newItem = new TreeItem<>();
//                                eachItem.getChildren().add(newItem);
//                                agentItems.set(i, newItem);
//                                creationStack.push(i);
//                            }
//                        }
//
//                    }
                }
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }

            for(int i=0; i<agentControllers.size(); ++i) {
                agentControllers.get(i).setState(state.get().agents[i]);
            }
//            for(int i=0; i<agentItems.size(); ++i) {
//                agentItems.get(i).setValue(state.get().agents[i]);
//            }

        } else if(state.get() == null){
            toursOutdated = false;
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
        final ProblemConfiguration finalizedProblem = optionsBoxController.getProblemConfiguration();
        final MemeticoConfiguration finalizedConfig = optionsBoxController.getMemeticoConfiguration();
        final ValidityFlag.ReadOnly finalizedContinuePermission = currentMemeticoContinuePermission.getReadOnly();
        try {
            TSPLibInstance tspLibInstance = new TSPLibInstance(finalizedProblem.problemFile);

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

            long maxGenerations = finalizedConfig.maxGenerations != 0 ? finalizedConfig.maxGenerations : (int) (5 * 13 * Math.log(13) * Math.sqrt(memeticoInstance.getDimension()));
            FileOutputStream dataOut = null;
            dataOut = new FileOutputStream("result.txt");
            DataOutputStream fileOut = new DataOutputStream(dataOut);

            FileOutputStream compact_dataOut = new FileOutputStream("result_fim.txt");
            DataOutputStream compact_fileOut = new DataOutputStream(compact_dataOut);

            long targetCost; //for letting the solver know when it's found the optimal (if known)
            switch (finalizedProblem.solutionType) {
                case TOUR:
                    tspLibInstance.addTour(finalizedProblem.tourFile);
                    targetCost = (long) tspLibInstance.getTours().get(tspLibInstance.getTours().size()-1).distance(tspLibInstance);
                    break;
                case COST:
                    targetCost = finalizedProblem.targetCost;
                    break;
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

            //launch memetico
            memeticoThread = new Thread(() -> {
    //            String MetodoConstrutivo = "Nearest Neighbour";
    //            String BuscaLocal = "Recursive Arc Insertion";//Recursive base.Arc Insertion";
    ////            String SingleorDouble = "Double";
    //            String OPCrossover = "Strategic Arc Crossover - SAX",
    //                    OPReStart = "RestartInsertion",
    //                    OPMutacao = "MutationInsertion";
    //            String structSol = "DiCycle";
    //            String structPop = "Ternary Tree";
    //            long MaxTime = 100, MaxGenNum;
    //            int PopSize = 13, mutationRate = 5;
    //            int numReplications = 1;


                try {
                    Memetico meme = new Memetico(logger, finalizedContinuePermission, memeticoInstance, finalizedConfig.solutionStructure, finalizedConfig.populationStructure, finalizedConfig.constructionAlgorithm,
                            finalizedConfig.populationSize, finalizedConfig.mutationRate, finalizedConfig.localSearchOp, finalizedConfig.crossoverOp, finalizedConfig.restartOp, finalizedConfig.mutationOp,
                            finalizedConfig.maxTime, maxGenerations, finalizedConfig.numReplications, tspLibInstance.getName(), targetCost, fileOut,
                            compact_fileOut);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            memeticoThread.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showOptionsBox() {
        optionsBoxController.open();
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


