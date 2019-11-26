package org.marcos.uon.tspaidemo.gui.memetico;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import memetico.logging.IPCLogger;
import memetico.logging.MemeticoSnapshot;
import memetico.logging.NullPCLogger;
import memetico.util.ProblemInstance;
import org.marcos.uon.tspaidemo.canvas.CanvasTSPGraph;
import org.marcos.uon.tspaidemo.canvas.ViewportGestures;
import org.marcos.uon.tspaidemo.gui.main.ContentController;
import org.marcos.uon.tspaidemo.gui.memetico.agent.AgentDisplay;
import org.marcos.uon.tspaidemo.gui.memetico.options.DisplayOptionsController;
import org.marcos.uon.tspaidemo.gui.memetico.options.RunConfigurationController;
import org.marcos.uon.tspaidemo.util.tree.TreeNode;
import tsplib4j.TSPLibInstance;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;

public class MemeticoContentController implements ContentController {

    //used during agent display arrangement
    private static class GridPositionData {
        public int id = -1;
        public int row = -1;
        public int column = -1;
        public int colorIndex = -1;
    }

    @FXML
    private ScrollPane infoPane;
    @FXML
    private StackPane infoStack;
    @FXML
    private VBox infoBox;
    @FXML
    private VBox contentRoot;
    @FXML
    private Text txtGeneration, txtProblemName, txtTargetCost, txtBestCost, txtAvgGenTime, txtTimeTotal, txtGenerationCount, txtRunningStatus;
    @FXML
    private Label lblTargetColor, lblBestColor;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private BorderPane titleBar, graphContainer;
    @FXML
    private AnchorPane graphWrapper;

    private List<AgentDisplay> agentControllers = new ArrayList<>();

    private RunConfigurationController runConfigurationController;

    private DisplayOptionsController displayOptionsController;

    private transient ObjectProperty<MemeticoSnapshot> currentSnapshot = new SimpleObjectProperty<>();
    private transient ObjectProperty<ProblemInstance> currentInstance = new SimpleObjectProperty<>();
    private transient IPCLogger.View theView = NullPCLogger.NULL_VIEW;
    private transient ReadOnlyIntegerWrapper numberOfFrames = new ReadOnlyIntegerWrapper(0);
    private transient ReadOnlyObjectWrapper<Optional<Duration>> currentFrameDuration = new ReadOnlyObjectWrapper<>(null);
    private transient IntegerProperty selectedFrameIndex = new SimpleIntegerProperty(0);
    private IntegerProperty generationValue = new SimpleIntegerProperty();

    private String lastDrawnGraphName = null;
    private CanvasTSPGraph displayGraph;

    private boolean toursOutdated = false, contentOutdated = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentRoot.getStylesheets().addAll(
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/memetico/content.css").toExternalForm(),
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/common.css").toExternalForm()
        );

