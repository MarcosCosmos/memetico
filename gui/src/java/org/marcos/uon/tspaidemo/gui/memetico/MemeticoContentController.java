package org.marcos.uon.tspaidemo.gui.memetico;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Text;
import memetico.logging.IPCLogger;
import memetico.logging.NullPCLogger;
import memetico.logging.MemeticoSnapshot;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.marcos.uon.tspaidemo.canvas.CanvasTSPGraph;
import org.marcos.uon.tspaidemo.gui.main.ContentController;
import org.marcos.uon.tspaidemo.gui.memetico.agent.AgentDisplay;
import org.marcos.uon.tspaidemo.gui.memetico.options.OptionsBoxController;
import org.marcos.uon.tspaidemo.util.tree.TreeNode;
import memetico.util.ProblemInstance;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private Text txtGeneration, txtProblemName, txtTargetCost, txtBestCost, txtTimeGeneration, txtTimeTotal;
    @FXML
    private Label lblTargetColor, lblBestColor;
    @FXML
    private GridPane agentsGrid;
    @FXML
    private BorderPane titleBar, graphContainer;

    private List<AgentDisplay> agentControllers = new ArrayList<>();

    private OptionsBoxController optionsBoxController;

    private transient ObjectProperty<MemeticoSnapshot> currentSnapshot = new SimpleObjectProperty<>();
    private transient ObjectProperty<ProblemInstance> currentInstance = new SimpleObjectProperty<>();
    private transient IPCLogger.View theView = NullPCLogger.NULL_VIEW;
    private transient ReadOnlyIntegerWrapper numberOfFrames = new ReadOnlyIntegerWrapper(0);
    private transient IntegerProperty selectedFrameIndex = new SimpleIntegerProperty(0);
    private IntegerProperty generationValue = new SimpleIntegerProperty();

    private String lastDrawnGraphName = null;
    private CanvasTSPGraph displayGraph;



    private boolean toursOutdated = false, contentOutdated = false;

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
        displayGraph.requestRedraw(); //unneeded, it is automatically handled by the canvas now? (no?)
        toursOutdated = true;
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentRoot.getStylesheets().addAll(
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/memetico/content.css").toExternalForm(),
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/common.css").toExternalForm()
        );

        displayGraph = new CanvasTSPGraph();
        graphContainer.setCenter(displayGraph.getGraphic());
        //enable auto sizing
        graphContainer.widthProperty().addListener(this::autoSizeListener);
        graphContainer.heightProperty().addListener(this::autoSizeListener);
        infoPane.prefViewportHeightProperty().bind(agentsGrid.heightProperty());
        infoPane.prefViewportWidthProperty().bind(agentsGrid.widthProperty());
        infoStack.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                infoPane.getViewportBounds().getWidth(), infoPane.viewportBoundsProperty()));
            try {

                //set up the content display with an observable reference to the current currentSnapshot to display.
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(
                                "/fxml/org/marcos/uon/tspaidemo/gui/memetico/options/options_box.fxml"
                        )
                );
                loader.load();
                optionsBoxController = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            theView = optionsBoxController.getLogger().newView();
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
        currentInstance.bind(Bindings.createObjectBinding(
                () -> currentSnapshot.get() == null ? null : optionsBoxController.getInstances().get(currentSnapshot.get().instanceName),
                currentSnapshot
        ));

        txtTimeGeneration.textProperty().bind(Bindings.createStringBinding(
                () -> currentSnapshot.get() == null ? "Unknown" : String.valueOf(TimeUnit.NANOSECONDS.toSeconds(currentSnapshot.get().logTime - (selectedFrameIndex.get() == 0 ? theView.getStartTime() : theView.get(selectedFrameIndex.get()-1).logTime))),
                currentSnapshot
        ));
        txtTimeTotal.textProperty().bind(Bindings.createStringBinding(
                () -> currentSnapshot.get() == null ? "Unknown" : String.valueOf(TimeUnit.NANOSECONDS.toSeconds(currentSnapshot.get().logTime - theView.getStartTime())),
                currentSnapshot
        ));

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



        optionsBoxController.getTargetDisplayToggle().addListener((e,o,n) -> toursOutdated = true);
        optionsBoxController.getTargetDisplayToggle().set(true);
        optionsBoxController.getBestDisplayToggle().addListener((e,o,n) -> toursOutdated = true);
        optionsBoxController.getBestDisplayToggle().set(true);

        //tell the options box we are ready to go
        optionsBoxController.applyConfiguration();

        currentInstance.addListener((observable, oldValue, newValue) -> contentOutdated = true);
        selectedFrameIndex.addListener((observable, oldValue, newValue) -> contentOutdated = true);

        lblTargetColor.backgroundProperty().set(new Background(new BackgroundFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, Arrays.asList(
                new Stop(0.0, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
                new Stop(0.08333333333, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
                new Stop(0.08333333333, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
                new Stop(0.36111111111, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
                new Stop(0.36111111111, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
                new Stop(0.63888888888, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR),
                new Stop(0.63888888888, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
                new Stop(0.9166666666, CanvasTSPGraph.DEFAULT_TARGET_EDGE_COLOR),
                new Stop(0.9166666666, CanvasTSPGraph.DEFAULT_BACKGROUND_COLOR)
        )), CornerRadii.EMPTY, Insets.EMPTY)));


        lblBestColor.backgroundProperty().set(new Background(new BackgroundFill(CanvasTSPGraph.DEFAULT_EDGE_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

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
            if(optionsBoxController.getTargetDisplayToggle().get()) {
                displayGraph.showTargets();
            } else {
                displayGraph.hideTargets();
            }
            MemeticoSnapshot theSnapshot = currentSnapshot.get();
            if (optionsBoxController.getBestDisplayToggle().get()) {
                int[][] edgesToAdd = new int[theInstance.getDimension()][];
                List<Integer> bestTour = theSnapshot.bestSolution.tour;
                for(int j = 0; j<bestTour.size(); ++j) {
                    edgesToAdd[j] = new int[]{bestTour.get(j), bestTour.get((j+1)%bestTour.size())};
                }
                displayGraph.addPredictionEdges(
                        Arrays.asList(edgesToAdd),
                        CanvasTSPGraph.DEFAULT_PREDICTION_COLOR
                );
            }

            List<BooleanProperty[]> toggles = optionsBoxController.getSolutionDisplayToggles();
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
                        int[][] edgesToAdd = new int[theInstance.getDimension()][];
                        List<Integer> bestTour = eachSolution.tour;
                        for(int j = 0; j<bestTour.size(); ++j) {
                            edgesToAdd[j] = new int[]{bestTour.get(j), bestTour.get((j+1)%bestTour.size())};
                        }
//                        double tmpCost = 0;
//                        DistanceTable tblDistance = theInstance.getDistanceTable();
//                        for (int[] ints : edgesToAdd) {
//                            tmpCost += tblDistance.getDistanceBetween(ints[0], ints[1]);
//                        }
//                        System.out.println(tmpCost);
                        displayGraph.addPredictionEdges(
                                Arrays.asList(edgesToAdd),
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
                theView = optionsBoxController.getLogger().newView();
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
        if(contentOutdated && currentSnapshot.get() != null) {
            toursOutdated = true;
            TSPLibInstance tspLibInstance = currentInstance.get().getTspLibInstance();
            ObservableList<Node> agentNodes = agentsGrid.getChildren();

            boolean listUpdated = false;
            //for all the graphs we are going to keep, if the instance changed, switch to the new one.
            if (!currentValue.instanceName.equals(lastDrawnGraphName)) {
                displayGraph.applyInstance(tspLibInstance);
                if (!displayGraph.isEmpty()) {
                    autoSizeListener(null, -1, -1);
                }
                lastDrawnGraphName = currentValue.instanceName;
            }

            int oldCount = optionsBoxController.getSolutionDisplayToggles().size(), newCount = currentValue.agents.size();
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
                    //give the currentSnapshot to the controller
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

                double hueSegmentSize = 360.0/(newCount);
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
                        double eachHue = hueSegmentSize*eachData.colorIndex;
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

            for(int i=0; i<agentControllers.size(); ++i) {
                agentControllers.get(i).setSnapShot(currentSnapshot.get().agents.get(i));
            }
        } else if(currentSnapshot.get() == null){
            toursOutdated = false;
            lastDrawnGraphName = null;
        }
        if(toursOutdated) {
            updateTours();
        }
        displayGraph.draw();
        contentOutdated = false;
    }

    public void showOptionsBox() {
        optionsBoxController.open();
    }

    /**
     * {@inheritDoc}
     */
    public Parent getRoot() {
        return contentRoot;
    }
}