        displayGraph = new CanvasTSPGraph();
        graphContainer.setCenter(displayGraph.getGraphic());
//        //enable auto sizing
//        graphContainer.widthProperty().addListener(this::autoSizeListener);
//        graphContainer.heightProperty().addListener(this::autoSizeListener);
        displayGraph.getGraphic().prefWidthProperty().bind(graphContainer.widthProperty());
        displayGraph.getGraphic().prefHeightProperty().bind(graphContainer.heightProperty());
        infoPane.prefViewportHeightProperty().bind(agentsGrid.heightProperty());
        infoPane.prefViewportWidthProperty().bind(agentsGrid.widthProperty());
        infoStack.minWidthProperty().bind(
                Bindings.createDoubleBinding(
                        () -> infoPane.getViewportBounds().getWidth(), infoPane.viewportBoundsProperty()
                )
        );
        try {

            //set up the options controllers
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/org/marcos/uon/tspaidemo/gui/memetico/options/run_configuration.fxml"
                    )
            );
            loader.load();
            runConfigurationController = loader.getController();

            loader = new FXMLLoader(
                    getClass().getResource(
                            "/fxml/org/marcos/uon/tspaidemo/gui/memetico/options/display_options.fxml"
                    )
            );
            loader.load();
            displayOptionsController = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            theView = runConfigurationController.getLogger().newView();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        generationValue.bind(
                Bindings.createIntegerBinding(
                        () -> {
                            MemeticoSnapshot curSnapshot = currentSnapshot.get();
                            return curSnapshot == null ? 0 : curSnapshot.generation;
                        },
                        currentSnapshot
                )
        );


        txtGeneration.textProperty()
                .bind(generationValue.asString());
        currentInstance.bind(runConfigurationController.chosenProblemInstanceProperty());

        txtBestCost.textProperty().bind(
                Bindings.createStringBinding(
                        () -> currentSnapshot.get() == null ? "Unknown" : String.valueOf(currentSnapshot.get().bestSolution.cost),
                        currentSnapshot
                )
        );

        txtProblemName.textProperty().bind(
                Bindings.createStringBinding(
                        () -> currentInstance.get() == null ? "Unknown (Notice: You may need to wait for an old run to safely exit before a new run can start)" : currentInstance.get().getName(),
                        currentInstance
                )
        );

        txtTargetCost.textProperty().bind(
                Bindings.createStringBinding(
                        () -> currentInstance.get() == null ? "Unknown" : String.valueOf((double) currentInstance.get().getTargetCost()),
                        currentInstance
                )
        );



        displayOptionsController.getTargetDisplayToggle().addListener((e, o, n) -> toursOutdated = true);
        displayOptionsController.getTargetDisplayToggle().set(true);
        displayOptionsController.getBestDisplayToggle().addListener((e, o, n) -> toursOutdated = true);
        displayOptionsController.getBestDisplayToggle().set(true);

        //tell the options box we are ready to go
        runConfigurationController.applyConfiguration();

//        currentInstance.addListener((observable, oldValue, newValue) -> contentOutdated = true);
        currentSnapshot.addListener((observable, oldValue, newValue) -> contentOutdated = true);

//        lblTargetColor.backgroundProperty().bind(new SimpleObjectProperty<>(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, Arrays.asList(
//                new Stop(0.0, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
//                new Stop(0.08333333333, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
//                new Stop(0.08333333333, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
//                new Stop(0.36111111111, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
//                new Stop(0.36111111111, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
//                new Stop(0.63888888888, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
//                new Stop(0.63888888888, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
//                new Stop(0.9166666666, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
//                new Stop(0.9166666666, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR)
//        )), CornerRadii.EMPTY, Insets.EMPTY))));

        lblTargetColor.backgroundProperty().bind(new SimpleObjectProperty<>(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, Arrays.asList(
                new Stop(0.0, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
                new Stop(0.25, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
                new Stop(0.25, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
                new Stop(0.75, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
                new Stop(0.75, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
                new Stop(1, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR)
        )), CornerRadii.EMPTY, Insets.EMPTY))));



        lblBestColor.backgroundProperty().bind(new SimpleObjectProperty<>(new Background(new BackgroundFill(CanvasTSPGraph.DEFAULT_EDGE_COLOR, CornerRadii.EMPTY, Insets.EMPTY))));

        ViewportGestures gestures = displayGraph.getGestures();
        graphContainer.addEventHandler(MouseEvent.MOUSE_PRESSED, gestures.getOnMousePressedEventHandler());
        graphContainer.addEventHandler(MouseEvent.MOUSE_RELEASED, gestures.getOnMouseReleasedEventHandler());
        graphContainer.addEventHandler(MouseEvent.MOUSE_DRAGGED, gestures.getOnMouseDraggedEventHandler());
        graphContainer.addEventHandler(ScrollEvent.ANY, gestures.getOnScrollEventHandler());

        currentFrameDuration.bind(Bindings.createObjectBinding(
                () -> {
                    int selectedFrameIndex = this.selectedFrameIndex.get();
                    int numberOfFrames = this.numberOfFrames.get();
                    if(selectedFrameIndex >= numberOfFrames-1) {
                        return Optional.empty();
                    } else {
                        return Optional.of(Duration.ofNanos(theView.get(selectedFrameIndex+1).logTime-theView.get(selectedFrameIndex).logTime));
                    }
                }, selectedFrameIndex, numberOfFrames
        ));

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
        //TODO: MAKE THIS BIDIRECTIONAL ON ACCOUNT OF VIEW INVALIDATION?
        selectedFrameIndex.bind(source);
    }

    public void unbindSelectedFrameIndex() {
        selectedFrameIndex.unbind();
    }

    private void updateTours() {
        //disable auto scaling
        if (!displayGraph.isEmpty()) {
            //reset and re-draw predictions
            displayGraph.clearPredictions();

            TSPLibInstance theInstance = currentInstance.get().getTspLibInstance();
            if(displayOptionsController.getTargetDisplayToggle().get()) {
                displayGraph.showTargets();
            } else {
                displayGraph.hideTargets();
            }
            MemeticoSnapshot theSnapshot = currentSnapshot.get();
            if (displayOptionsController.getBestDisplayToggle().get()) {
                List<int[]> edgesToAdd = new ArrayList<>(theInstance.getDimension());
                List<Integer> bestTour = theSnapshot.bestSolution.tour;
                for(int j = 0; j<bestTour.size(); ++j) {
                    edgesToAdd.add(new int[]{bestTour.get(j), bestTour.get((j+1)%bestTour.size())});
                }
                displayGraph.addPredictionEdges(
                        edgesToAdd,
                        CanvasTSPGraph.DEFAULT_PREDICTION_COLOR
                );
            }

            List<BooleanProperty[]> toggles = displayOptionsController.getSolutionDisplayToggles();
            for (int i = 0; i < toggles.size(); ++i) {
                BooleanProperty[] eachToggles = toggles.get(i);
                AgentDisplay eachAgentController = agentControllers.get(i);
                for (int k = 0; k < eachToggles.length; ++k) {
                    if (eachToggles[k].get()) {
                        MemeticoSnapshot.LightTour eachSolution = (
                                k == 0 ?
                                        theSnapshot
                                                .agents.get(i)
                                                .pocket
                                        :
                                        theSnapshot
                                                .agents.get(i)
                                                .current
                        );
                        List<int[]> edgesToAdd = new ArrayList<>(theInstance.getDimension());
                        List<Integer> eachTour = eachSolution.tour;
                        for(int j = 0; j<eachTour.size(); ++j) {
                            edgesToAdd.add(new int[]{eachTour.get(j), eachTour.get((j+1)%eachTour.size())});
                        }
//                        double tmpCost = 0;
//                        DistanceTable tblDistance = theInstance.getDistanceTable();
//                        for (int[] ints : edgesToAdd) {
//                            tmpCost += tblDistance.getDistanceBetween(ints[0], ints[1]);
//                        }
//                        System.out.println(tmpCost);
                        displayGraph.addPredictionEdges(
                                edgesToAdd,
                                (k == 0 ? eachAgentController.getPocketColor() : eachAgentController.getCurrentColor())
                        );
                    }
                }
            }
        }
        toursOutdated = false;
    }

    public void frameCountUpdate() {
        try {
            if(!theView.isValid()) {
                //the view we want it future could be attached to a separate logger (so we can start a new run without waiting for the old run to terminate, for example)
                theView = runConfigurationController.getLogger().newView();
                currentSnapshot.set(null);
                numberOfFrames.set(0); //set it to zero at least once so that the frame index moves to zero
            }
            theView.update();
            numberOfFrames.set(theView.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void contentUpdate() {
        if(!theView.isEmpty()) {
            currentSnapshot.set(theView.get(selectedFrameIndex.get()));
        }


        MemeticoSnapshot currentValue = currentSnapshot.get();
//        agentsTree.setCellFactory(p -> new AgentTreeCell());
        //only check the complex logic if we can draw a currentSnapshot
        if(contentOutdated) {
            //first update the information on the search status
            if(!theView.isEmpty()) {
                if(currentValue.isFinal) {
                    if(currentValue.bestSolution.cost <= currentInstance.get().getTargetCost()) {
                        txtRunningStatus.setText("Success");
                        txtRunningStatus.getStyleClass().setAll("successStatus");
                    } else {
                        txtRunningStatus.setText("Failure");
                        txtRunningStatus.getStyleClass().setAll("failStatus");
                    }
                } else {
                    txtRunningStatus.setText("Unfinished");
                    txtRunningStatus.getStyleClass().clear();
                }
                NumberFormat elapsedTimeFormatter = new DecimalFormat("#0.0000");
                long totalCPUTime = currentValue.logTime - theView.getStartTime();
                txtTimeTotal.setText(elapsedTimeFormatter.format((totalCPUTime / 1_000_000_000.0)));
                txtAvgGenTime.setText(elapsedTimeFormatter.format((totalCPUTime/Math.max(1.0, (double)currentValue.generation)) / 1_000_000_000.0));
            } else {
                txtRunningStatus.setText("Unfinished");
                txtRunningStatus.getStyleClass().clear();
                txtTimeTotal.setText("Unknown");
                txtAvgGenTime.setText("Unknown");
                agentControllers.clear();
                agentsGrid.getChildren().clear();
                displayOptionsController.adjustAgentOptionsDisplay(displayOptionsController.getSolutionDisplayToggles().size(), 0);
            }

            toursOutdated = true;
            txtGenerationCount.textProperty().set(String.valueOf(!theView.isEmpty() ? theView.get(theView.size()-1).generation : 0));
            if (currentValue != null) {
                TSPLibInstance tspLibInstance = currentInstance.get().getTspLibInstance();
                ObservableList<Node> agentNodes = agentsGrid.getChildren();

                boolean listUpdated = false;
                //for all the graphs we are going to keep, if the instance changed, switch to the new one.
                if (!currentValue.instanceName.equals(lastDrawnGraphName)) {
                    displayGraph.applyInstance(tspLibInstance);
                    lastDrawnGraphName = currentValue.instanceName;
                }

                int oldCount = displayOptionsController.getSolutionDisplayToggles().size(), newCount = currentValue.agents.size();
                displayOptionsController.adjustAgentOptionsDisplay(oldCount, newCount);
                if (newCount != oldCount) {
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
                        //give the currentSnapshot to the controller
                        AgentDisplay newNode = new AgentDisplay();

                        agentControllers.add(newNode);
                        //add the data to the lists
                        agentNodes.add(newNode);
                    }
                    List<BooleanProperty[]> displayToggles = displayOptionsController.getSolutionDisplayToggles();
                    //addListeners for all the display toggles so that we still get graph display updates even when the playback is paused
                    displayToggles.subList(oldCount, newCount).forEach(each -> {
                        for (BooleanProperty eachToggle : each) {
                            eachToggle.addListener((e, o, n) -> toursOutdated = true);
                        }
                    });
                }
                //if the list was updated, the re-arrange the cells in the grid
                if (listUpdated) {
                    //use a tree structure for ease of understanding and debugging; populate using known structure from memetico

                    List<GridPositionData> arrangementInstructions = new ArrayList<>(currentValue.agents.size()); //unordered list of instructions which can be used to assign grid positions

                    //                //the following implements a horizontal-then-vertical arrangement (root is top-left)
                    //                {
                    //                    GridPositionData rootData = new GridPositionData();
                    //
                    //                    rootData.id = 0;
                    //                    rootData.column = 0;
                    //                    TreeNode<GridPositionData> root = new TreeNode<>(rootData);
                    //
                    //                    int seen = 0;
                    //                    //construct the tree by having each node create and attach their children.
                    //                    Stack<TreeNode<GridPositionData>> creationStack = new Stack<>();
                    //
                    //                    creationStack.push(root);
                    //                    while (!creationStack.isEmpty()) {
                    //                        TreeNode<GridPositionData> eachNode = creationStack.pop();
                    //                        GridPositionData eachData = eachNode.getData();
                    //                        int firstChildId = currentValue.nAry * eachData.id + 1;
                    //                        int pastChildId = Math.min(firstChildId + currentValue.nAry, currentValue.agents.size()); //value past the end of the to-create list.
                    //                        for (int i = firstChildId; i < pastChildId; ++i) {
                    //                            GridPositionData newData = new GridPositionData();
                    //                            newData.id = i;
                    //                            newData.column = eachData.column + 1;
                    //                            TreeNode<GridPositionData> newNode = new TreeNode<>(newData);
                    //                            eachNode.attach(newNode);
                    //                            creationStack.push(newNode);
                    //                        }
                    //                    }
                    //
                    //                    //now walk the root
                    //                    TreeNode<GridPositionData> current = root;
                    //                    do {
                    //                        GridPositionData curData = current.getData();
                    //                        //if unseen, (row == -1), drill down
                    //                        if (curData.row == -1) {
                    //                            curData.row = seen++;
                    //                            arrangementInstructions.add(curData);
                    //                            if (!current.isLeaf()) {
                    //                                current = current.children().get(0);
                    //                                continue;
                    //                            }
                    //                        }
                    //                        if (current.hasNextSibling()) {
                    //                            current = current.nextSibling();
                    //                        } else {
                    //                            //if we can't drill down further, drill up until we can
                    //                            while (!current.isRoot() && current.getData().row != -1) {
                    //                                current = current.parent();
                    //                                if (current.hasNextSibling()) {
                    //                                    current = current.nextSibling();
                    //                                }
                    //                            }
                    //                        }
                    //                    } while (current != root);
                    //                }

                    //the following implements a vertical-then-horizontal arrangement (root is at the center-top)
                    {
                        //construct the tree by having each node create and attach their children.
                        Queue<TreeNode<GridPositionData>> creationQueue = new ArrayDeque<>();
                        GridPositionData rootData = new GridPositionData();
                        int seen = 0;
                        int columnsAllocated = 0;
                        rootData.id = 0;
                        rootData.row = 0;
                        rootData.colorIndex = seen++;
                        TreeNode<GridPositionData> root = new TreeNode<>(rootData);
                        creationQueue.add(root);

                        Stack<TreeNode<GridPositionData>> arrangementStack = new Stack<>();
                        while (!creationQueue.isEmpty()) {
                            TreeNode<GridPositionData> eachNode = creationQueue.remove();
                            GridPositionData eachData = eachNode.getData();
                            int firstChildId = currentValue.nAry * eachData.id + 1;
                            int pastChildId = Math.min(firstChildId + currentValue.nAry, currentValue.agents.size()); //value past the end of the to-create list.
                            for (int i = firstChildId; i < pastChildId; ++i) {
                                GridPositionData newData = new GridPositionData();
                                newData.id = i;
                                newData.row = eachData.row + 1;
                                newData.colorIndex = seen++;
                                TreeNode<GridPositionData> newNode = new TreeNode<>(newData);
                                eachNode.attach(newNode);
                                creationQueue.add(newNode);
                            }
                            //if it's not an orphan, add it to the stack to column-positioned later;
                            //if it is an orphan, we can position it now; (this ensures the left-most branch is full if the tree were to be unbalanced)
                            if (eachNode.isLeaf()) {
                                eachData.column = columnsAllocated++;

                                //we want placeholders to leave empty columns between subpopulations visually
                                if (eachData.id != currentValue.agents.size() - 1 && eachNode.parent().children().indexOf(eachNode) == eachNode.parent().children().size() - 1) {
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

                        //all lowest-level nodes have been positioned, so we can safely position progressive parents
                        while (!arrangementStack.isEmpty()) {
                            TreeNode<GridPositionData> eachNode = arrangementStack.pop();
                            GridPositionData eachData = eachNode.getData();
                            List<TreeNode<GridPositionData>> eachChildren = eachNode.children();
                            //use the middle for odd-numbers and for evens, base it on which side we are on within the parents
                            int childToAlignTo;
                            if (eachChildren.size() % 2 == 1) {
                                //uneven is the easy option
                                childToAlignTo = eachChildren.size() / 2;
                            } else if (!eachNode.isRoot()) {
                                List<TreeNode<GridPositionData>> eachSiblings = eachNode.parent().children();
                                if (eachSiblings.indexOf(eachNode) >= eachSiblings.size() / 2) {
                                    childToAlignTo = eachChildren.size() / 2 - 1;
                                } else {
                                    childToAlignTo = eachChildren.size() / 2;
                                }
                            } else {

                                childToAlignTo = eachChildren.size() / 2 - 1;
                            }
                            eachData.column = eachNode.children().get(childToAlignTo).getData().column;
                            arrangementInstructions.add(eachData);
                        }


                    }

                    double hueSegmentSize = 360.0 / (newCount);
                    for (GridPositionData eachData : arrangementInstructions) {
                        if (eachData.id == -1) {
                            //create a placeholder
                            Pane placeHolder = new Pane();
                            placeHolder.getStyleClass().add("filler");
                            placeHolder.setMinWidth(25);
                            agentNodes.add(placeHolder);
                            GridPane.setRowIndex(placeHolder, eachData.row);
                            GridPane.setColumnIndex(placeHolder, eachData.column);
                            GridPane.setColumnSpan(placeHolder, 1);
                        } else {

                            AgentDisplay eachAgent = agentControllers.get(eachData.id);
                            GridPane.setRowIndex(eachAgent, eachData.row);
                            GridPane.setColumnIndex(eachAgent, eachData.column);
                            double eachHue = hueSegmentSize * eachData.colorIndex;
                            eachAgent.setPocketColor(Color.hsb(eachHue, 1, 1, 0.75));
                            eachAgent.setCurrentColor(Color.hsb(eachHue, 1, 0.70, 0.75));
                        }
                    }

                    //                //setup some colours
                    //
                    //                for(int i=0; i<newCount; ++i) {
                    //                    double eachHue = hueSegmentSize*GridPane.getRowIndex(eachAgent);
                    //
                    //                    eachAgent.setPocketColor(Color.hsb(eachHue, 1, 1, 0.75));
                    //                    eachAgent.setCurrentColor(Color.hsb(eachHue, 1, 0.70, 0.75));
                    //                }
                }

                for (int i = 0; i < agentControllers.size(); ++i) {
                    agentControllers.get(i).setSnapShot(currentSnapshot.get().agents.get(i));
                }
            } else if (currentSnapshot.get() == null) {
                for (int i = 0; i < agentControllers.size(); ++i) {
                    agentControllers.get(i).setSnapShot(null);
                }
                displayGraph.clearInstance();
                lastDrawnGraphName = null;
            }
            contentOutdated = false;
        }
        if (toursOutdated) {
            updateTours();
        }
        displayGraph.draw(); //the display graph knows if it has updates to consider other than the data we give it
    }

    @Override
    public ReadOnlyObjectProperty<Optional<Duration>> currentFrameDurationProperty() {
        return currentFrameDuration.getReadOnlyProperty();
    }

    public void showConfiguration() {
        runConfigurationController.open();
    }

    public void showDisplayOptions() {
        displayOptionsController.open();
    }

    /**
     * {@inheritDoc}
     */
    public Parent getRoot() {
        return contentRoot;
    }
}


